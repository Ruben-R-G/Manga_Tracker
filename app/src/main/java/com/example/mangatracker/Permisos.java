package com.example.mangatracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.security.Provider;

public class Permisos {

    public static void PedirPermisos(Activity act, String[] permisos)
    {
        if(!TienePermisos(act, permisos))
        {
            ActivityCompat.requestPermissions(act, permisos, 1);
        }
    }

    public static boolean TienePermisos(Context ctx, String[] permisos)
    {
        for (String permission : permisos)
        {
            if (ActivityCompat.checkSelfPermission(ctx, permission)
                    != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void GestionarOptimizacionBateria(Activity act)
    {
        PowerManager pm = (PowerManager)act.getApplicationContext().getSystemService(Context.POWER_SERVICE);

        if(!pm.isIgnoringBatteryOptimizations(act.getApplicationContext().getPackageName()))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setMessage("Se está optimizando la bateria para esta app" +
                    "y puede conllevar que el servicio no funcione correctamente." +
                    "\n¿Quieres desactivar la optimización de batería?")
                    .setPositiveButton("Si", (dialog, which) -> {
                        act.startActivity(
                                new Intent()
                                        .setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                    })
                    .setNegativeButton("No", ((dialog, which) -> {}));

            builder.create().show();
        }
    }

}
