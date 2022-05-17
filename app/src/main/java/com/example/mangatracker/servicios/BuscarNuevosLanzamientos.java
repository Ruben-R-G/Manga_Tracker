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
import com.example.mangatracker.webservice.WSLogs;
import com.example.mangatracker.webservice.WSMangas;

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
import java.util.stream.Collectors;

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

    private List<Manga> nuevosMangas;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        new Thread(() -> {
            try {
                notificaciones = new Notificaciones(this);
                AddedMangasDB.InstaciarBD(this);
                Log.d(TAG, "onCreate");
                AddedMangasDB.InsertarLog(TAG, "1.0", "Servicio iniciado");

                //Obtenemos los lanzamientos que se lanzan ese día para mostrar la notificacion
                ComprobarMangasLanzadosHoy();

                //Se quedan a null las fechas que ya han pasado
                AddedMangasDB.ObtenerNuevosLanzamientos(true);

                ComprobarMangasConFecha();

                FutureTask<Integer> lanzamientos = new FutureTask<>(NuevosLanzamientos());
                ExecutorService exec = Executors.newCachedThreadPool();
                exec.submit(lanzamientos);

                Log.d(TAG, "Esperando al FutureTask");

                while (!lanzamientos.isDone()) {
                }
                Integer result = lanzamientos.get();
                exec.shutdown();

                notificacionResultadosNuevosLanzamientos(result);

                //Para los lanzamientos próximos
                //notificacionProximosLanzamientos();

                //Para los lanzamientos adelantados
                if (mangasComprobacion != null) {
                    notificacionLanzamientosAdelantados();
                    notificacionLanzamientosAtrasados();
                }

                //Para los lanzamiento que se lanzan ese dia
                notificacionLanzamientosHoy();

                notificaciones.LanzamientosPrueba();
            } catch (Exception e) {
                AddedMangasDB.InsertarLog(TAG, "1.999",
                        "EXCEPCIÓN NO CONTROLADA: " + e.getMessage().replace('\'', '"'));
                Log.e(TAG, "EXCEPCION NO CONTROLADA: \n" + e.getMessage());
                mensaje += e.getMessage() + "\n";
            }
            CrearAlarma();

            WSLogs.EnviarLogs(getApplicationContext());

            this.stopSelf(); //Llama al onDestroy
        }).start();
    }


    //region Comprobaciones
    private void ComprobarMangasLanzadosHoy() {
        mangasLanzadosHoy = Arrays.stream(AddedMangasDB.ObtenerNuevosLanzamientos(false))
                .filter(m -> {
                    Date FechaManga;
                    FechaManga = m.getFecha();
                    Calendar hoy = Calendar.getInstance();

                    return FechaManga.before(hoy.getTime());

                }).toArray(Manga[]::new);

        AddedMangasDB.InsertarLog(TAG, "1.1",
                "Comprobados los mangas lanzados hoy. Número de mangas: " + mangasLanzadosHoy.length);
    }

    private void ComprobarMangasConFecha() {

        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                == Calendar.SUNDAY
                &&
                Constantes.ObtenerProximaNotificacion()
                        .get(Calendar.HOUR_OF_DAY)
                        == Constantes.horasNotificaciones[0]
        ) //Mangas adelantados
        {
            Log.d(TAG, "Comprobamos todos los mangas");

            mangasComprobacion = AddedMangasDB.ObtenerNuevosLanzamientos(false);

            if (mangasComprobacion.length != 0) {
                for (Manga m : mangasComprobacion) {
                    AddedMangasDB.ActualizarFecha(m.getId(), null);
                }
            }
            AddedMangasDB.InsertarLog(TAG, "1.2", "Comprobamos todos los mangas con fecha. " +
                    "Mangas que se han puesto con fecha a null: " + mangasComprobacion.length);
        }
    }

    //endregion

    //region Notificaciones

    private void notificacionLanzamientosHoy() {

        if (mangasLanzadosHoy == null) return;
        if (mangasLanzadosHoy.length == 0) return;

        AddedMangasDB.InsertarLog(TAG, "1.7",
                "Mangas que salen el día de hoy: " +
                        (mangasLanzadosHoy == null ? "Objeto mangasLanzadosHoy no esta instanciado" :
                                mangasLanzadosHoy.length));

        String texto = "Hoy se lanza: \n" +
                (Arrays.stream(mangasLanzadosHoy).map(Manga::getNombre)
                        .reduce("",
                                (ant, prox) -> String.format("%s%s, ", ant, prox)));

        AddedMangasDB.InsertarLog(TAG, "1.7.1",
                "Creamos notificación de mangas que se lanzan hoy: " + texto);

        notificaciones.createNotification(
                (mangasLanzadosHoy.length +
                        (mangasLanzadosHoy.length == 1 ? " manga sale" : " mangas salen") + " hoy!"),
                texto.substring(0, texto.length() - 2), //-2 para quitar la , y espacio
                Constantes.CHANNEL_ID_HOY
        );
    }

    private void notificacionLanzamientosAdelantados() {
        AddedMangasDB.InsertarLog(TAG, "1.5",
                "Comprobamos mangas que se adelanten");

        Manga[] mangasAdelantados;
        mangasAdelantados = Arrays.stream(mangasComprobacion).filter(man -> {
            Manga mangaBD = AddedMangasDB.ObtenerUno(man.getId());
            if (mangaBD == null) {
                Log.e(TAG, "Manga nulo. ID: " + man.getId());
                AddedMangasDB.InsertarLog(TAG, "1.5.1",
                        "Manga nulo. ID: " + man.getId());

                return false;
            }

            Calendar mangaAntesDeActualizar = Calendar.getInstance();
            Calendar mangaDespuesDeActualizar = Calendar.getInstance();

            mangaAntesDeActualizar.setTime(man.getFecha());
            mangaDespuesDeActualizar.setTime(mangaBD.getFecha());

            return mangaAntesDeActualizar.compareTo(mangaDespuesDeActualizar) > 0;

        }).toArray(Manga[]::new);

        Log.d(TAG, "Mangas adelantados = " + mangasAdelantados.length);

        AddedMangasDB.InsertarLog(TAG, "1.5.2",
                "Mangas adelantados = " + mangasAdelantados.length);

        if (mangasAdelantados.length == 0)
            return;

        String texto = "Se adelanta " +
                (Arrays.stream(mangasAdelantados).map(Manga::getNombre)
                        .reduce("",
                                (ant, prox) -> String.format("%s%s, ", ant, prox)));

        AddedMangasDB.InsertarLog(TAG, "1.5.3",
                "Creamos notificación de mangas adelantados: " + texto);

        notificaciones.createNotification(
                mangasAdelantados.length
                        + " adelantos en mangas próximos al lanzamiento",
                texto.substring(0, texto.length() - 2), //-2 para quitar la , y espacio,
                Constantes.CHANNEL_ID_ADELANTOS
        );
    }

    private void notificacionLanzamientosAtrasados() {
        AddedMangasDB.InsertarLog(TAG, "1.6",
                "Comprobamos mangas que se atrasen");

        Manga[] mangasAtrasados;
        mangasAtrasados = Arrays.stream(mangasComprobacion).filter(man -> {
            Manga mangaBD = AddedMangasDB.ObtenerUno(man.getId());
            if (mangaBD == null) {
                Log.e(TAG, "Manga nulo. ID: " + man.getId());
                AddedMangasDB.InsertarLog(TAG, "1.6.1", "Manga nulo. ID: " +
                        man.getId());

                return false;
            }

            Calendar mangaAntesDeActualizar = Calendar.getInstance();
            Calendar mangaDespuesDeActualizar = Calendar.getInstance();

            mangaAntesDeActualizar.setTime(man.getFecha());
            mangaDespuesDeActualizar.setTime(mangaBD.getFecha());

            return mangaAntesDeActualizar.compareTo(mangaDespuesDeActualizar) < 0;

        }).toArray(Manga[]::new);

        Log.d(TAG, "Mangas atrasados = " + mangasAtrasados.length);
        AddedMangasDB.InsertarLog(TAG, "1.6.2",
                "Mangas atrasados = " + mangasAtrasados.length);

        if (mangasAtrasados.length == 0)
            return;

        String texto = "Se retrasa " +
                (Arrays.stream(mangasAtrasados).map(Manga::getNombre)
                        .reduce("",
                                (ant, prox) -> String.format("%s%s, ", ant, prox)));

        AddedMangasDB.InsertarLog(TAG, "1.6.3",
                "Creamos notificación de mangas atrasados: " + texto);


        notificaciones.createNotification(
                mangasAtrasados.length
                        + " retrasos en mangas próximos al lanzamiento",
                texto.substring(0, texto.length() - 2), //-2 para quitar la , y espacio,
                Constantes.CHANNEL_ID_RETRASOS
        );
    }

    private void notificacionResultadosNuevosLanzamientos(Integer result) {
        Log.d(TAG, "Resultado: " + result);


        switch (result) {
            case -1:
                Log.e(TAG, "Error al obtener datos del FutureTask");
                AddedMangasDB.InsertarLog(TAG, "1.4",
                        "Resultado: " + result +
                                ". Error al obtener datos del FutureTask");
                mensaje += "Error al obtener datos del FutureTask";
                break;
            case 0:
                Log.d(TAG, "No hay mangas añadidos");
                AddedMangasDB.InsertarLog(TAG, "1.4",
                        "Resultado: " + result +
                                ". No hay nuevos mangas añadidos");
                break;
            case 1:
            case 2:
                AddedMangasDB.InsertarLog(TAG, "1.4",
                        "Resultado: " + result +
                                ". Hay nuevos lanzamientos. Se lanza la notificacion");

                notificaciones.createNotification((result == 2 ?
                                "¡Nuevos lanzamientos!" : "Nuevo lanzamiento: " + mangasNotif[0])
                        , (result == 2 ? "Comprueba los nuevos lanzamientos" :
                                "Nuevo lanzamiento de " + mangasNotif[0] + " el día " + mangasNotif[1])
                        , CHANNEL_ID
                );
                break;
            case 99:
                AddedMangasDB.InsertarLog(TAG, "1.4",
                        "Resultado: " + result +
                                ". Solicitud realizada a ws");

                notificaciones.createNotification((nuevosMangas.size() == 2 ?
                                "¡Nuevos lanzamientos!" : "Nuevo lanzamiento: " + mangasNotif[0])
                        , (nuevosMangas.size() == 2 ? "Comprueba los nuevos lanzamientos" :
                                "Nuevo lanzamiento de " + mangasNotif[0] + " el día " + mangasNotif[1])
                        , CHANNEL_ID);
        }
    }

    //endregion

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CrearAlarma() {
        Calendar proximaNotificacion = Constantes.ObtenerProximaNotificacion();

        Log.d(TAG, "Proxima notificacion: " + sdf.format(proximaNotificacion.getTime()));

        AddedMangasDB.InsertarLog(TAG, "1.8",
                "Proxima notificacion: " + sdf.format(proximaNotificacion.getTime()));

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

        AddedMangasDB.InsertarLog(TAG, "1.8.1", "Alarma creada para la siguiente llamada al servicio");

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        AddedMangasDB.InsertarLog(TAG, "9.0", "onDestroy");
    }

    /**
     * Llamada al servicio para recuperar datos de listado manga
     *
     * @return 0 si no hay mangas sin fecha en la app,
     * 1 si está correcto y es solo un lanzamiento nuevo, 2 si correcto y varios lanzamientos,
     * -1 si hay algún error
     */
    public Callable<Integer> NuevosLanzamientos() {
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                try {
                    boolean pruebas = false;
                    //AddedMangasDB.InstaciarBD(this);
                    Manga[] ms = AddedMangasDB.ActualizarNuevosLanzamientos();

                    if (ms.length == 0) return 0; //No hay mangas sin fecha, no actualizo nada

                    Log.d(TAG, "Hay " + ms.length + " mangas sin proxima fecha y sin finalizar");
                    AddedMangasDB.InsertarLog(TAG, "1.3.0", "Hay " + ms.length + " mangas sin proxima fecha y sin finalizar");

                    if (pruebas) {
                        List<Manga> mangas = Arrays.asList(ms.clone());
                        mangas = WSMangas.EnviarPeticion(mangas);

                        AddedMangasDB.ActualizarMangas(mangas);

                        if (mangasComprobacion != null)
                        {
                            nuevosMangas = mangas.stream().filter(m -> {
                                for (Manga man : mangasComprobacion) {
                                    if (man.getId() == m.getId()) return false;
                                }

                                return true;
                            }).collect(Collectors.toList());
                        }

                        return 99;
                    } else {

                        Boolean MultiplesLanzamientos = false;
                        int TotalNuevosLanzamientos = 0;

                        for (int i = 0; i < ms.length; i++) {
                            Manga m = ms[i];

                            long tiempoEspera = (long) Math.floor((Math.random() + 1) * 3000);
                            Log.d(TAG, "Esperando " + tiempoEspera + " para el siguiente");

                            sleep(tiempoEspera);

                            Manga NuevoLanz = new Manga(-1, "Nada nuevo");

                            NuevoLanz.setId(m.getId());
                            try {
                                Log.e(TAG, "Buscando datos del id: " + m.getId());
                                AddedMangasDB.InsertarLog(TAG, "1.3.1", "Buscando datos del id: " + m.getId());

                                MangaDatos nuevosDatos = MangaScrapper.ObtenerDatosDe(m.getId());

                                NuevoLanz.setNombre(m.getNombre());

                                //Obtengo los datos actualizados y tambien los inserto
                                NuevoLanz.setTomosEditados(nuevosDatos.getTomosEditados());
                                NuevoLanz.setTomosEnPreparacion(nuevosDatos.getTomosEnPreparacion());
                                NuevoLanz.setTomosNoEditados(nuevosDatos.getTomosNoEditados());
                                NuevoLanz.setTerminado(nuevosDatos.getTerminado());

                                //Mantengo los datos (al hacer la insercion en sql,
                                // tambien se actualizan los tomos comprados)
                                NuevoLanz.setTomosComprados(m.getTomosComprados());
                                NuevoLanz.setDrop(m.getDrop());
                                NuevoLanz.setFav(m.getFav());

                                AddedMangasDB.ActualizarManga(NuevoLanz);

                                //Si no se obtiene fecha, no cuenta como nuevo lanzamiento
                                if (nuevosDatos.getFecha() == null) {
                                    AddedMangasDB.InsertarLog(TAG, "1.3.2",
                                            NuevoLanz.getId() + ": En Preparación: " + NuevoLanz.getTomosEnPreparacion()
                                                    + " / Fecha Lanzamiento: "
                                                    + "No existe");
                                    continue;
                                }

                                NuevoLanz.setFecha(nuevosDatos.getFecha());
                                AddedMangasDB.ActualizarFecha(NuevoLanz.getId(),
                                        NuevoLanz.getFecha());

                                AddedMangasDB.InsertarLog(TAG, "1.3.2",
                                        NuevoLanz.getId() + ": En Preparación: " + NuevoLanz.getTomosEnPreparacion()
                                                + " / Fecha Lanzamiento: " + NuevoLanz.getFecha());

                                if (mangasComprobacion != null) {
                                    if (mangasComprobacion.length != 0) {
                                        if (Arrays.stream(mangasComprobacion).anyMatch(man -> man.getId()
                                                == NuevoLanz.getId())) {
                                            AddedMangasDB.InsertarLog(TAG, "1.3.3", NuevoLanz.getId() +
                                                    " es un id para comprobar. No buscamos si es un nuevo lanzamiento. Saltamos esa parte.");
                                            continue;
                                        }
                                    }
                                }
                                TotalNuevosLanzamientos++;

                                if (TotalNuevosLanzamientos > 1) {
                                    MultiplesLanzamientos = true;
                                    continue;
                                }

                                //Datos del manga que se mostrará en la notificación si es el único
                                mangasNotif[0] = NuevoLanz.getNombre();
                                mangasNotif[1] = sdfSinHoras.format(NuevoLanz.getFecha());

                            } catch (IOException e) {
                                AddedMangasDB.InsertarLog(TAG, "1.3.99", NuevoLanz.getId() +
                                        " Error IOException: " + e.getMessage().replace('\'', '"'));
                                e.printStackTrace();
                                mensaje += e.getMessage() + "\n";
                            } catch (NoSuchMethodError e) {
                                AddedMangasDB.InsertarLog(TAG, "1.3.99", NuevoLanz.getId() +
                                        " Error NoSuchMethodError: " + e.getMessage().replace('\'', '"'));
                                e.printStackTrace();
                                mensaje += e.getMessage() + "\n";

                                sleep(5000);
                            } catch (Exception e) {
                                AddedMangasDB.InsertarLog(TAG, "1.3.99", NuevoLanz.getId() +
                                        " Error Exception: " + e.getMessage().replace('\'', '"'));
                                e.printStackTrace();
                                mensaje += e.getMessage() + "\n";
                            }
                        }

                        return TotalNuevosLanzamientos > 1 ? 2 : TotalNuevosLanzamientos;
                    }
                } catch (Exception e) {
                    AddedMangasDB.InsertarLog(TAG, "1.3.999",
                            "Error en la búsqueda de nuevos lanzamientos: " +
                                    e.getMessage().replace('\'', '"'));
                    e.printStackTrace();
                    return -1;
                }
            }
        };
    }


}


