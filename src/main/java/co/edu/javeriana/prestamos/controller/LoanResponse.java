package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.model.Prestamo;

// Define el JSON de respuesta
public class LoanResponse {
    private Integer id_prestamo;
    private Integer id_libro;
    private Integer id_usuario;
    private String estado; // TRADUCIDO A STRING
    private Double valor_multa;

    public LoanResponse(Prestamo p) {
        this.id_prestamo = p.getId_prestamo();
        this.id_libro = p.getId_libro();
        this.id_usuario = p.getId_usuario();
        this.valor_multa = p.getValor_multa();

        switch (p.getId_estado_prestamo()) {
            case 1:
                this.estado = "SOLICITADO";
                break;
            case 2:
                this.estado = "ACTIVO";
                break;
            case 3:
                this.estado = "DEVUELTO";
                break;
            case 4:
                this.estado = "VENCIDO";
                break;
            default:
                this.estado = "DESCONOCIDO";
                break;
        }
    }

    public Integer getId_prestamo() {
        return id_prestamo;
    }

    public void setId_prestamo(Integer id_prestamo) {
        this.id_prestamo = id_prestamo;
    }

    public Integer getId_libro() {
        return id_libro;
    }

    public void setId_libro(Integer id_libro) {
        this.id_libro = id_libro;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getValor_multa() {
        return valor_multa;
    }

    public void setValor_multa(Double valor_multa) {
        this.valor_multa = valor_multa;
    }
}
