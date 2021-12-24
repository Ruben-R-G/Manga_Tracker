package com.example.mangatracker.clases;

import com.example.mangatracker.Excepciones.NumeroMangasException;

import java.io.Serializable;

public class Manga implements Serializable {
    private int id;
    private String nombre;
    private int tomosEditados;
    private int tomosEnPreparacion;
    private int tomosNoEditados;
    private int tomosComprados;
    private String fecha;
    private int terminado;
    private int fav;
    private int drop;


    public Manga(int id, String nombre, int tomosEditados, int tomosEnPreparacion,
                 int tomosNoEditados, int tomosComprados, String fecha, int terminado, int fav, int drop) {
        this.id = id;
        this.nombre = nombre;
        this.tomosEditados = tomosEditados;
        this.tomosEnPreparacion = tomosEnPreparacion;
        this.tomosNoEditados = tomosNoEditados;
        this.tomosComprados = tomosComprados;
        this.fecha = fecha;
        this.terminado = terminado;
        this.fav = fav;
        this.drop = drop;
    }

    public Manga(int id, String nombre, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
    }

    public Manga(int id, String nombre, String fecha, int tomosComprados) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.tomosComprados = tomosComprados;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getFav() {
        return fav;
    }

    public void setFav(int fav) {
        this.fav = fav;
    }

    public int getDrop() {
        return drop;
    }

    public void setDrop(int drop) {
        this.drop = drop;
    }

    public int getTerminado() {
        return terminado;
    }

    public void setTerminado(int terminado) {
        this.terminado = terminado;
    }

    public Manga(String nombre, int tomosEditados, int tomosEnPreparacion, int tomosNoEditados) {
        this.nombre = nombre;
        this.tomosEditados = tomosEditados;
        this.tomosEnPreparacion = tomosEnPreparacion;
        this.tomosNoEditados = tomosNoEditados;
        tomosComprados = -1;
    }

    public Manga(String nombre, int tomosEditados, int tomosEnPreparacion, int tomosNoEditados, int tomosComprados) {
        this.nombre = nombre;
        this.tomosEditados = tomosEditados;
        this.tomosEnPreparacion = tomosEnPreparacion;
        this.tomosNoEditados = tomosNoEditados;
        this.tomosComprados = tomosComprados;
    }

    public Manga() {
    }

    public Manga(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTomosEditados() {
        return tomosEditados;
    }

    public void setTomosEditados(int tomosEditados) {
        this.tomosEditados = tomosEditados;
    }

    public int getTomosEnPreparacion() {
        return tomosEnPreparacion;
    }

    public void setTomosEnPreparacion(int tomosEnPreparacion) {
        this.tomosEnPreparacion = tomosEnPreparacion;
    }

    public int getTomosNoEditados() {
        return tomosNoEditados;
    }

    public void setTomosNoEditados(int tomosNoEditados) {
        this.tomosNoEditados = tomosNoEditados;
    }

    public int getTomosComprados() {
        return tomosComprados;
    }

    //todo controlar cuando los tomos comprados sean menor que 0
    public void setTomosComprados(int tomosComprados) throws NumeroMangasException {
        if(tomosComprados > tomosEditados)
        {
            throw new NumeroMangasException(
                    "El número de mangas comprados no puede ser superior al número de mangas editados");
        }
        this.tomosComprados = tomosComprados;

    }

    public void SumaRestaUno(boolean Suma) throws NumeroMangasException {
        setTomosComprados(getTomosComprados() + (Suma ? 1 : -1));
    }
}
