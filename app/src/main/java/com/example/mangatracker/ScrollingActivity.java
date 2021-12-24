package com.example.mangatracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.mangatracker.adapters.AdaptadorRecycler;
import com.example.mangatracker.broadcast.NuevosLanzamientosBroadcastReceiver;
import com.example.mangatracker.casosuso.CambioActividades;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.db.AddedMangasDB;
import com.example.mangatracker.servicios.BuscarNuevosLanzamientos;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mangatracker.databinding.ActivityScrollingBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScrollingActivity extends AppCompatActivity {

    private ActivityScrollingBinding binding;
    private RecyclerView rv;
    private final String TAG = Constantes.TAG_APP + "MA";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AddedMangasDB.InstaciarBD(this);


        binding = ActivityScrollingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> CambioActividades.CambioAddManga(ScrollingActivity.this));

        OperacionesRV();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();

        IniciarServicio();

        try {
            ((AdaptadorRecycler)rv.getAdapter()).ActualizarMangas(AdaptadorRecycler.TIPOADAPTER.ScrollingActivity,
                    AddedMangasDB.ObtenerTodos());
            rv.getAdapter().notifyDataSetChanged();
    //        Toast.makeText(this, "Numero de mangas añadidos: "+rv.getAdapter().getItemCount(), Toast.LENGTH_LONG).show();
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

//        Toast.makeText(this, "Estado de las notificaciones: " +
//                (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notificaciones", false) ?
//                "activadas" : "desactivadas"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("reiniciarservicio");
//        broadcastIntent.setClass(this, NuevosLanzamientosBroadcastReceiver.class);
//        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Me voy a cagar en mis putisimos muertos
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        switch (id){
            case R.id.action_settings:
//                Toast.makeText(this, "En proceso de. Un respiro pls", Toast.LENGTH_LONG).show();
                CambioActividades.CambioPreferencias(this);
                break;

            case R.id.MainMenuItemNuevosLanzamientos:
                CambioActividades.CambioNuevosLanzamientos(this);
                break;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void OperacionesRV() {
        rv = findViewById(R.id.rvMangasAdded);

        rv.setAdapter(new AdaptadorRecycler(AdaptadorRecycler.TIPOADAPTER.ScrollingActivity,
                AddedMangasDB.ObtenerTodos()));
        rv.setLayoutManager(new LinearLayoutManager(this));

        //Uso GetureDetector para que comprobar que el gesto realizado es un tap
        final GestureDetector gestureDetector = new GestureDetector(ScrollingActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void IniciarServicio() { //Genera la alarma que iniciará el servicio
        if(!PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean("notificaciones", false)) return;

        Log.d(TAG, "Lanzando el servicio...");

//        startService(new Intent(this, BuscarNuevosLanzamientos.class));

        Calendar proximaNotificacion = Constantes.ObtenerProximaNotificacion();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Log.d(TAG, "Proxima notificacion: "+ sdf.format(proximaNotificacion.getTime()));
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
    }
}