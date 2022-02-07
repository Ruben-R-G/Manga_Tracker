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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import clases.MangaDatos;
import operaciones.MangaScrapper;

import static com.example.mangatracker.constantes.Constantes.CHANNEL_ID;
import static com.example.mangatracker.constantes.Constantes.sdf;
import static com.example.mangatracker.constantes.Constantes.sdfSinHoras;
import static java.lang.Thread.sleep;

public class BuscarNuevosLanzamientos extends Service {
    private String mensaje = "";
    private final String TAG = Constantes.TAG_APP + "BNL";
    private Notificaciones notificaciones;

    //Comprobar cada semana que los mangas con fecha no se hayan adelantado
    private Manga[] mangasComprobacion;

    //Comprobar los mangas que se lancen ese día para notificarlo
    private Manga[] mangasLanzadosHoy;

    //Comprobacion de los nuevos lanzamientos (se actualizan también otros datos)
    private String[] mangasNotif = new String[2];

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        new Thread(() -> {
            try {
                notificaciones = new Notificaciones(this);
                AddedMangasDB.InstaciarBD(this);
                Log.d(TAG, "onCreate");

                //Obtenemos los lanzamientos que se lanzan ese día para mostrar la notificacion
                ComprobarMangasLanzadosHoy();

                //Se quedan a null las fechas que ya han pasado
                AddedMangasDB.ObtenerNuevosLanzamientos(true);

                ComprobarMangasConFecha();

                FutureTask<Integer> lanzamientos = new FutureTask<>(NuevosLanzamientos());
                ExecutorService exec = Executors.newCachedThreadPool();
                exec.submit(lanzamientos);

                Log.d(TAG, "Esperando al FutureTask");

                while(!lanzamientos.isDone())
                {
                }
                Integer result = lanzamientos.get();
                exec.shutdown();

                notificacionResultadosNuevosLanzamientos(result);

                //Para los lanzamientos próximos
                //notificacionProximosLanzamientos();

                //Para los lanzamientos adelantados
                if(mangasComprobacion != null)
                {
                    notificacionLanzamientosAdelantados();
                    notificacionLanzamientosAtrasados();
                }

                //Para los lanzamiento que se lanzan ese dia
                notificacionLanzamientosHoy();

                notificaciones.LanzamientosPrueba();
            } catch (Exception e) {
                Log.e(TAG, "EXCEPCION NO CONTROLADA: \n" + e.getMessage());
                mensaje += e.getMessage() + "\n";
            }
            CrearAlarma();
            this.stopSelf(); //Llama al onDestroy
        }).start();
    }



    //region Comprobaciones
    private void ComprobarMangasLanzadosHoy() {
        mangasLanzadosHoy = Arrays.stream(AddedMangasDB.ObtenerNuevosLanzamientos(false))
                .filter(m -> {
                    Date FechaManga;
                    try {
                            FechaManga = sdfSinHoras.parse(m.getFecha());
                            Calendar hoy = Calendar.getInstance();
                            //hoy.add(Calendar.DAY_OF_YEAR, -1);

                            return FechaManga.before(hoy.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                }).toArray(Manga[]::new);
    }

    private void ComprobarMangasConFecha() {

        if(//Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            //    == Calendar.SUNDAY
            //    &&
                Constantes.ObtenerProximaNotificacion()
                        .get(Calendar.HOUR_OF_DAY)
                        == Constantes.horasNotificaciones[0]
        ) //Mangas adelantados
        {
            Log.d(TAG, "Comprobamos todos los mangas");
            mangasComprobacion = AddedMangasDB.ObtenerNuevosLanzamientos(false);
            for (Manga m : mangasComprobacion) {
                AddedMangasDB.ActualizarFecha(m.getId(), null);
            }
        }
    }

    //endregion

    //region Notificaciones

    private void notificacionLanzamientosHoy() {
        if(mangasLanzadosHoy.length == 0) return;

        String texto = "Hoy se lanza: \n"+
                (Arrays.stream(mangasLanzadosHoy).map(Manga::getNombre)
                        .reduce("",
                                (ant, prox) -> String.format("%s%s, ", ant, prox)));

        notificaciones.createNotification(
                (mangasLanzadosHoy.length +
                        (mangasLanzadosHoy.length == 1 ? " manga sale" : " mangas salen")+ " hoy!"),
                texto.substring(0, texto.length()-2), //-2 para quitar la , y espacio
                Constantes.CHANNEL_ID_HOY
                );
    }

    private void notificacionLanzamientosAdelantados() {
        Manga[] mangasAdelantados;
        mangasAdelantados = Arrays.stream(mangasComprobacion).filter(man -> {
            Manga mangaBD = AddedMangasDB.ObtenerUno(man.getId());
            if(mangaBD == null){
                Log.e(TAG, "Manga nulo. ID: "+ man.getId());
                return false;
            }

            Calendar mangaAntesDeActualizar = Calendar.getInstance();
            Calendar mangaDespuesDeActualizar = Calendar.getInstance();

            try {
                mangaAntesDeActualizar.setTime(sdfSinHoras.parse(man.getFecha()));
                mangaDespuesDeActualizar.setTime(sdfSinHoras.parse(mangaBD.getFecha()));

                return mangaAntesDeActualizar.compareTo(mangaDespuesDeActualizar) > 0;
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }).toArray(Manga[]::new);

        Log.d(TAG, "Mangas adelantados = "+mangasAdelantados.length);

        if(mangasAdelantados.length == 0)
            return;

        String texto = "Se adelanta "+
                (Arrays.stream(mangasAdelantados).map(Manga::getNombre)
                        .reduce("",
                                (ant, prox) -> String.format("%s%s, ", ant, prox)));

        notificaciones.createNotification(
                mangasAdelantados.length
                        + " adelantos en mangas próximos al lanzamiento",
                texto.substring(0, texto.length()-2), //-2 para quitar la , y espacio,
                Constantes.CHANNEL_ID_ADELANTOS
        );
    }

    private void notificacionLanzamientosAtrasados() {
        Manga[] mangasAtrasados;
        mangasAtrasados = Arrays.stream(mangasComprobacion).filter(man -> {
            Manga mangaBD = AddedMangasDB.ObtenerUno(man.getId());
            if(mangaBD == null){
                Log.e(TAG, "Manga nulo. ID: "+ man.getId());
                return false;
            }

            Calendar mangaAntesDeActualizar = Calendar.getInstance();
            Calendar mangaDespuesDeActualizar = Calendar.getInstance();

            try {
                mangaAntesDeActualizar.setTime(sdfSinHoras.parse(man.getFecha()));
                mangaDespuesDeActualizar.setTime(sdfSinHoras.parse(mangaBD.getFecha()));

                return mangaAntesDeActualizar.compareTo(mangaDespuesDeActualizar) < 0;
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return false;
            }
        }).toArray(Manga[]::new);

        Log.d(TAG, "Mangas atrasados = "+mangasAtrasados.length);

        if(mangasAtrasados.length == 0)
            return;

        String texto = "Se retrasa "+
                (Arrays.stream(mangasAtrasados).map(Manga::getNombre)
                        .reduce("",
                                (ant, prox) -> String.format("%s%s, ", ant, prox)));

        notificaciones.createNotification(
                mangasAtrasados.length
                        + " retrasos en mangas próximos al lanzamiento",
                texto.substring(0, texto.length()-2), //-2 para quitar la , y espacio,
                Constantes.CHANNEL_ID_RETRASOS
        );
    }

    private void notificacionResultadosNuevosLanzamientos(Integer result) {
        Log.d(TAG, "Resultado: "+result);
        switch (result)
        {
            case -1:
                Log.e(TAG, "Error al obtener datos del FutureTask");
                mensaje += "Error al obtener datos del FutureTask";
                break;
            case 0:
                Log.d(TAG, "No hay mangas añadidos");
                break;
            case 1:
            case 2:
                notificaciones.createNotification((result == 2 ?
                                "¡Nuevos lanzamientos!" : "Nuevo lanzamiento: "+mangasNotif[0])
                        , (result == 2 ? "Comprueba los nuevos lanzamientos" :
                                "Nuevo lanzamiento de "+mangasNotif[0]+" el día "+mangasNotif[1])
                        , CHANNEL_ID
                );
        }
    }

    //endregion

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CrearAlarma() {
        Calendar proximaNotificacion = Constantes.ObtenerProximaNotificacion();

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

    /**
     * Llamada al servicio para recuperar datos de listado manga
     * @return 0 si no hay mangas sin fecha en la app,
     * 1 si está correcto y es solo un lanzamiento nuevo, 2 si correcto y varios lanzamientos,
     * -1 si hay algún error
     */
    public Callable<Integer> NuevosLanzamientos() {
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                try {
                    //AddedMangasDB.InstaciarBD(this);
                    Manga[] ms = AddedMangasDB.ActualizarNuevosLanzamientos();

                    if (ms.length == 0) return 0; //No hay mangas sin fecha, no actualizo nada

                    Log.d(TAG, "Hay " + ms.length + " mangas sin proxima fecha y sin finalizar");



                    Boolean MultiplesLanzamientos = false;
                    int TotalNuevosLanzamientos = 0;

                    for (Manga m : ms) {
                        long tiempoEspera = (long) Math.floor((Math.random() + 1) * 3000);
                        Log.d(TAG, "Esperando " + tiempoEspera + " para el siguiente");

                        sleep(tiempoEspera);

                        Manga NuevoLanz = new Manga(-1, "Nada nuevo");

                        NuevoLanz.setId(m.getId());
                        try {
                            Log.e(TAG, "Buscando datos del id: " + m.getId());

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

                            if(mangasComprobacion != null) {
                                if (Arrays.stream(mangasComprobacion).anyMatch(man -> man.getId()
                                        == NuevoLanz.getId()))
                                    continue;
                            }
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

                    return TotalNuevosLanzamientos > 1 ? 1 : TotalNuevosLanzamientos;

                }catch (Exception e)
                {
                    e.printStackTrace();
                    return -1;
                }
            }
        };
    }

}


