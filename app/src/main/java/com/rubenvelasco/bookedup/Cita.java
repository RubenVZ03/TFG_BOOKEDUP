package com.rubenvelasco.bookedup;

import java.util.Date;

public class Cita {
    private String dia;
    private String hora;
    private String mes;
    private String profesional;
    private String servicio;
    private String ubicacion;
    private String nombre;
    private Date timestamp;
    private String estado;

    public Cita() {
    }

    public Cita(String dia, String hora, String mes, String profesional, String servicio, String ubicacion, String nombre, Date timestamp, String estado) {
        this.dia = dia;
        this.hora = hora;
        this.mes = mes;
        this.profesional = profesional;
        this.servicio = servicio;
        this.ubicacion = ubicacion;
        this.nombre = nombre;
        this.timestamp = timestamp;
        this.estado = estado;
    }

    public String getDia() {
        return dia;
    }

    public String getHora() {
        return hora;
    }

    public String getMes() {
        return mes;
    }

    public String getProfesional() {
        return profesional;
    }

    public String getServicio() {
        return servicio;
    }

    public String getNombre() {
        return nombre;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getEstado() {
        return estado;
    }
}
