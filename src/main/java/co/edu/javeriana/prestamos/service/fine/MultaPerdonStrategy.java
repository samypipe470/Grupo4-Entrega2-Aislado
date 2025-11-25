package co.edu.javeriana.prestamos.service.fine;

import java.time.LocalDateTime;

public class MultaPerdonStrategy implements MultaStrategy {
    @Override
    public double calcular(LocalDateTime fechaEsperada, LocalDateTime fechaReal, double valorDiario) {
        return 0d;
    }
}
