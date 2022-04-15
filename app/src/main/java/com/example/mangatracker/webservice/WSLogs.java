package com.example.mangatracker.webservice;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mangatracker.clases.LogManga;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.db.AddedMangasDB;

import java.io.IOException;
import java.util.List;

import operaciones.Transmision_WS;

public class WSLogs {
    static String json;
    final static String TAG = Constantes.TAG_APP + "WSL";

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
            }else{
                AddedMangasDB.InsertarLog("Logs_WS", "1.0",
                        "Error al llamar WS. Json: \n" + json);

                Log.d(TAG, "Error al llamar WS. Json: \n" + json);
            }

        }catch(Exception e)
        {
            System.out.println("Error en el proceso de enviar logs");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static String GenerarJson(List<LogManga> listaLog) {
        json = "[\n";
        listaLog.forEach(lg -> json += lg.toString());
        json = json.substring(0, json.length() - 2); //Quitamos la ultima coma
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
