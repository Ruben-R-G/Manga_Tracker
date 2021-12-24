package com.example.mangatracker.constantes;

import java.util.Calendar;

public class Constantes {
    public static final boolean debug = false;
    public static final String TAG_APP = "MangaTracker - ";
    public static final String CHANNEL_ID = "NuevosLanzamientosChannel";
    public static final int[] horasNotificaciones = {9, 13, 19};
    public static final int[] minutosPruebaNotificaciones = {4, 6, 8};
    public static final String CHANNEL_ID_PRUEBA = "PruebaNuevosLanzamientosChannel";

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

            //Sale del bucle, no hay horas ara este dia, añadimos un dia

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

