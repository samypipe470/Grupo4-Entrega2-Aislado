package co.edu.javeriana.prestamos.exception;

/**
 * Excepción para reglas de negocio controladas (se mapean a 4xx).
 * Las RuntimeException restantes se propagan como 500.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
