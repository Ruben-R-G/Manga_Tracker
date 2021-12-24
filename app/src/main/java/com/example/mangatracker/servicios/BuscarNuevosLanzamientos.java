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
    private final String TAG = Constantes.TAG_APP + "BNL";
    private String mensaje = "";
    @Override
    public IBinder onBind (Intent arg0) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate () {
        try {
            AddedMangasDB.InstaciarBD(this);
            Log.d(TAG, "onCreate");
            AddedMangasDB.ObtenerNuevosLanzamientos(true); //Se quedan a null las fechas que ya han pasado
            NuevosLanzamientos();
            LanzamientosPrueba();
        }catch(Exception e)
        {
            Log.e(TAG, "EXCEPCION NO CONTROLADA: \n"+e.getMessage());
            mensaje += e.getMessage() + "\n";
        }
        CrearAlarma();
        this.stopSelf(); //Llama al onDestroy
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CrearAlarma() {
        if(!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("notificaciones", false)) return;


        Calendar proximaNotificacion = Constantes.ObtenerProximaNotificacion();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Log.d(TAG, "Proxima notificacion: "+ sdf.format(proximaNotificacion.getTime()));
        //En lugar de destruirse, llamo de nuevo al BroadcastReceiver en x
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                new Intent(this, NuevosLanzamientosBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);
        }else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy () {
        super.onDestroy() ;
        Log.d(TAG ,"onDestroy" ) ;
    }


    public void NuevosLanzamientos()
    {
        //                    PruebaNotificacion();

        if(!PreferenceManager.getDefaultSharedPreferences(this)
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
        if(ms.length == 0) return;

        Log.d(TAG, "Hay "+ ms.length  +" mangas sin proxima fecha y sin finalizar");

        //Comprobacion de los nuevos lanzamientos (se actualizan también otros datos)
        String [] mangasNotif = new String[2];

        Boolean MultiplesLanzamientos = false;
        int TotalNuevosLanzamientos = 0;
        for(Manga m : ms)
        {
            Manga NuevoLanz = new Manga(-1, "Nada nuevo");

            NuevoLanz.setId(m.getId());
            try {
                //Si nada se ha roto, tambien actualizo los datos del manga
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

                if(nuevosDatos.getFecha() == null) continue;

                NuevoLanz.setFecha(new SimpleDateFormat("dd-MM-yyyy")
                        .format(nuevosDatos.getFecha()));
                AddedMangasDB.ActualizarFecha(NuevoLanz.getId(), NuevoLanz.getFecha());

                TotalNuevosLanzamientos++;

                if(TotalNuevosLanzamientos > 1) {
                    MultiplesLanzamientos = true;
                    continue;
                }

                mangasNotif[0] = NuevoLanz.getNombre();
                mangasNotif[1] = NuevoLanz.getFecha();

            } catch (IOException e) {
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            } catch (ParseException e) {
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            } catch (NoSuchMethodError e){
                Log.e(TAG, "Joder");
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            } catch (Exception e){
                e.printStackTrace();
                mensaje += e.getMessage() + "\n";
            }
        }
        if(TotalNuevosLanzamientos != 0)
            createNotification(MultiplesLanzamientos, mangasNotif[0], mangasNotif[1]);

    }

    private void createNotification (Boolean multiplesLanzamientos,
                                     String nombre, String fecha) {
        try
        {
            //Para hacer que al pulsar la notificacion, abra una actividad
            Intent intent = new Intent(this, NuevosLanzamientosActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            //Builder para la notificacion
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    CHANNEL_ID)
                    //Detallamos el builder. Se puede hacer esto al generar la notificacion si
                    //se van a generar varias notificaciones distintas con el builder
                    .setSmallIcon(R.drawable.manga)
                    .setContentTitle(multiplesLanzamientos ? "¡Nuevos lanzamientos!" : "Nuevo lanzamiento: "+nombre)
                    .setContentText(multiplesLanzamientos ? "Comprueba los nuevos lanzamientos" :
                            "Nuevo lanzamiento de "+nombre+" el día "+fecha)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(multiplesLanzamientos ? "Comprueba los nuevos lanzamientos" :
                                    "Nuevo lanzamiento de "+nombre+" el día "+fecha))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    //La intent que lleva la notificacion
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true); //Elimina la notificacion al hacer tap

            CrearCanalNotificacion();

            int id_notificacion = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            //Lanzar la notificacion
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(id_notificacion,
                    notificationBuilder.build());

        }catch(Exception e)
        {
            Log.d(TAG, "ERROR:\n"+e.getMessage());
            mensaje += e.getMessage() + "\n";
        }
    }

    private void CrearCanalNotificacion() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //Solo para versiones mayores que 26
        {
            String nombre = "Canal notificacion";
            String descripcion = "Notificacion para los nuevos lanzamientos";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel canal = new NotificationChannel(Constantes.CHANNEL_ID, nombre, importancia);
            canal.setDescription(descripcion);

            //Registrar el canal en el sistema
            //A partir de aqui no se puede cambiar la importancia o comportamiento de la notificacion
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(canal);
        }

    }

    private void LanzamientosPrueba() {
        try
        {
            //Builder para la notificacion
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    CHANNEL_ID)
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
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
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
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(canal);
        }
    }

    /*
    private void PruebaNotificacion()
    {
if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //Solo para versiones mayores que 26
        {
            String nombre = "Canal notificacion";
            String descripcion = "Prueba de canal de notificacion";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel canal = new NotificationChannel(CHANNEL_ID, nombre, importancia);
            canal.setDescription(descripcion);

            //Registrar el canal en el sistema
            //A partir de aqui no se puede cambiar la importancia o comportamiento de la notificacion
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(canal);
        }    }
*/


}


