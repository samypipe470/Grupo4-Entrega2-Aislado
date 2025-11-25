package co.edu.javeriana.prestamos.service.fine;

import java.time.LocalDateTime;

public interface MultaStrategy {
    double calcular(LocalDateTime fechaEsperada, LocalDateTime fechaReal, double valorDiario);
}
