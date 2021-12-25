package com.example.mangatracker.casosuso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.mangatracker.ActualizarMangaActivity;
import com.example.mangatracker.AddMangaActivity;
import com.example.mangatracker.NuevosLanzamientosActivity;
import com.example.mangatracker.PreferenciasActivity;
import com.example.mangatracker.PruebasActivity;
import com.example.mangatracker.ScrollingActivity;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.pruebas.Pruebas;

public class CambioActividades {

    public static void CambioAddManga(Context ctx){
        ctx.startActivity(new Intent(ctx, AddMangaActivity.class));
    }

    public static void CambioActualizarManga(Context ctx, String nombreActivity, Manga manga)
    {
        ctx.startActivity(new Intent(ctx, ActualizarMangaActivity.class)
                .putExtra("actividad padre", nombreActivity)
                .putExtra("manga", manga));
    }

    public static void CambioNuevosLanzamientos(Context ctx)
    {
        ctx.startActivity(new Intent(ctx, NuevosLanzamientosActivity.class));
    }

    public static void CambioMain(Context ctx)
    {
        ctx.startActivity(new Intent(ctx, ScrollingActivity.class));
    }

    public static void CambioPreferencias(Context ctx)
    {
        ctx.startActivity(new Intent(ctx, PreferenciasActivity.class));
    }

    public static void CambioPruebas(Context ctx)
    {
        ctx.startActivity(new Intent(ctx, PruebasActivity.class));
    }
}
