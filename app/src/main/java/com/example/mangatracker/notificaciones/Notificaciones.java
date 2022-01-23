package com.example.mangatracker.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.example.mangatracker.NuevosLanzamientosActivity;
import com.example.mangatracker.R;
import com.example.mangatracker.constantes.Constantes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.mangatracker.constantes.Constantes.CHANNEL_ID;
import static com.example.mangatracker.constantes.Constantes.CHANNEL_ID_PRUEBA;

public class Notificaciones {
    private String mensaje = "";
    private final String TAG = Constantes.TAG_APP + "NOTIF";
    private Context ctx;
    private final static AtomicInteger idNotificacion = new AtomicInteger(0);
    public Notificaciones(Context ctx) {
        this.ctx = ctx;
    }


    public void createNotification (String titulo, String texto, String channel) {
        if (!PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean("notificaciones", false)) return;

        try
        {
            Log.d(TAG, "Lanzando notificacion. Canal: "+channel);

            //Para hacer que al pulsar la notificacion, abra una actividad
            Intent intent = new Intent(ctx, NuevosLanzamientosActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

            //Builder para la notificacion
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx,
                    CHANNEL_ID)
                    //Detallamos el builder. Se puede hacer esto al generar la notificacion si
                    //se van a generar varias notificaciones distintas con el builder
                    .setSmallIcon(R.drawable.manga)
                    .setContentTitle(titulo)
                    .setContentText(texto)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(texto))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    //La intent que lleva la notificacion
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true); //Elimina la notificacion al hacer tap

            CrearCanalNotificacion(channel);

            //int id_notificacion = (int) ((Calendar.getInstance().getTimeInMillis() / 1000L) % Integer.MAX_VALUE);
            //Log.d(TAG, "ID de notificacion: "+id_notificacion);

            int id_notificacion = idNotificacion.incrementAndGet();
            Log.d(TAG, "ID de notificacion: "+id_notificacion);

            //Lanzar la notificacion
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);
            notificationManagerCompat.notify(id_notificacion,
                    notificationBuilder.build());

        }catch(Exception e)
        {
            Log.d(TAG, "ERROR:\n"+e.getMessage());
            mensaje += e.getMessage() + "\n";
        }
    }

    private void CrearCanalNotificacion(String channel) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //Solo para versiones mayores que 26
        {
            String nombre = "Canal " + channel;
            String descripcion = "Notificacion para los nuevos lanzamientos. "+channel;
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel canal = new NotificationChannel(channel, nombre, importancia);
            canal.setDescription(descripcion);

            //Registrar el canal en el sistema
            //A partir de aqui no se puede cambiar la importancia o comportamiento de la notificacion
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(canal);
        }

    }

    //region Pruebas de Notificaciones
    public void LanzamientosPrueba() {
        if (!PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean("notificaciones", false)) return;

        try
        {
            //Builder para la notificacion
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx,
                    CHANNEL_ID_PRUEBA)
                    //Detallamos el builder. Se puede hacer esto al generar la notificacion si
                    //se van a generar varias notificaciones distintas con el builder
                    .setSmallIcon(R.drawable.manga)
                    .setContentTitle("Se han buscado nuevos lanzamientos")
                    .setContentText("Se ha buscado un nuevo lanzamiento a las "+
                            new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                    .format(Calendar.getInstance().getTime()))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Se ha buscado un nuevo lanzamiento a las "+
                                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                            .format(Calendar.getInstance().getTime())
                                    + (mensaje.equals("") ? "" : "\nErrores: \n"+mensaje)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true); //Elimina la notificacion al hacer tap

            CrearCanalNotificacionPrueba();


            //Lanzar la notificacion
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);
            notificationManagerCompat.notify((int) (((new Date().getTime() / 1000L) % Integer.MAX_VALUE)+23),
                    notificationBuilder.build());

        }catch(Exception e)
        {
            Log.d(TAG, "ERROR:\n"+e.getMessage());
        }
    }

    private void CrearCanalNotificacionPrueba() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //Solo para versiones mayores que 26
        {
            String nombre = "Canal notificacion prueba";
            String descripcion = "Notificacion para la prueba de los nuevos lanzamientos";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel canal = new NotificationChannel(Constantes.CHANNEL_ID_PRUEBA, nombre, importancia);
            canal.setDescription(descripcion);

            //Registrar el canal en el sistema
            //A partir de aqui no se puede cambiar la importancia o comportamiento de la notificacion
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(canal);
        }
    }
    //endregion
}
