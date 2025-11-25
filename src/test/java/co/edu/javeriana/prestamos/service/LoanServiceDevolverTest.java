package co.edu.javeriana.prestamos.service;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceDevolverTest {

    @Test
    void devolverPrestamo_ok_actualizaEstado_y_notificaCola() throws Exception {
        CatalogClient catalog = mock(CatalogClient.class);
        LibroRepository libroRepo = mock(LibroRepository.class);
        MappingService mapping = mock(MappingService.class);
        PrestamoRepository prestamoRepo = mock(PrestamoRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        MultaStrategyFactory multaStrategyFactory = new MultaStrategyFactory();

        Prestamo p = new Prestamo(10, 7, 96, LoanService.ESTADO_ACTIVO, LocalDateTime.now().plusDays(7));
        when(prestamoRepo.findById(10)).thenReturn(Optional.of(p));
        when(catalog.devolverUno("96")).thenReturn(true);

        ArgumentCaptor<Prestamo> cap = ArgumentCaptor.forClass(Prestamo.class);
        when(prestamoRepo.save(cap.capture())).thenAnswer(a -> cap.getValue());

        when(configuracionRepository.findByClave(anyString())).thenReturn(java.util.Optional.empty());

        LoanService service = new LoanService(catalog, libroRepo, mapping, prestamoRepo, reservationService, configuracionRepository, usuarioRepository, notificationService, multaStrategyFactory);
        Prestamo actualizado = service.devolverPrestamo(10, 7, true);

        assertEquals(LoanService.ESTADO_DEVUELTO, actualizado.getId_estado_prestamo());
        verify(reservationService, times(1)).notifyNextInQueue(96);
    }
}
