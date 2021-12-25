package com.example.mangatracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.mangatracker.broadcast.NuevosLanzamientosBroadcastReceiver;
import com.example.mangatracker.databinding.PruebasBinding;
import com.example.mangatracker.notificaciones.Notificaciones;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PruebasActivity extends AppCompatActivity {
    PruebasBinding binding;
    Notificaciones notificaciones;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PruebasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notificaciones = new Notificaciones(this);

        CrearEventos();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void CrearEventos() {
        binding.PruebasBtnLanzarNotificacion.setOnClickListener(l ->
                notificaciones.LanzamientosPrueba());

        binding.PruebasBtnLanzarServicio.setOnClickListener(l ->
                IniciarServicio());
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void IniciarServicio() { //Genera la alarma que iniciará el servicio
        if(!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("notificaciones", false)) return;

        Calendar proximaNotificacion = Calendar.getInstance();
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


    }
}
