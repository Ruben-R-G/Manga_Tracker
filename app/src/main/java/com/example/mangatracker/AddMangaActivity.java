package com.example.mangatracker;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangatracker.adapters.AdaptadorRecycler;
import com.example.mangatracker.casosuso.CambioActividades;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.databinding.AddMangaBinding;
import com.example.mangatracker.db.AddedMangasDB;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import clases.MangaBusqueda;
import operaciones.BuscarManga;

import static java.lang.Thread.sleep;

public class AddMangaActivity extends AppCompatActivity {
    AddMangaBinding binding;
    EditText busqueda;
    private ProgressBar progressBar;
    List<Manga> mangas = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddMangaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        busqueda = findViewById(R.id.editTxtTitulo);
        progressBar = findViewById(R.id.progressBarAddManga);

        Permisos.PedirPermisos(this, new String[]{Manifest.permission.INTERNET});
        CrearEventos();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();

        //todo meter en activity result
        binding.btnBuscar.callOnClick();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {
                try {
                    MangaBusqueda[] mangasEncontrados =
                            BuscarManga.BuscarPorNombreManga(busqueda.getText().toString());

                    mangas = new ArrayList<>();


                    for(MangaBusqueda mBuscado : mangasEncontrados)
                    {
                        Manga m = new Manga(mBuscado.getId(), mBuscado.getNombre());

                        mangas.add(m);
                    }

                    QuitarMangasAdded();

                    runOnUiThread(() -> {
                        ((AdaptadorRecycler)binding.addMangaRecycler.getAdapter())
                                .ActualizarMangas(AdaptadorRecycler.TIPOADAPTER.AddManga,
                                        mangas.toArray(new Manga[]{}));

                        binding.addMangaRecycler.getAdapter().notifyDataSetChanged();

                        progressBar.setVisibility(View.GONE);
                    });

                } catch (IOException e) {
                    Toast.makeText(v.getContext(), "Ha ocurrido un error al buscar", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }).start();
        });

        //Para pruebas
        //Pruebas.llenarMangas();

        OperacionesRV();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void QuitarMangasAdded() {
        mangas = mangas.stream().filter(m ->
                !Arrays.stream(AddedMangasDB.ObtenerTodos())
                        .anyMatch(m2 -> m.getId() == m2.getId())
        ).collect(Collectors.toList());
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
