package com.example.mangatracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.example.mangatracker.broadcast.NuevosLanzamientosBroadcastReceiver;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.databinding.PruebasBinding;
import com.example.mangatracker.db.AddedMangasDB;
import com.example.mangatracker.notificaciones.Notificaciones;
import com.example.mangatracker.webservice.WSLogs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PruebasActivity extends AppCompatActivity {
    private final String TAG = Constantes.TAG_APP + "Pruebas";
    PruebasBinding binding;
    Notificaciones notificaciones;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PruebasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notificaciones = new Notificaciones(this);

        CrearEventos();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CrearEventos() {
        binding.PruebasBtnLanzarNotificacion.setOnClickListener(l ->
                notificaciones.LanzamientosPrueba());

        binding.PruebasBtnLanzarServicio.setOnClickListener(l ->
                IniciarServicio(false));

        binding.pruebasBtnLanzarServicioInmediato.setOnClickListener(l ->
                IniciarServicio(true));

        binding.btnBorrarLogs.setOnClickListener(l -> BorrarLogs());

        binding.btnCrearFicheroLog.setOnClickListener(l ->
                WSLogs.EnviarLogs(this.getApplicationContext()));

        binding.btnVerFicheroLog.setOnClickListener(l -> VerFicheroLog());

        binding.btnEnviarLogs.setOnClickListener(l -> EnviarLogs());
    }

    private void EnviarLogs() {
        Uri uri = getUriDeFichero(Constantes.ficheroLogs);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"r.rodriguezgarlito@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Fichero de logs");

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Enviar correo..."));
    }

    private void VerFicheroLog() {
        try {
            Uri uri = getUriDeFichero(Constantes.ficheroLogs);
            String mime = getContentResolver().getType(uri);

            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setDataAndType(uri, mime);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private Uri getUriDeFichero(String fichero) {
        File file = new File(this.getApplicationContext().getFilesDir(), fichero);

        Uri uri = FileProvider.getUriForFile(this,
                this.getApplication().getPackageName() + ".fileprovider", file);
        return uri;
    }

    private void BorrarLogs() {
        AddedMangasDB.InstaciarBD(getApplicationContext());
        AddedMangasDB.BorrarLogs();

        Toast.makeText(this, "Logs eliminados", Toast.LENGTH_LONG).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void IniciarServicio(boolean LanzarInmediato) { //Genera la alarma que iniciará el servicio
        Calendar proximaNotificacion = Calendar.getInstance();

        if(!LanzarInmediato)
            proximaNotificacion.add(Calendar.MINUTE, 2);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        //En lugar de destruirse, llamo de nuevo al BroadcastReceiver en x
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                new Intent(this, NuevosLanzamientosBroadcastReceiver.class)
                        .addFlags(Intent.FLAG_RECEIVER_FOREGROUND),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);
        }else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    proximaNotificacion.getTimeInMillis(), pendingIntent);


        Toast.makeText(this, "Se lanzara la notificacion a las "
                +sdf.format(proximaNotificacion.getTime()), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Lanzando servicio a las "+ Constantes.sdf.format(proximaNotificacion.getTime()));
    }
}
