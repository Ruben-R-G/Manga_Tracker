package com.example.mangatracker.webservice;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.mangatracker.clases.LogManga;
import com.example.mangatracker.db.AddedMangasDB;

import java.io.IOException;
import java.util.List;

import operaciones.Transmision_WS;

public class WSLogs {
    static String json;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void EnviarLogs()
    {
        try{
            AddedMangasDB.InstaciarBD(null);
            List<LogManga> listaLog = AddedMangasDB.ObtenerLogs();

            if(listaLog.size() == 0) return;

            String json = GenerarJson(listaLog);
            if(EnviarJson(json))
            {
                AddedMangasDB.BorrarLogs();
            }

        }catch(Exception e)
        {
            System.out.println("Error en el proceso de enviar logs");
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static String GenerarJson(List<LogManga> listaLog) {
        json = "[\n";
        listaLog.forEach(lg -> json += listaLog.toString());
        json = json.substring(0, json.length() - 1);
        json += "]";

        return json;
    }


    private static boolean EnviarJson(String json) {

        try {
            return Transmision_WS.EnviarDatos(json);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
