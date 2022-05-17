package com.example.mangatracker.webservice;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mangatracker.clases.LogManga;
import com.example.mangatracker.constantes.Constantes;
import com.example.mangatracker.db.AddedMangasDB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import operaciones.Transmision_WS;

public class WSLogs {
    static String json;
    static String datos;
    final static String TAG = Constantes.TAG_APP + "WSL";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void EnviarLogs(Context ctx)
    {
        try {
            AddedMangasDB.InstaciarBD(null);
            List<LogManga> listaLog = AddedMangasDB.ObtenerLogs();

            if (listaLog.size() == 0) return;

            //String datos = GenerarJson(listaLog);
//            if(EnviarJson(json))
//            {
//                AddedMangasDB.BorrarLogs();
//            }else{
            // crear fichero de logs
//            AddedMangasDB.InsertarLog("Logs_WS", "1.0",
//                    "Error al llamar WS. Json: \n" + json);
//
//            Log.d(TAG, "Error al llamar WS. Json: \n" + json);
            //}
            GenerarFichero(ctx, listaLog);


        }catch(Exception e)
        {
            AddedMangasDB.InsertarLog("Logs_WS", "1.0",
                    "Error en el proceso de enviar logs" + json);
            System.out.println("Error en el proceso de enviar logs");
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void GenerarFichero(Context ctx, List<LogManga> listaLog) {
        datos = "";

        //listaLog.forEach(f -> datos += f.StringFichero());
        for(LogManga log : listaLog)
        {
            datos += log.StringFichero();
        }

        try(FileOutputStream out = ctx.openFileOutput(Constantes.ficheroLogs,
                Context.MODE_APPEND)){
            out.write(datos.getBytes());
            AddedMangasDB.BorrarLogs();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
            return (boolean) Transmision_WS.EnviarDatos(json, Transmision_WS.WSLOG, "POST");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
