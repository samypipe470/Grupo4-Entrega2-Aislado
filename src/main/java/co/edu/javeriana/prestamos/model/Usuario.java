package co.edu.javeriana.prestamos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    private Integer id_usuario;
    private String username;
    private String nombre;
    private String contrasena;
    private Integer id_tipo_usuario;
    private Integer id_estado_usuario;
    private int intentos_fallidos;

    public Usuario() {
    }

    // Constructor simulado (para pruebas)
    public Usuario(Integer id, String username, String nombre, int tipo) {
        this.id_usuario = id;
        this.username = username;
        this.nombre = nombre;
        this.id_tipo_usuario = tipo;
        this.id_estado_usuario = 1; // 1 = "activo"
        this.intentos_fallidos = 0;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Integer getId_tipo_usuario() {
        return id_tipo_usuario;
    }

    public void setId_tipo_usuario(Integer id_tipo_usuario) {
        this.id_tipo_usuario = id_tipo_usuario;
    }

    public Integer getId_estado_usuario() {
        return id_estado_usuario;
    }

    public void setId_estado_usuario(Integer id_estado_usuario) {
        this.id_estado_usuario = id_estado_usuario;
    }

    public int getIntentos_fallidos() {
        return intentos_fallidos;
    }

    public void setIntentos_fallidos(int intentos_fallidos) {
        this.intentos_fallidos = intentos_fallidos;
    }
}

