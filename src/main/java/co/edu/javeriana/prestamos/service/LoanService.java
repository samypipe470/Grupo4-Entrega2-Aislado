package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.exception.BusinessException;
import co.edu.javeriana.prestamos.model.Prestamo;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.ConfiguracionRepository;
import co.edu.javeriana.prestamos.repository.LibroRepository;
import co.edu.javeriana.prestamos.repository.PrestamoRepository;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.service.state.EstadoPrestamo;
import co.edu.javeriana.prestamos.service.state.EstadoPrestamoFactory;
import co.edu.javeriana.prestamos.service.fine.MultaStrategy;
import co.edu.javeriana.prestamos.service.fine.MultaStrategyFactory;
import co.edu.javeriana.prestamos.notification.NotificationEvent;
import co.edu.javeriana.prestamos.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoanService {

    public static final int ESTADO_SOLICITADO = 1;
    public static final int ESTADO_ACTIVO = 2;
    public static final int ESTADO_DEVUELTO = 3;
    public static final int ESTADO_VENCIDO = 4;

    private static final String CFG_MAX_PRESTAMOS = "max_prestamos_simultaneos";
    private static final String CFG_PERIODO_PRESTAMO = "periodo_prestamo_dias";
    private static final String CFG_DIAS_RENOVACION = "dias_renovacion";
    private static final String CFG_VALOR_MULTA_DIARIA = "valor_multa_diaria";

    private final CatalogClient catalogClient;
    private final LibroRepository libroRepository;
    private final MappingService mappingService;
    private final PrestamoRepository prestamoRepository;
    private final ReservationService reservationService;
    private final ConfiguracionRepository configuracionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationService notificationService;
    private final MultaStrategyFactory multaStrategyFactory;
    private final Map<String, Integer> configuracionCache = new ConcurrentHashMap<>();
    private final Map<String, Double> configuracionDoubleCache = new ConcurrentHashMap<>();

    public LoanService(CatalogClient catalogClient,
                       LibroRepository libroRepository,
                       MappingService mappingService,
                       PrestamoRepository prestamoRepository,
                       ReservationService reservationService,
                       ConfiguracionRepository configuracionRepository,
                       UsuarioRepository usuarioRepository,
                       NotificationService notificationService,
                       MultaStrategyFactory multaStrategyFactory) {
        this.catalogClient = catalogClient;
        this.libroRepository = libroRepository;
        this.mappingService = mappingService;
        this.prestamoRepository = prestamoRepository;
        this.reservationService = reservationService;
        this.configuracionRepository = configuracionRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificationService = notificationService;
        this.multaStrategyFactory = multaStrategyFactory;
    }

    @Transactional
    public Prestamo solicitarPrestamo(Integer usuarioId, Integer libroId) {
        Integer dbLibroId = mappingService.mapToDbId(libroId);
        if (dbLibroId == null) {
            dbLibroId = libroId;
        }
        if (!libroRepository.existsById(dbLibroId)) {
            throw new BusinessException("Error 400: El id_libro=" + dbLibroId + " no existe en la BD. Configure el mapeo BOOK_ID_MAP en G4.");
        }

        markOverdueLoans();

        List<Prestamo> prestamosDelUsuario = prestamoRepository.findActivosYVencidosByUsuarioId(usuarioId);
        boolean tieneVencidos = prestamosDelUsuario.stream()
                .anyMatch(p -> p.getId_estado_prestamo() == ESTADO_VENCIDO);
        if (tieneVencidos) {
            throw new BusinessException("Error 400: El usuario tiene préstamos vencidos.");
        }

        int maxPrestamos = getConfigInt(CFG_MAX_PRESTAMOS, Integer.MAX_VALUE);
        long prestamosNoDevueltos = prestamosDelUsuario.stream()
                .filter(p -> {
                    int estado = p.getId_estado_prestamo();
                    return estado == ESTADO_SOLICITADO || estado == ESTADO_ACTIVO || estado == ESTADO_VENCIDO;
                })
                .count();
        if (prestamosNoDevueltos >= maxPrestamos) {
            throw new BusinessException("Error 400: Límite de " + maxPrestamos + " préstamos alcanzado.");
        }

        boolean reservado = false;
        try {
            reservado = catalogClient.reservarUno(String.valueOf(libroId));
            if (!reservado) {
                throw new BusinessException("Error 400: Libro no disponible o no encontrado en Catálogo.");
            }

            int prestamoDias = getConfigInt(CFG_PERIODO_PRESTAMO, 14);
            LocalDateTime fechaDevolucion = LocalDateTime.now().plusDays(prestamoDias);
            Prestamo nuevoPrestamo = new Prestamo(null, usuarioId, dbLibroId, ESTADO_SOLICITADO, fechaDevolucion);
            Prestamo guardado = prestamoRepository.save(nuevoPrestamo);
            notificarPrestamoConfirmado(guardado);
            return guardado;
        } catch (RuntimeException e) {
            if (reservado) {
                try {
                    catalogClient.devolverUno(String.valueOf(libroId));
                } catch (Exception ignored) {
                }
            }
            throw e;
        }
    }

    public List<Prestamo> getMisPrestamos(Integer usuarioId) {
        markOverdueLoans();
        return prestamoRepository.findActivosYVencidosByUsuarioId(usuarioId);
    }

    public Optional<Prestamo> getPrestamoById(Integer id) {
        return prestamoRepository.findById(id);
    }

    @Transactional
    public Prestamo devolverPrestamo(Integer prestamoId, Integer requesterId, boolean isPrivileged) {
        Prestamo p = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new BusinessException("Error 404: Préstamo no encontrado."));

        if (!isPrivileged && !p.getId_usuario().equals(requesterId)) {
            throw new BusinessException("Error 403: No autorizado.");
        }

        if (p.getId_estado_prestamo() == ESTADO_DEVUELTO) {
            throw new BusinessException("Error 400: El préstamo ya está devuelto.");
        }

        boolean ok = catalogClient.devolverUno(String.valueOf(p.getId_libro()));
        if (!ok) {
            throw new RuntimeException("Error 500: No fue posible actualizar disponibilidad en Catálogo.");
        }

        try {
            EstadoPrestamo estado = EstadoPrestamoFactory.fromCode(p.getId_estado_prestamo());
            p = estado.devolver(p);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        p = prestamoRepository.save(p);
        try {
            reservationService.notifyNextInQueue(p.getId_libro());
        } catch (Exception ignored) {
        }
        aplicarMultaSiAplica(p);
        return p;
    }

    @Transactional
    public Prestamo renovarPrestamo(Integer prestamoId, Integer requesterId, boolean isPrivileged) {
        Prestamo p = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new BusinessException("Error 404: Préstamo no encontrado."));

        if (!isPrivileged && !p.getId_usuario().equals(requesterId)) {
            throw new BusinessException("Error 403: No autorizado.");
        }
        if (p.getId_estado_prestamo() != ESTADO_ACTIVO) {
            throw new BusinessException("Error 400: Solo se pueden renovar préstamos activos.");
        }

        int periodoPrestamoDias = getConfigInt(CFG_PERIODO_PRESTAMO, 14);
        int diasRenovacion = getConfigInt(CFG_DIAS_RENOVACION, 7);
        LocalDateTime baseDue = p.getFecha_prestamo().plusDays(periodoPrestamoDias);
        boolean yaRenovado = p.getFecha_devolucion_esperada() != null && p.getFecha_devolucion_esperada().isAfter(baseDue);
        if (yaRenovado) {
            throw new BusinessException("Error 400: El préstamo ya fue renovado una vez.");
        }
        p.setFecha_devolucion_esperada(baseDue.plusDays(diasRenovacion));
        return prestamoRepository.save(p);
    }

    @Transactional
    public Prestamo aprobarPrestamo(Integer prestamoId) {
        Prestamo p = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new BusinessException("Error 404: Préstamo no encontrado."));
        try {
            EstadoPrestamo estado = EstadoPrestamoFactory.fromCode(p.getId_estado_prestamo());
            p = estado.aprobar(p);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return prestamoRepository.save(p);
    }

    public List<Prestamo> listarPrestamos(Integer estado) {
        markOverdueLoans();
        if (estado == null) {
            return prestamoRepository.findAll();
        }
        return prestamoRepository.findByEstado(estado);
    }

    public List<Prestamo> listarVencidos() {
        markOverdueLoans();
        return prestamoRepository.findByEstado(ESTADO_VENCIDO);
    }

    private void markOverdueLoans() {
        List<Prestamo> overdue = prestamoRepository.findOverdueNow();
        if (overdue.isEmpty()) {
            return;
        }
        overdue.forEach(p -> p.setId_estado_prestamo(ESTADO_VENCIDO));
        prestamoRepository.saveAll(overdue);
    }

    private int getConfigInt(String key, int fallback) {
        return configuracionCache.computeIfAbsent(key, k ->
                configuracionRepository.findByClave(k)
                        .map(cfg -> parseConfigValue(k, cfg.getValor(), fallback))
                        .orElse(fallback)
        );
    }

    private int parseConfigValue(String key, String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Valor inválido para configuración '" + key + "': " + raw);
        }
    }

    private double getConfigDouble(String key, double fallback) {
        return configuracionDoubleCache.computeIfAbsent(key, k ->
                configuracionRepository.findByClave(k)
                        .map(cfg -> parseConfigDoubleValue(k, cfg.getValor(), fallback))
                        .orElse(fallback)
        );
    }

    private double parseConfigDoubleValue(String key, String raw, double fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Valor inválido para configuración '" + key + "': " + raw);
        }
    }

    private void aplicarMultaSiAplica(Prestamo p) {
        if (p.getFecha_devolucion_esperada() == null || p.getFecha_devolucion_real() == null) {
            return;
        }
        if (!p.getFecha_devolucion_real().isAfter(p.getFecha_devolucion_esperada())) {
            p.setValor_multa(0d);
            prestamoRepository.save(p);
            return;
        }
        double tarifa = getConfigDouble(CFG_VALOR_MULTA_DIARIA, 0d);
        Integer tipoUsuario = usuarioRepository.findById(p.getId_usuario())
                .map(Usuario::getId_tipo_usuario)
                .orElse(null);
        MultaStrategy strategy = multaStrategyFactory.forUserType(tipoUsuario);
        double valor = strategy.calcular(p.getFecha_devolucion_esperada(), p.getFecha_devolucion_real(), tarifa);
        p.setValor_multa(valor);
        prestamoRepository.save(p);
        if (valor > 0) {
            notificationService.publish(NotificationEvent.of(
                    "MULTA_GENERADA",
                    Map.of(
                            "usuarioId", p.getId_usuario(),
                            "prestamoId", p.getId_prestamo(),
                            "valor", valor
                    )
            ));
        }
    }

    private void notificarPrestamoConfirmado(Prestamo p) {
        notificationService.publish(NotificationEvent.of(
                "PRESTAMO_CONFIRMADO",
                Map.of(
                        "usuarioId", p.getId_usuario(),
                        "prestamoId", p.getId_prestamo(),
                        "libroId", p.getId_libro(),
                        "fechaDevolucionEsperada", p.getFecha_devolucion_esperada()
                )
        ));
    }

    public void marcarPrestamosVencidosProgramado() {
        markOverdueLoans();
    }

    public void notificarVencimientosProximos(int diasAntes) {
        LocalDateTime desde = LocalDateTime.now().plusDays(diasAntes).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime hasta = desde.plusDays(1);
        List<Prestamo> proximos = prestamoRepository.findActiveDueBetween(desde, hasta);
        for (Prestamo p : proximos) {
            notificationService.publish(NotificationEvent.of(
                    "PRESTAMO_POR_VENCER",
                    Map.of(
                            "usuarioId", p.getId_usuario(),
                            "prestamoId", p.getId_prestamo(),
                            "fechaDevolucionEsperada", p.getFecha_devolucion_esperada()
                    )
            ));
        }
    }
}
