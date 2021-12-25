package com.example.mangatracker.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.servicios.BuscarNuevosLanzamientos;

public class NuevosLanzamientosBroadcastReceiver extends BroadcastReceiver {
    private String TAG = Constantes.TAG_APP + "NuevosLanzamientosBroadcastReceiver";

    //Inicia el servicio para actualizar mangas y enviar notificaciones
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        context.startService(new Intent(context, BuscarNuevosLanzamientos.class));
    }
}
