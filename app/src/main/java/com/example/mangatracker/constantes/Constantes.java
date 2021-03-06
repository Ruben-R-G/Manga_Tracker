package com.example.mangatracker.constantes;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Constantes {
    public static final boolean debug = false; //true para que en lugar de ir por horas, vaya por minutos
    public static final String TAG_APP = "MangaTracker - ";
    public static final String CHANNEL_ID = "NuevosLanzamientosChannel";
    public static final String CHANNEL_ID_RETRASOS = "LanzamientosRetrasadosChannel";
    public static final String CHANNEL_ID_ADELANTOS = "LanzamientosAdelantadosChannel";
    public static final String CHANNEL_ID_HOY = "LanzamientosHoyChannel";
    public static final int[] horasNotificaciones = {9, 13, 19};
    public static final int[] minutosPruebaNotificaciones = {4, 6, 8};
    public static final String CHANNEL_ID_PRUEBA = "PruebaNuevosLanzamientosChannel";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static final SimpleDateFormat sdfSinHoras = new SimpleDateFormat("dd-MM-yyyy");
    public static final String ficheroLogs = "MangaLogs.txt";


    public static Calendar ObtenerProximaNotificacion() {
        Calendar cal = Calendar.getInstance();

        int hora = cal.get(Calendar.HOUR_OF_DAY);

        if(!debug) {
            for (int horanoti : horasNotificaciones) {
                if (hora < horanoti) {
                    cal.set(Calendar.HOUR_OF_DAY, horanoti);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    return cal;
                }
            }

            //Sale del bucle, no hay horas para este dia, añadimos un dia

            cal.add(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, horasNotificaciones[0]);

            cal.set(Calendar.MINUTE, 0);
        }else{
            //PRUEBAS

            int minuto = cal.get(Calendar.MINUTE);

            for(int minutonoti : minutosPruebaNotificaciones)
            {
                if(minuto < minutonoti)
                {
                    cal.set(Calendar.HOUR_OF_DAY, hora);
                    cal.set(Calendar.MINUTE, minutonoti);
                    cal.set(Calendar.SECOND, 0);

                    return cal;
                }
            }

            cal.add(Calendar.HOUR_OF_DAY, 1);

            cal.set(Calendar.MINUTE, minutosPruebaNotificaciones[0]);
        }

        cal.set(Calendar.SECOND, 0);

        return cal;
    }
}

