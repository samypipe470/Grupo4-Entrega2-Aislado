package co.edu.javeriana.prestamos.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailSimuladoObserver implements NotificationObserver {
    private static final Logger log = LoggerFactory.getLogger(EmailSimuladoObserver.class);

    @Override
    public void onEvent(NotificationEvent event) {
        String type = event.getType();
        if ("RESERVA_NOTIFICADA".equalsIgnoreCase(type)
                || "PRESTAMO_CONFIRMADO".equalsIgnoreCase(type)
                || "PRESTAMO_POR_VENCER".equalsIgnoreCase(type)
                || "MULTA_GENERADA".equalsIgnoreCase(type)) {
            log.info("[EMAIL] Enviando correo simulado ({}) -> {}", type, event.getPayload());
        }
    }
}
