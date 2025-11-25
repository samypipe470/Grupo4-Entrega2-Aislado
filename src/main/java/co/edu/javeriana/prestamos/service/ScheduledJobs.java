package co.edu.javeriana.prestamos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobs {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobs.class);

    private final LoanService loanService;
    private final ReservationService reservationService;

    public ScheduledJobs(LoanService loanService, ReservationService reservationService) {
        this.loanService = loanService;
        this.reservationService = reservationService;
    }

    /*// CAMBIO 1: Cron cada 10 segundos
    @Scheduled(cron = "*//*10 * * * * *")
    public void marcarVencidos() {
        loanService.marcarPrestamosVencidosProgramado();
        // CAMBIO 2: Usar info en vez de debug para que salga seguro
        log.info("Job marcarVencidos ejecutado - Buscando préstamos vencidos...");
    }*/

    @Scheduled(cron = "0 0 0 * * *")
    public void marcarVencidos() {
        loanService.marcarPrestamosVencidosProgramado();
        log.debug("Job marcarVencidos ejecutado");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expirarReservas() {
        reservationService.expireStaleReservationsProgramado();
        log.debug("Job expirarReservas ejecutado");
    }

    @Scheduled(cron = "0 30 8 * * *")
    public void recordatoriosVencimiento() {
        loanService.notificarVencimientosProximos(3);
        log.debug("Job recordatoriosVencimiento ejecutado");
    }

    /*@Scheduled(cron = "*//*10 * * * * *")
    public void recordatoriosVencimiento() {
        // El 3 es clave: busca préstamos que vencen en 3 días
        loanService.notificarVencimientosProximos(3);
        // Usa INFO para verlo seguro en la consola
        log.info("Job recordatorios ejecutado - Buscando próximos a vencer...");
    }*/

}
