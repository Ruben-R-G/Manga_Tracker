package com.example.mangatracker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class Permisos {
    public static boolean TienePermisos(Context ctx, String[] permisos)
    {
        for (String permission : permisos)
        {
            if (ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    public static void PedirPermisos(Activity act, String[] permisos)
    {
        if(!TienePermisos(act, permisos))
        {
            ActivityCompat.requestPermissions(act, permisos, 1);
        }
    }
}
