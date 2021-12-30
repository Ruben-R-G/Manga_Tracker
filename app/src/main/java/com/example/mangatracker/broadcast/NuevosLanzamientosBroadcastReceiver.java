package com.example.mangatracker.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.servicios.BuscarNuevosLanzamientos;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NuevosLanzamientosBroadcastReceiver extends BroadcastReceiver {
    private String TAG = Constantes.TAG_APP + "NuevosLanzamientosBroadcastReceiver";

    //Inicia el servicio para actualizar mangas y enviar notificaciones
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Calendar cal = Constantes.ObtenerProximaNotificacion();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                IniciarServicio(context);
            }
            return;
        }
        context.startService(new Intent(context, BuscarNuevosLanzamientos.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void IniciarServicio(Context context) { //Genera la alarma que iniciará el servicio
        Log.d(TAG, "Lanzando el servicio...");

        Calendar proximaNotificacion = Constantes.ObtenerProximaNotificacion();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Log.d(TAG, "Proxima notificacion: "+ sdf.format(proximaNotificacion.getTime()));

        //En lugar de destruirse, llamo de nuevo al BroadcastReceiver en x
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,
                new Intent(context, NuevosLanzamientosBroadcastReceiver.class)
                        .addFlags(Intent.FLAG_RECEIVER_FOREGROUND),
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);
        }else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);
    }
}
