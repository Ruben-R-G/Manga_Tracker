package com.example.mangatracker.clases;

import com.example.mangatracker.Excepciones.NumeroMangasException;

import java.io.Serializable;
import java.util.Date;

public class Manga implements Serializable {
    private int id;
    private String nombre;
    private int tomosEditados;
    private int tomosEnPreparacion;
    private int tomosNoEditados;
    private int tomosComprados;
    private Date fecha;
    private boolean terminado;
    private boolean fav;
    private boolean drop;


    public Manga(int id, String nombre, int tomosEditados, int tomosEnPreparacion,
                 int tomosNoEditados, int tomosComprados, Date fecha,
                 boolean terminado, boolean fav, boolean drop) {
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

    public Manga(int id, String nombre, Date fecha) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
    }

    public Manga(int id, String nombre, Date fecha, int tomosComprados) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.tomosComprados = tomosComprados;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public boolean getFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public boolean getDrop() {
        return drop;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public boolean getTerminado() {
        return terminado;
    }

    public void setTerminado(boolean terminado) {
        this.terminado = terminado;
    }

    public Manga(String nombre, int tomosEditados, int tomosEnPreparacion,
                 int tomosNoEditados) {
        this.nombre = nombre;
        this.tomosEditados = tomosEditados;
        this.tomosEnPreparacion = tomosEnPreparacion;
        this.tomosNoEditados = tomosNoEditados;
        tomosComprados = -1;
    }

    public Manga(String nombre, int tomosEditados, int tomosEnPreparacion,
                 int tomosNoEditados, int tomosComprados) {
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

    public void setTomosComprados(int tomosComprados) throws NumeroMangasException {
        if(tomosComprados > tomosEditados)
        {
            throw new NumeroMangasException(
                    String.format("El número de mangas comprados (%d) no puede ser superior " +
                            "al número de mangas editados (%d)", tomosComprados, tomosEditados));
        }
        this.tomosComprados = tomosComprados;

    }

    public void SumaRestaUno(boolean Suma) throws NumeroMangasException {
        setTomosComprados(getTomosComprados() + (Suma ? 1 : -1));
    }

    @Override
    public String toString() {
        return
                "{\"id\":" + id +
                ", \"nombre\":\"" + nombre + '\"' +
                ", \"tomosEditados\":" + tomosEditados +
                ", \"tomosEnPreparacion\":" + tomosEnPreparacion +
                ", \"tomosNoEditados\":" + tomosNoEditados +
                ", \"tomosComprados\":" + tomosComprados +
                ", \"fecha\":" + (fecha == null ? "null" : "\"" + fecha + "\"") +
                ", \"terminado\":" + terminado +
                ", \"fav\":" + fav +
                ", \"drop\":" + drop +
                "},";
    }
}
