package com.example.mangatracker.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangatracker.AddMangaActivity;
import com.example.mangatracker.Excepciones.NumeroMangasException;
import com.example.mangatracker.R;
import com.example.mangatracker.ScrollingActivity;
import com.example.mangatracker.casosuso.CambioActividades;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.db.AddedMangasDB;
import com.example.mangatracker.pruebas.Pruebas;

import org.jetbrains.annotations.NotNull;

/*
Crear un adaptador para el recyclerView. Este es usado para los mangas añadidos
 */
public class AdaptadorRecycler extends RecyclerView.Adapter<AdaptadorRecycler.ViewHolder> {
    public enum TIPOADAPTER
    {
        AddManga, ScrollingActivity, NuevosLanzamientosActivity
    }

    private Manga[] mangas; //Para probar antes de usar bd
    private TIPOADAPTER tipo;


    /*
    Crear una clase viewHolder con el layout del item, creando un constructor y
    lo que tenga el layout
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nombreAddManga;
        private final TextView nombre;
        private final TextView tomosEditados;
        private final TextView tomosEnPreparacion;
        private final TextView tomosNoEditados;
        private final TextView tomosComprados;
        private final ImageButton btnMasUno;
        private final ImageButton btnMenosUno;
        private final ImageButton btnEditar;
        private final TextView NlNombre;
        private final TextView NlFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreAddManga = itemView.findViewById(R.id.mangaitemaddTxtVNombre);
            nombre = itemView.findViewById(R.id.txtViewNombre);
            tomosEditados = itemView.findViewById(R.id.txtViewEditados);
            tomosEnPreparacion = itemView.findViewById(R.id.txtViewNumerosEnPreparacion);
            tomosNoEditados = itemView.findViewById(R.id.txtViewNumerosNoEditados);
            tomosComprados = itemView.findViewById(R.id.txtViewTomosComprados);
            btnMasUno = itemView.findViewById(R.id.mangaitemAddUno);
            btnMenosUno = itemView.findViewById(R.id.mangaitemBtnRemUno);
            btnEditar = itemView.findViewById(R.id.mangaitemEditar);
            NlNombre = itemView.findViewById(R.id.TvNlItem);
            NlFecha = itemView.findViewById(R.id.TvNlFecha);
        }

        public TextView getNombreAddManga() {
            return nombreAddManga;
        }

        public TextView getNombre() {
            return nombre;
        }

        public TextView getTomosEditados() {
            return tomosEditados;
        }

        public TextView getTomosEnPreparacion() {
            return tomosEnPreparacion;
        }

        public TextView getTomosNoEditados() {
            return tomosNoEditados;
        }

        public TextView getTomosComprados() {
            return tomosComprados;
        }

        public ImageButton getBtnMasUno() {
            return btnMasUno;
        }

        public ImageButton getBtnMenosUno() {
            return btnMenosUno;
        }

        public ImageButton getBtnEditar() {
            return btnEditar;
        }

        public TextView getNlNombre() {
            return NlNombre;
        }

        public TextView getNlFecha() {
            return NlFecha;
        }
    }

    public AdaptadorRecycler(TIPOADAPTER tipo, Manga[] mangas) {
        this.mangas = mangas;
        this.tipo = tipo;
    }

    public void ActualizarMangas(TIPOADAPTER tipo, Manga[] mangas) {
        this.mangas = mangas;
        this.tipo = tipo;
    }

    /*
    Se infla la vista del item
     */
    @NonNull
    @Override
    public AdaptadorRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(tipo == TIPOADAPTER.ScrollingActivity ? R.layout.mangaitem :
                                tipo == TIPOADAPTER.AddManga ? R.layout.mangaitemadd :
                                        R.layout.nuevos_lanzamientos_item,
                        parent, false);
        return new ViewHolder(view);
    }

    /*
    Se proporcionan datos a la vista del item
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull AdaptadorRecycler.ViewHolder holder, int position) {
        switch (tipo)
        {
            case AddManga:
                holder.getNombreAddManga().setText(mangas[position].getNombre());
                break;
            case ScrollingActivity:
                holder.getNombre().setText(mangas[position].getNombre());
                holder.getTomosEditados().setText(Integer.toString(mangas[position].getTomosEditados()));
                holder.getTomosEnPreparacion().setText(Integer.toString(mangas[position].getTomosEnPreparacion()));
                holder.getTomosNoEditados().setText(Integer.toString(mangas[position].getTomosNoEditados()));
                CrearListenersTomosComprados(holder, mangas[position]);
                break;
            case NuevosLanzamientosActivity:
                holder.getNlNombre().setText(mangas[position].getNombre());
                holder.getNlFecha().setText(mangas[position].getFecha());
                break;
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void CrearListenersTomosComprados(@NotNull ViewHolder holder, Manga mangas1) {
        holder.getTomosComprados().setText(Integer.toString(mangas1.getTomosComprados()));

        holder.getBtnMasUno().setOnClickListener(v -> {
            try {
                mangas1.SumaRestaUno(true);
                //Pruebas.getMangasAdded().removeIf(m -> m.getNombre().equals(mangas1));
                //Pruebas.getMangasAdded().add(mangas1);
                AddedMangasDB.ActualizarManga(mangas1);
                notifyDataSetChanged();
            } catch (NumeroMangasException e) {
                e.printStackTrace();
            }
        });
        holder.getBtnMenosUno().setOnClickListener(v -> {
            try {
                mangas1.SumaRestaUno(false);
                //Pruebas.getMangasAdded().removeIf(m -> m.getNombre().equals(mangas1));
                //Pruebas.getMangasAdded().add(mangas1);
                AddedMangasDB.ActualizarManga(mangas1);
                notifyDataSetChanged();
            } catch (NumeroMangasException e) {
                e.printStackTrace();
            }
        });
        holder.getBtnEditar().setOnClickListener(v -> {
            CambioActividades.CambioActualizarManga(v.getContext(), "ScrollingActivity",
                    mangas1);
        });
    }

    @Override
    public int getItemCount() {
        return mangas.length;
    }
}
