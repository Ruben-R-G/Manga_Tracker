package com.example.mangatracker;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangatracker.adapters.AdaptadorRecycler;
import com.example.mangatracker.casosuso.CambioActividades;
import com.example.mangatracker.databinding.NuevosLanzamientosActivityBinding;
import com.example.mangatracker.db.AddedMangasDB;
import com.example.mangatracker.pruebas.Pruebas;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NuevosLanzamientosActivity extends AppCompatActivity {
    private NuevosLanzamientosActivityBinding binding;
    private RecyclerView rv;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NuevosLanzamientosActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.nlToolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.nlToolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> CambioActividades.CambioMain(this));
        OperacionesRV();
    }

    private void OperacionesRV() {
        rv = findViewById(R.id.nl_recview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rv.setAdapter(new AdaptadorRecycler(AdaptadorRecycler.TIPOADAPTER.NuevosLanzamientosActivity,
                    AddedMangasDB.ObtenerNuevosLanzamientos(false)));
        }
//        rv.setAdapter(new AdaptadorRecycler(AdaptadorRecycler.TIPOADAPTER.NuevosLanzamientosActivity,
//                Pruebas.PruebaNuevosLanzamientos()));

        rv.setLayoutManager(new LinearLayoutManager(this));

        //Uso GetureDetector para que comprobar que el gesto realizado es un tap
        final GestureDetector gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });
    }


}
