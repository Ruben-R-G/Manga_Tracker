package com.example.mangatracker;

import android.Manifest;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangatracker.adapters.AdaptadorRecycler;
import com.example.mangatracker.casosuso.CambioActividades;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.databinding.AddMangaBinding;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import clases.MangaBusqueda;
import operaciones.BuscarManga;

public class AddMangaActivity extends AppCompatActivity {
    AddMangaBinding binding;
    EditText busqueda;
    List<Manga> mangas = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddMangaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        busqueda = findViewById(R.id.editTxtTitulo);

        Permisos.PedirPermisos(this, new String[]{Manifest.permission.INTERNET});
        CrearEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //todo Actualizar la query de la busqueda
    }

    private void CrearEventos() {
        binding.btnBuscar.setOnClickListener((v) -> {
            if(busqueda.getText().length() < 3)
            {
                Toast.makeText(v.getContext(), "Introduce al menos 3 caracteres", Toast.LENGTH_LONG).show();
                return;
            }
            if(!Permisos.TienePermisos(this, new String[]{Manifest.permission.INTERNET}))
            {
                Toast.makeText(v.getContext(), "Debes habilitar los permisos de Internet", Toast.LENGTH_LONG).show();
                return;
            }

            /*
            * todo usar hilos para llamadas a listadomanga
            *  Quitar las 3 lineas de strictmode si se usan hilos o async
            */
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                MangaBusqueda[] mangasEncontrados =
                        BuscarManga.BuscarPorNombreManga(busqueda.getText().toString());

                mangas = new ArrayList<>();

                for(MangaBusqueda mBuscado : mangasEncontrados)
                {
                    Manga m = new Manga(mBuscado.getId(), mBuscado.getNombre());

                    mangas.add(m);
                }

                ((AdaptadorRecycler)binding.addMangaRecycler.getAdapter())
                        .ActualizarMangas(AdaptadorRecycler.TIPOADAPTER.AddManga,
                                mangas.toArray(new Manga[]{}));

                binding.addMangaRecycler.getAdapter().notifyDataSetChanged();

            } catch (IOException e) {
                Toast.makeText(v.getContext(), "Ha ocurrido un error al buscar", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        //Para pruebas
        //Pruebas.llenarMangas();

        OperacionesRV();

    }

    private void OperacionesRV() {
        binding.addMangaRecycler.setAdapter
                (new AdaptadorRecycler(AdaptadorRecycler.TIPOADAPTER.AddManga,
                new Manga[]{}));
        binding.addMangaRecycler.setLayoutManager(new LinearLayoutManager(this));

        //Uso GetureDetector para que comprobar que el gesto realizado es un tap
        final GestureDetector gestureDetector = new GestureDetector(AddMangaActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });

        binding.addMangaRecycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {
                View child = binding.addMangaRecycler.findChildViewUnder(e.getX(), e.getY());

                //Compruebo que ha ocurrido un gesto y que el GD es true (es un tap)
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int pos = binding.addMangaRecycler.getChildAdapterPosition(child);

                    new AlertDialog.Builder(AddMangaActivity.this)
                            .setMessage("¿Quieres añadir el manga " + mangas.get(pos).getNombre() + "?")
                            .setPositiveButton("Si", (dialog, which) -> {
                                CambioActividades.CambioActualizarManga(AddMangaActivity.this,
                                        "AddMangaActivity", mangas.get(pos));

                            })
                            .setNegativeButton("Cancelar", (dial, w) -> {
                            })
                            .create()
                            .show();
                    return true;
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }
}
