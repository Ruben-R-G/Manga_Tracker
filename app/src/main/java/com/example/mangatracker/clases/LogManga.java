package com.example.mangatracker.clases;

public class LogManga {
    private String paso;
    private String codigo_tramo;
    private String fecha;
    private String evento;

    public LogManga(String paso, String codigo_tramo, String fecha, String evento) {
        this.paso = paso;
        this.codigo_tramo = codigo_tramo;
        this.fecha = fecha;
        this.evento = evento;
    }

    public String getPaso() {
        return paso;
    }

    public void setPaso(String paso) {
        this.paso = paso;
    }

    public String getCodigo_tramo() {
        return codigo_tramo;
    }

    public void setCodigo_tramo(String codigo_tramo) {
        this.codigo_tramo = codigo_tramo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    @Override
    public String toString() {
        return "\n{" +
                "\"paso\"=\"" + paso + "\"," +
                "\"codigo_tramo=\"" + codigo_tramo + "\"," +
                "\"fecha=\"" + fecha + "\"," +
                "\"evento=\"" + evento + "\"" +
                "},";
    }

    public String StringFichero()
    {
        return fecha + " - PASO " + paso + " - Codigo Tramo: "
                + codigo_tramo + ": " + evento + "\n\n";
    }
}
