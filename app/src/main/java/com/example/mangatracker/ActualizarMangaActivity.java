package com.example.mangatracker;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mangatracker.Excepciones.NumeroMangasException;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.databinding.ActualizarMangaBinding;
import com.example.mangatracker.db.AddedMangasDB;
import java.io.IOException;
import java.text.SimpleDateFormat;
import clases.MangaDatos;
import operaciones.MangaScrapper;

public class ActualizarMangaActivity extends AppCompatActivity {
    private ActualizarMangaBinding binding;
    private Manga mangaActualizar;
    private String actividadPadre; //Para comprobar la actividad que ha lanzado esta
    private final String TAG = Constantes.TAG_APP + "AMA";

    //Componentes layout
    private TextView txtNombre;
    private TextView txtTomosEditados;
    private TextView txtTomosEnEdicion;
    private TextView txtTomosNoEditados;
    private EditText etTomosComprados;
    private Switch swHold;

    private MenuItem item;

    private static MangaDatos md;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mangaActualizar = (Manga)getIntent().getSerializableExtra("manga");
        actividadPadre = getIntent().getStringExtra("actividad padre");

        binding = ActualizarMangaBinding.inflate(getLayoutInflater());
        setContentView(R.layout.actualizar_manga);

        try {
            LlenarDatos();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage("Error. No se han podido recuperar datos del manga\n\n"+e.getLocalizedMessage())
                    .setPositiveButton("Volver", (dialog, which) -> finish()).show();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void LlenarDatos() throws InterruptedException {

        if(actividadPadre.equals("AddMangaActivity")) {
            Thread th = new Thread(() -> {
                try {
                    md = MangaScrapper.ObtenerDatosDe(mangaActualizar.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            th.start();
            th.join();


            if (md == null && actividadPadre.equals("AddMangaActivity")) {
                new AlertDialog.Builder(this)
                        .setMessage("Error. No se han podido recuperar datos del manga")
                        .setPositiveButton("Volver", (dialog, which) -> finish()).show();
                return;
            }
            mangaActualizar.setTerminado(md.getTerminado());
            mangaActualizar.setTomosNoEditados(md.getTomosNoEditados());
            mangaActualizar.setTomosEnPreparacion(md.getTomosEnPreparacion());
            mangaActualizar.setTomosEditados(md.getTomosEditados());

            if (md.getFecha() != null) {
                mangaActualizar.setFecha( md.getFecha());
            }
        }else
        {
            mangaActualizar = AddedMangasDB.ObtenerUno(mangaActualizar.getId());
        }


        txtNombre = findViewById(R.id.actualizar_manga_txtVNombre);
        txtNombre.append(mangaActualizar.getNombre());

        txtTomosEditados = findViewById(R.id.actualizarmanga_txtValaventa);
        txtTomosEditados.append(Integer.toString(mangaActualizar.getTomosEditados()));

        txtTomosEnEdicion = findViewById(R.id.actualizarmanga_txtVenedicion);
        txtTomosEnEdicion.append(Integer.toString(mangaActualizar.getTomosEnPreparacion()));

        txtTomosNoEditados = findViewById(R.id.actualizarmanga_txtVNoEditados);
        txtTomosNoEditados.append(Integer.toString(mangaActualizar.getTomosNoEditados()));

        etTomosComprados = findViewById(R.id.actualizarmanga_tomoscomprados);
        etTomosComprados.setText(mangaActualizar.getTomosComprados() == -1 ? "0" :
                Integer.toString(mangaActualizar.getTomosComprados()));

        swHold = findViewById(R.id.swHold);
        swHold.setChecked(mangaActualizar.getDrop());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar_manga, menu);
        item = menu.findItem(R.id.menuactualizarmangaBorrar);

        if(!actividadPadre.equals("ScrollingActivity")){
            item.setVisible(false);
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuactualizarmangas_cancelar:
                finish();
                break;
            case R.id.menuactualizarmanga_insertar:
                try {
                    OperacionSegunActivityPadre();
                }catch(NumberFormatException e){
                    Toast.makeText(this, "Debes introducir un n??mero v??lido para los mangas comprados",
                            Toast.LENGTH_LONG).show();
                    break;
                } catch (NumeroMangasException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    break;
                }
                finish();
                break;
            case R.id.menuactualizarmangaBorrar:
                BorrarItem();
                break;
            default:
                Toast.makeText(this, "Ha ocurrido un problema (no se reconoce el bot??n)",
                        Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void BorrarItem() {
        if(actividadPadre.equals("ScrollingActivity"))
        {
            new AlertDialog.Builder(ActualizarMangaActivity.this)
                    .setMessage("??Quieres borrar este manga?")
                    .setNegativeButton("No", (dialog, which) -> {
                    })
                    .setPositiveButton("Si", (d, w) -> {
                        //Pruebas.getMangasAdded()
                        //        .removeIf(m -> m.getNombre().equals(mangaActualizar.getNombre()));
                        AddedMangasDB.EliminarManga(mangaActualizar.getId());
                        finish();
                    })
                    .show();
        }else {
            Toast.makeText(this, "No se puede eliminar un item que no se ha a??adido", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void OperacionSegunActivityPadre() throws NumeroMangasException {
        switch (actividadPadre){
            case "AddMangaActivity":
                AddManga();
                break;
            case "ScrollingActivity":
                UpdateManga();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void UpdateManga() throws NumeroMangasException {
        //Pruebas.getMangasAdded().removeIf((m) -> m.getNombre().equals(mangaActualizar.getNombre()));
        mangaActualizar.setTomosComprados(Integer.parseInt(etTomosComprados.getText().toString()));
        mangaActualizar.setDrop(swHold.isChecked());
        //Pruebas.getMangasAdded().add(mangaActualizar);
        AddedMangasDB.ActualizarManga(mangaActualizar);
//        AddedMangasDB.ActualizarFecha(mangaActualizar.getId(), mangaActualizar.getFecha());
    }

    private void AddManga() throws NumeroMangasException {
        String s = "No se ha podido a??adir el manga.";
        mangaActualizar.setTomosComprados(Integer.parseInt(etTomosComprados.getText().toString()));
        mangaActualizar.setDrop(swHold.isChecked());

        try {
            AddedMangasDB.InsertarManga(mangaActualizar);
            //if(Pruebas.addManga(mangaActualizar)){
            s = "Manga a??adido con ??xito";
            // }
        }catch(Exception e){
            Log.e(TAG, "ERROR: \n"+e.getMessage());
            s += "\n Error: "+e.getMessage();
        }
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

}
