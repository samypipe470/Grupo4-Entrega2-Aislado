package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.model.Reserva;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Integer id_reserva;
    private Integer id_usuario;
    private Integer id_libro;
    private LocalDateTime fecha_reserva;
    private Integer id_estado_reserva;

    public ReservationResponse(Reserva r) {
        this.id_reserva = r.getId_reserva();
        this.id_usuario = r.getId_usuario();
        this.id_libro = r.getId_libro();
        this.fecha_reserva = r.getFecha_reserva();
        this.id_estado_reserva = r.getId_estado_reserva();
    }

    public Integer getId_reserva() {
        return id_reserva;
    }

    public void setId_reserva(Integer id_reserva) {
        this.id_reserva = id_reserva;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Integer getId_libro() {
        return id_libro;
    }

    public void setId_libro(Integer id_libro) {
        this.id_libro = id_libro;
    }

    public LocalDateTime getFecha_reserva() {
        return fecha_reserva;
    }

    public void setFecha_reserva(LocalDateTime fecha_reserva) {
        this.fecha_reserva = fecha_reserva;
    }

    public Integer getId_estado_reserva() {
        return id_estado_reserva;
    }

    public void setId_estado_reserva(Integer id_estado_reserva) {
        this.id_estado_reserva = id_estado_reserva;
    }
}

