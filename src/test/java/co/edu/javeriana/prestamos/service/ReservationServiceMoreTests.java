package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.model.Reserva;
import co.edu.javeriana.prestamos.notification.NotificationEvent;
import co.edu.javeriana.prestamos.notification.NotificationService;
import co.edu.javeriana.prestamos.repository.ConfiguracionRepository;
import co.edu.javeriana.prestamos.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceMoreTests {

    @Test
    void createReservation_falla_si_yaExisteActivaONotificada() {
        ReservaRepository repo = mock(ReservaRepository.class);
        MappingService mapping = mock(MappingService.class);
        CatalogClient client = mock(CatalogClient.class);
        NotificationService notif = mock(NotificationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);

        when(mapping.mapToDbId(123)).thenReturn(96);
        CatalogClient.BookDto book = new CatalogClient.BookDto();
        book.cantidadDisponible = 0;
        when(client.getBook("123")).thenReturn(book);
        when(repo.existsActiveOrNotified(7, 96, List.of(1,2))).thenReturn(true);

        when(configuracionRepository.findByClave(anyString())).thenReturn(java.util.Optional.empty());

        ReservationService service = new ReservationService(repo, mapping, client, notif, configuracionRepository);
        Exception ex = assertThrows(Exception.class, () -> service.createReservation(7, 123));
        assertTrue(ex.getMessage().contains("409") || ex.getMessage().toLowerCase().contains("ya tiene"));
    }

    @Test
    void cancelReservation_noPrivilegiadoNoPropietario_devuelve403() {
        ReservaRepository repo = mock(ReservaRepository.class);
        MappingService mapping = mock(MappingService.class);
        CatalogClient client = mock(CatalogClient.class);
        NotificationService notif = mock(NotificationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);

        Reserva r = new Reserva(5, 7, 96, ReservationService.ESTADO_PENDIENTE);
        when(repo.findById(5)).thenReturn(Optional.of(r));

        when(configuracionRepository.findByClave(anyString())).thenReturn(java.util.Optional.empty());

        ReservationService service = new ReservationService(repo, mapping, client, notif, configuracionRepository);
        Exception ex = assertThrows(Exception.class, () -> service.cancelReservation(5, 8, false));
        assertTrue(ex.getMessage().contains("403") || ex.getMessage().toLowerCase().contains("no autorizado"));
    }

    @Test
    void notifyNextInQueue_promuevePrimeroYPublicaEvento() {
        ReservaRepository repo = mock(ReservaRepository.class);
        MappingService mapping = mock(MappingService.class);
        CatalogClient client = mock(CatalogClient.class);
        NotificationService notif = mock(NotificationService.class);
        ConfiguracionRepository configuracionRepository = mock(ConfiguracionRepository.class);

        Reserva r1 = new Reserva(1, 7, 96, ReservationService.ESTADO_PENDIENTE);
        r1.setFecha_reserva(LocalDateTime.now().minusHours(3));
        Reserva r2 = new Reserva(2, 8, 96, ReservationService.ESTADO_PENDIENTE);
        r2.setFecha_reserva(LocalDateTime.now().minusHours(1));

        when(repo.findQueueByLibroAndEstado(96, ReservationService.ESTADO_PENDIENTE))
                .thenReturn(List.of(r1, r2));

        when(configuracionRepository.findByClave(anyString())).thenReturn(java.util.Optional.empty());

        ReservationService service = new ReservationService(repo, mapping, client, notif, configuracionRepository);
        service.notifyNextInQueue(96);

        assertEquals(ReservationService.ESTADO_DISPONIBLE, r1.getId_estado_reserva());
        verify(repo, times(1)).save(r1);

        ArgumentCaptor<NotificationEvent> cap = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notif, times(1)).publish(cap.capture());
        NotificationEvent evt = cap.getValue();
        assertEquals("RESERVA_NOTIFICADA", evt.getType());
        Map<String, Object> payload = evt.getPayload();
        assertEquals(1, payload.get("reservaId"));
    }
}
