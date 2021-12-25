package com.example.mangatracker.servicios;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.example.mangatracker.NuevosLanzamientosActivity;
import com.example.mangatracker.R;
import com.example.mangatracker.broadcast.NuevosLanzamientosBroadcastReceiver;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.db.AddedMangasDB;
import com.example.mangatracker.notificaciones.Notificaciones;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import clases.MangaDatos;
import operaciones.MangaScrapper;

import static com.example.mangatracker.constantes.Constantes.CHANNEL_ID;

public class BuscarNuevosLanzamientos extends Service {
    private String mensaje = "";
    private final String TAG = Constantes.TAG_APP + "BNL";
    private Notificaciones notificaciones;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        try {
            notificaciones = new Notificaciones(this);
            AddedMangasDB.InstaciarBD(this);
            Log.d(TAG, "onCreate");
            AddedMangasDB.ObtenerNuevosLanzamientos(true); //Se quedan a null las fechas que ya han pasado
            NuevosLanzamientos();
            notificaciones.LanzamientosPrueba();
        } catch (Exception e) {
            Log.e(TAG, "EXCEPCION NO CONTROLADA: \n" + e.getMessage());
            mensaje += e.getMessage() + "\n";
        }
        CrearAlarma();
        this.stopSelf(); //Llama al onDestroy
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CrearAlarma() {
        if (!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("notificaciones", false)) return;


        Calendar proximaNotificacion = Constantes.ObtenerProximaNotificacion();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Log.d(TAG, "Proxima notificacion: " + sdf.format(proximaNotificacion.getTime()));

        //En lugar de destruirse, llamo de nuevo al BroadcastReceiver en x
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                new Intent(this, NuevosLanzamientosBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);
        } else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    public void NuevosLanzamientos() {
        if (!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("notificaciones", false)) return;

        /*
         * todo usar hilos para llamadas a listadomanga
         *  Quitar las 3 lineas de strictmode si se usan hilos o async
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //AddedMangasDB.InstaciarBD(this);
        Manga[] ms = AddedMangasDB.ActualizarNuevosLanzamientos();

        if (ms.length == 0) return; //No hay mangas sin fecha, no actualizo nada

        Log.d(TAG, "Hay " + ms.length + " mangas sin proxima fecha y sin finalizar");

        //Comprobacion de los nuevos lanzamientos (se actualizan también otros datos)
        String[] mangasNotif = new String[2];

        Boolean MultiplesLanzamientos = false;
        int TotalNuevosLanzamientos = 0;

        for (Manga m : ms) {
            Manga NuevoLanz = new Manga(-1, "Nada nuevo");

            NuevoLanz.setId(m.getId());
            try {
                MangaDatos nuevosDatos = MangaScrapper.ObtenerDatosDe(m.getId());

                NuevoLanz.setNombre(m.getNombre());

                //Obtengo los datos actualizados y tambien los inserto
                NuevoLanz.setTomosEditados(nuevosDatos.getTomosEditados());
                NuevoLanz.setTomosEnPreparacion(nuevosDatos.getTomosEnPreparacion());
                NuevoLanz.setTomosNoEditados(nuevosDatos.getTomosNoEditados());
                NuevoLanz.setTerminado(nuevosDatos.getTerminado());

                //Mantengo los tomos comprados (al hacer la insercion en sql,
                // tambien se actualizan los tomos comprados)
                NuevoLanz.setTomosComprados(m.getTomosComprados());

                AddedMangasDB.ActualizarManga(NuevoLanz);

                //Si no se obtiene fecha, no cuenta como nuevo lanzamiento
                if (nuevosDatos.getFecha() == null) continue;

                NuevoLanz.setFecha(new SimpleDateFormat("dd-MM-yyyy")
                        .format(nuevosDatos.getFecha()));
                AddedMangasDB.ActualizarFecha(NuevoLanz.getId(), NuevoLanz.getFecha());

                TotalNuevosLanzamientos++;

                if (TotalNuevosLanzamientos > 1) {
                    MultiplesLanzamientos = true;
                    continue;
                }

                //Datos del manga que se mostrará en la notificación si es el único
                mangasNotif[0] = NuevoLanz.getNombre();
                mangasNotif[1] = NuevoLanz.getFecha();

            } catch (IOException e) {
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            } catch (NoSuchMethodError e) {
                Log.e(TAG, "Joder");
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            } catch (Exception e) {
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            }
        }
        if (TotalNuevosLanzamientos != 0)
            notificaciones.createNotification(MultiplesLanzamientos, mangasNotif[0], mangasNotif[1]);

    }

}


