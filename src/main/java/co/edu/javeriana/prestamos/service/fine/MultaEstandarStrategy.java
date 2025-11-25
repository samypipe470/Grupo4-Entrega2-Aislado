package co.edu.javeriana.prestamos.service.fine;

import java.time.Duration;
import java.time.LocalDateTime;

public class MultaEstandarStrategy implements MultaStrategy {
    @Override
    public double calcular(LocalDateTime fechaEsperada, LocalDateTime fechaReal, double valorDiario) {
        if (fechaEsperada == null || fechaReal == null || valorDiario <= 0) {
            return 0d;
        }
        if (!fechaReal.isAfter(fechaEsperada)) {
            return 0d;
        }
        long dias = Math.max(1, Duration.between(fechaEsperada, fechaReal).toDays());
        return dias * valorDiario;
    }
}
