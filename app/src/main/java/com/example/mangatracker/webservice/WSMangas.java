package com.example.mangatracker.webservice;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mangatracker.clases.LogManga;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.db.AddedMangasDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import clases.MangaDatos;
import operaciones.Transmision_WS;

public class WSMangas {
    static String json;
    final static String TAG = Constantes.TAG_APP + "WSL";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<Manga> EnviarPeticion(List<Manga> listaManga)
    {
        try{
          
            if(listaManga.size() == 0) return listaManga;

            String json = GenerarJson(listaManga);
            
            listaManga = EnviarJson(json);
            
            if(listaManga != null)
            {
                return listaManga;

            }else{
                AddedMangasDB.InsertarLog("Manga_WS", "1.0",
                        "Error al llamar WS. Json: \n" + json);

                Log.d(TAG, "Error al llamar WS. Json: \n" + json);
            }

        }catch(Exception e)
        {
            System.out.println("Error en el proceso de enviar mangas");
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static String GenerarJson(List<Manga> listaManga) {
        json = "[";
        listaManga.forEach(lm -> json += lm.toString());
        json = json.substring(0, json.length() - 1); //Quitamos la ultima coma
        json += "]";

        Log.d(TAG, json);

        return json;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Manga> EnviarJson(String json) {
        //Usamos metodo put porque con get no funciona
        try {
            List<MangaDatos> datos = (List<MangaDatos>)
                    Transmision_WS.EnviarDatos(json, Transmision_WS.WSMANGA, "PUT");

            List<Manga> mangas = new ArrayList<>();
            datos.forEach(m -> {
                Manga m2 = new Manga(m.getId(), m.getNombre(), m.getTomosEditados()
                ,m.getTomosEnPreparacion(),m.getTomosNoEditados(), m.getTomosComprados(),
                        m.getFecha(), m.getTerminado(), m.getFavorito(),
                        m.getDroppeado());
                mangas.add(m2);
            });

            return mangas;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
