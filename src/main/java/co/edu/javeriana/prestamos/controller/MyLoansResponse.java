package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.model.Prestamo;

import java.util.List;
import java.util.stream.Collectors;

// Define el JSON: { "prestamos_activos": [...], "historial": [...] }
public class MyLoansResponse {
    private List<LoanResponse> prestamos_activos;
    private List<LoanResponse> historial;

    // Constructor que clasifica los préstamos
    public MyLoansResponse(List<Prestamo> prestamos) {
        this.prestamos_activos = prestamos.stream()
                .filter(p -> {
                    int estado = p.getId_estado_prestamo();
                    return estado == 1 || estado == 2 || estado == 4; // Incluye SOLICITADO, ACTIVO y VENCIDO
                })
                .map(LoanResponse::new)
                .collect(Collectors.toList());

        this.historial = prestamos.stream()
                .filter(p -> p.getId_estado_prestamo() == 3) // Solo Devueltos
                .map(LoanResponse::new)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getPrestamos_activos() {
        return prestamos_activos;
    }

    public void setPrestamos_activos(List<LoanResponse> prestamos_activos) {
        this.prestamos_activos = prestamos_activos;
    }

    public List<LoanResponse> getHistorial() {
        return historial;
    }

    public void setHistorial(List<LoanResponse> historial) {
        this.historial = historial;
    }
}

