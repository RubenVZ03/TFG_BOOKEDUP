package com.rubenvelasco.bookedup;

import java.util.ArrayList;

public class Servicio {
    private String id;
    private String nombre;
    private String descripcion;
    private String horario;
    private String imageUrl;
    private String categoria;
    private ArrayList<String> dias;
    private ArrayList<String> ofertas;
    private String profesional;
    private String ubicacion;
    private double valoracion;
    private boolean oferta;

    public Servicio() {
    }

    public Servicio(String nombre, String descripcion, String horario, String imageUrl, String categoria, ArrayList<String> dias, String profesional, String ubicacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.horario = horario;
        this.imageUrl = imageUrl;
        this.categoria = categoria;
        this.dias = dias;
        this.profesional = profesional;
        this.ubicacion = ubicacion;
    }

    // Constructor completo con ofertas
    public Servicio(String nombre, String descripcion, String horario, String imageUrl, String categoria, ArrayList<String> dias, String profesional, String ubicacion, ArrayList<String> ofertas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.horario = horario;
        this.imageUrl = imageUrl;
        this.categoria = categoria;
        this.dias = dias;
        this.profesional = profesional;
        this.ubicacion = ubicacion;
        this.ofertas = ofertas;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getHorario() {
        return horario;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategoria() {
        return categoria;
    }

    public ArrayList<String> getDias() {
        return dias;
    }

    public String getProfesional() {
        return profesional;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public double getValoracion() {
        return valoracion;
    }

    public ArrayList<String> getOfertas() {
        return ofertas;
    }

    public void setId(String id) {
    }

    public String getId() {
        return id;
    }
}
