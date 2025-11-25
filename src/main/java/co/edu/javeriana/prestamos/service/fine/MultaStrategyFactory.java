package co.edu.javeriana.prestamos.service.fine;

import org.springframework.stereotype.Component;

@Component
public class MultaStrategyFactory {

    public MultaStrategy forUserType(Integer userType) {
        if (userType != null && userType == 3) {
            return new MultaPerdonStrategy();
        }
        return new MultaEstandarStrategy();
    }
}
