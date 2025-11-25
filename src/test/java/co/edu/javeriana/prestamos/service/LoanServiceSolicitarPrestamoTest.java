package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.model.Configuracion;
import co.edu.javeriana.prestamos.model.Prestamo;
import co.edu.javeriana.prestamos.repository.ConfiguracionRepository;
import co.edu.javeriana.prestamos.repository.LibroRepository;
import co.edu.javeriana.prestamos.repository.PrestamoRepository;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.notification.NotificationService;
import co.edu.javeriana.prestamos.service.fine.MultaStrategyFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceSolicitarPrestamoTest {

    private LoanService newService(CatalogClient catalog,
                                  LibroRepository libroRepo,
                                  MappingService mapping,
                                  PrestamoRepository prestamoRepo,
                                  ReservationService reservationService,
                                  ConfiguracionRepository configuracionRepository,
                                  UsuarioRepository usuarioRepository,
                                  NotificationService notificationService,
                                  MultaStrategyFactory multaStrategyFactory) {
        return new LoanService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
    }

    @Test
    void solicitarPrestamo_ok_reservaEnG3_y_guardaEnBD() {
        CatalogClient catalog = mock(CatalogClient.class);
        LibroRepository libroRepo = mock(LibroRepository.class);
        MappingService mapping = mock(MappingService.class);
        PrestamoRepository prestamoRepo = mock(PrestamoRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        MultaStrategyFactory multaStrategyFactory = new MultaStrategyFactory();

        when(mapping.mapToDbId(123)).thenReturn(96);
        when(libroRepo.existsById(96)).thenReturn(true);
        when(catalog.reservarUno("123")).thenReturn(true);
        when(prestamoRepo.findActivosYVencidosByUsuarioId(7)).thenReturn(new ArrayList<>());
        when(configuracionRepository.findByClave(anyString())).thenReturn(Optional.empty());

        ArgumentCaptor<Prestamo> cap = ArgumentCaptor.forClass(Prestamo.class);
        when(prestamoRepo.save(cap.capture())).thenAnswer(a -> {
            Prestamo p = cap.getValue();
            p.setId_prestamo(1);
            return p;
        });

        LoanService service = newService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
        Prestamo p = service.solicitarPrestamo(7, 123);

        assertNotNull(p.getId_prestamo());
        assertEquals(LoanService.ESTADO_SOLICITADO, p.getId_estado_prestamo());
        assertEquals(96, p.getId_libro());
    }

    @Test
    void solicitarPrestamo_falla_si_catalogoSinDisponibilidad() {
        CatalogClient catalog = mock(CatalogClient.class);
        LibroRepository libroRepo = mock(LibroRepository.class);
        MappingService mapping = mock(MappingService.class);
        PrestamoRepository prestamoRepo = mock(PrestamoRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        MultaStrategyFactory multaStrategyFactory = new MultaStrategyFactory();

        when(mapping.mapToDbId(123)).thenReturn(96);
        when(libroRepo.existsById(96)).thenReturn(true);
        when(catalog.reservarUno("123")).thenReturn(false);
        when(configuracionRepository.findByClave(anyString())).thenReturn(Optional.empty());

        LoanService service = newService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
        Exception ex = assertThrows(Exception.class, () -> service.solicitarPrestamo(7, 123));
        assertTrue(ex.getMessage().toLowerCase().contains("no disponible") || ex.getMessage().toLowerCase().contains("no encontrado"));
    }

    @Test
    void solicitarPrestamo_falla_por_limiteTresActivos() {
        CatalogClient catalog = mock(CatalogClient.class);
        LibroRepository libroRepo = mock(LibroRepository.class);
        MappingService mapping = mock(MappingService.class);
        PrestamoRepository prestamoRepo = mock(PrestamoRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        MultaStrategyFactory multaStrategyFactory = new MultaStrategyFactory();

        when(mapping.mapToDbId(123)).thenReturn(96);
        when(libroRepo.existsById(96)).thenReturn(true);
        when(catalog.reservarUno("123")).thenReturn(true);

        List<Prestamo> existentes = List.of(
                new Prestamo(1, 7, 10, LoanService.ESTADO_ACTIVO, LocalDateTime.now().plusDays(5)),
                new Prestamo(2, 7, 11, LoanService.ESTADO_ACTIVO, LocalDateTime.now().plusDays(5)),
                new Prestamo(3, 7, 12, LoanService.ESTADO_ACTIVO, LocalDateTime.now().plusDays(5))
        );
        when(prestamoRepo.findActivosYVencidosByUsuarioId(7)).thenReturn(existentes);
        when(configuracionRepository.findByClave(anyString())).thenReturn(Optional.empty());
        when(configuracionRepository.findByClave(eq("max_prestamos_simultaneos")))
                .thenReturn(Optional.of(new Configuracion("max_prestamos_simultaneos", "3")));

        LoanService service = newService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
        Exception ex = assertThrows(Exception.class, () -> service.solicitarPrestamo(7, 123));
        assertTrue(ex.getMessage().toLowerCase().contains("limite") || ex.getMessage().contains("3"));
    }

    @Test
    void solicitarPrestamo_falla_por_vencidos() {
        CatalogClient catalog = mock(CatalogClient.class);
        LibroRepository libroRepo = mock(LibroRepository.class);
        MappingService mapping = mock(MappingService.class);
        PrestamoRepository prestamoRepo = mock(PrestamoRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        MultaStrategyFactory multaStrategyFactory = new MultaStrategyFactory();

        when(mapping.mapToDbId(123)).thenReturn(96);
        when(libroRepo.existsById(96)).thenReturn(true);
        when(catalog.reservarUno("123")).thenReturn(true);

        List<Prestamo> existentes = List.of(
                new Prestamo(1, 7, 10, LoanService.ESTADO_VENCIDO, LocalDateTime.now().minusDays(2))
        );
        when(prestamoRepo.findActivosYVencidosByUsuarioId(7)).thenReturn(existentes);
        when(configuracionRepository.findByClave(anyString())).thenReturn(Optional.empty());

        LoanService service = newService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
        Exception ex = assertThrows(Exception.class, () -> service.solicitarPrestamo(7, 123));
        assertTrue(ex.getMessage().toLowerCase().contains("vencid"));
    }

    @Test
    void solicitarPrestamo_falla_si_libroNoExisteEnBD() {
        CatalogClient catalog = mock(CatalogClient.class);
        LibroRepository libroRepo = mock(LibroRepository.class);
        MappingService mapping = mock(MappingService.class);
        PrestamoRepository prestamoRepo = mock(PrestamoRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        MultaStrategyFactory multaStrategyFactory = new MultaStrategyFactory();

        when(mapping.mapToDbId(123)).thenReturn(96);
        when(libroRepo.existsById(96)).thenReturn(false);
        when(configuracionRepository.findByClave(anyString())).thenReturn(Optional.empty());

        LoanService service = newService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
        Exception ex = assertThrows(Exception.class, () -> service.solicitarPrestamo(7, 123));
        assertTrue(ex.getMessage().toLowerCase().contains("no existe"));
    }
}
