package co.edu.javeriana.prestamos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_reserva;
    private Integer id_usuario;
    private Integer id_libro;
    private LocalDateTime fecha_reserva;
    private Integer id_estado_reserva; // 1 = PENDIENTE, 2 = NOTIFICADO, 3 = CANCELADA, 4 = EXPIRADA

    public Reserva() {
    }

    public Reserva(Integer id, Integer idUsuario, Integer idLibro, Integer estado) {
        this.id_reserva = id;
        this.id_usuario = idUsuario;
        this.id_libro = idLibro;
        this.id_estado_reserva = estado;
        this.fecha_reserva = LocalDateTime.now();
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

