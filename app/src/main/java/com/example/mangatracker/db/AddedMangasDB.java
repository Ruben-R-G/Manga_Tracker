package com.example.mangatracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.mangatracker.clases.LogManga;
import com.example.mangatracker.clases.Manga;
import com.example.mangatracker.constantes.Constantes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddedMangasDB extends SQLiteOpenHelper {
    public static final String NOMBRE_DB = "MangaTracker.db";
    public static final int VERSION_DB = 2;
    public static final String TAG = Constantes.TAG_APP + "BD";
    private static AddedMangasDB bd = null;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static SimpleDateFormat fechaFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSS");

    @Override
    public void onCreate(SQLiteDatabase db) {
        CrearTablas(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "CREATE TABLE LOG_MANGAS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", CODIGO_TRAMO TEXT" +
                ", PASO TEXT" +
                ", EVENTO TEXT" +
                ", FECHA TEXT)";
        db.execSQL(sql);
    }

    public AddedMangasDB(@Nullable @org.jetbrains.annotations.Nullable Context context) {
        super(context, NOMBRE_DB, null, VERSION_DB);
    }

    public static AddedMangasDB getContext()
    {
        return bd;
    }

    public static void InstaciarBD(Context ctx) {
        if (bd == null)
            bd = new AddedMangasDB(ctx);
    }

    private AddedMangasDB() {
        super(null, NOMBRE_DB, null, VERSION_DB);
    }

    private void CrearTablas(SQLiteDatabase db) {
        String sql = "CREATE TABLE MANGAS_ADDED(" +
                "ID INTEGER PRIMARY KEY," +
                "NOMBRE TEXT," +
                "TOMOS_EDITADOS INTEGER," +
                "TOMOS_EN_PREPARACION INTEGER," +
                "TOMOS_NO_EDITADOS INTEGER," +
                "TOMOS_COMPRADOS INTEGER," +
                "SIGUIENTE_LANZAMIENTO TEXT," +
                "TERMINADO INTEGER," +
                "FAVORITO INTEGER," +
                "DROPPEADO INTEGER" +
                ")";
        db.execSQL(sql);
        sql = "CREATE TABLE TOMOS_POR_COMPRAR(" +
                "ID_MANGA INTEGER," +
                "NUM_TOMO INTEGER" +
                ")";
        db.execSQL(sql);

    }

    public static void InsertarManga(Manga m) {
        m.setNombre(m.getNombre().replace("'", "''"));
        String sql = String.format(
                "INSERT INTO MANGAS_ADDED VALUES (%d, '%s', %d, %d, %d, %d, %s, %d, 0, 0)",
                m.getId(),
                m.getNombre(),
                m.getTomosEditados(),
                m.getTomosEnPreparacion(),
                m.getTomosNoEditados(),
                m.getTomosComprados(),
                m.getFecha() == null ? "NULL" : "'"+ m.getFecha() +"'",
                m.getTerminado()
        );

        bd.getWritableDatabase().execSQL(sql);
    }

    public static void EliminarManga(int id) {
        String sql = "DELETE FROM MANGAS_ADDED WHERE ID = " + id;
        bd.getWritableDatabase().execSQL(sql);
    }

    public static void ActualizarManga(Manga m) {
        String sql = String.format(
                "UPDATE MANGAS_ADDED SET " +
                        "TOMOS_EDITADOS = %d," +
                        "TOMOS_EN_PREPARACION = %d," +
                        "TOMOS_NO_EDITADOS = %d," +
                        "TOMOS_COMPRADOS = %d," +
                        "TERMINADO = %d" +
                        " WHERE ID = %d",
                m.getTomosEditados(),
                m.getTomosEnPreparacion(),
                m.getTomosNoEditados(),
                m.getTomosComprados(),
                m.getTerminado(),
                m.getId()
        );
        bd.getWritableDatabase().execSQL(sql);
    }

    public static void HacerFavorito(int id) {
        String sql = "UPDATE MANGAS_ADDED SET FAVORITO = 1 WHERE ID=" + id;
        bd.getWritableDatabase().execSQL(sql);
    }

    public static void HacerDroppeado(int id) {
        String sql = "UPDATE MANGAS_ADDED SET DROPPEADO = 1 WHERE ID=" + id;
        bd.getWritableDatabase().execSQL(sql);
    }

    public static void ActualizarFecha(int id, String nuevaFecha) {
        String sql = "UPDATE MANGAS_ADDED SET SIGUIENTE_LANZAMIENTO = " + (nuevaFecha != null
                ? "'" + nuevaFecha + "'" : "NULL")
                + " WHERE ID=" + id;
        bd.getWritableDatabase().execSQL(sql);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Manga ObtenerUno(int id) {
        String sql = "SELECT * FROM MANGAS_ADDED WHERE ID = " + id;
        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if (c.moveToFirst()) //Si hay al menos un dato
        {
                String nombre = c.getString(1);
                int edit = c.getInt(2);
                int prep = c.getInt(3);
                int noedit = c.getInt(4);
                int comp = c.getInt(5);
                String fecha = c.getString(6);
                int terminado = c.getInt(7);
                int fav = c.getInt(8);
                int drop = c.getInt(9);

                c.close();
                return new Manga(id, nombre, edit, prep, noedit, comp, fecha, terminado, fav, drop);
        }
        c.close();
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Manga[] ObtenerTodos() {
        List<Manga> listado = new ArrayList<>();
        String sql = "SELECT * FROM MANGAS_ADDED ORDER BY " +
                "TERMINADO ASC, FAVORITO DESC, (TOMOS_EDITADOS - TOMOS_COMPRADOS) DESC, TOMOS_EN_PREPARACION DESC";
        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if (c.moveToFirst()) //Si hay al menos un dato
        {
            do {
                int id = c.getInt(0);
                String nombre = c.getString(1);
                int edit = c.getInt(2);
                int prep = c.getInt(3);
                int noedit = c.getInt(4);
                int comp = c.getInt(5);
                String fecha = c.getString(6);
                int terminado = c.getInt(7);
                int fav = c.getInt(8);
                int drop = c.getInt(9);

                listado.add(new Manga(id, nombre, edit, prep, noedit, comp, fecha, terminado, fav, drop));
            } while (c.moveToNext());
        }
        c.close();
        //OrdenarMangas(listado);
        return listado.size() > 0 ? listado.toArray(new Manga[]{}) : new Manga[]{};
    }

    /**
     * Obtiene los mangas que tienen proxima fecha de lanzamiento
     * @param actualizar False: Obtiene los datos. True: Obtiene los datos y actualiza las fechas
     * @return El listado de mangas
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Manga[] ObtenerNuevosLanzamientos(boolean actualizar) {
        List<Manga> listado = new ArrayList<>();
        String sql = "SELECT * FROM MANGAS_ADDED WHERE SIGUIENTE_LANZAMIENTO IS NOT NULL ORDER BY FAVORITO DESC";
        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if (c.moveToFirst()) //Si hay al menos un dato
        {
            do {
                int id = c.getInt(0);
                String nombre = c.getString(1);
                String fecha = c.getString(6);
                listado.add(new Manga(id, nombre, fecha));
            } while (c.moveToNext());
        }
        c.close();

        if(actualizar)
        {
            ComprobarFechasManga(listado);
        }

        OrdenarMangas(listado);
        return listado.size() > 0 ? listado.toArray(new Manga[]{}) : new Manga[]{};

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void OrdenarMangas(List<Manga> listado) {
        listado.sort((m1, m2) -> {
            try {
                if(m1.getFecha() == null || m2.getFecha() == null){
                    if(m1.getFecha() == null && m2.getFecha() == null)
                        return 0;
                    if(m1.getFecha() == null && m2.getFecha() != null)
                        return 1;
                    if(m1.getFecha() != null && m2.getFecha() == null)
                        return -1;
                }
                return simpleDateFormat.parse(m1.getFecha()).before(simpleDateFormat.parse(m2.getFecha())) ? -1:
                        simpleDateFormat.parse(m1.getFecha()).after(simpleDateFormat.parse(m2.getFecha())) ? 1:
                                0;
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void ComprobarFechasManga(List<Manga> listado) {
        Date FechaManga;
        List<Integer> idMangasEliminar = new ArrayList<>();
        try {
            for (Manga m : listado) {
                FechaManga = simpleDateFormat.parse(m.getFecha());
                Calendar hoy = Calendar.getInstance();
                //hoy.add(Calendar.DAY_OF_YEAR, -1);

                if (FechaManga.before(hoy.getTime())) {
                    ActualizarFecha(m.getId(), null);
                    //listado.removeIf(manga-> m.getId() == manga.getId());
                    idMangasEliminar.add(m.getId());
                }
            }


            listado.removeIf(manga -> idMangasEliminar.contains(manga.getId()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public static Manga[] ActualizarNuevosLanzamientos() {
        List<Manga> listado = new ArrayList<>();
        String sql = "SELECT * FROM MANGAS_ADDED WHERE SIGUIENTE_LANZAMIENTO IS NULL AND TERMINADO = 0 ORDER BY FAVORITO DESC";
        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if (c.moveToFirst()) //Si hay al menos un dato
        {
            do {
                int id = c.getInt(0);
                String nombre = c.getString(1);
                String fecha = c.getString(6);
                int tomosComprados = c.getInt(5);
                listado.add(new Manga(id, nombre, fecha, tomosComprados));
            } while (c.moveToNext());
        }
        c.close();
        return listado.size() > 0 ? listado.toArray(new Manga[]{}) : new Manga[]{};

    }




    //region LOG
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void InsertarLog(String codigo, String paso, String evento)
    {
        try
        {

            evento = evento.replace('\'', '"');

            String sql = "INSERT INTO LOG_MANGAS (CODIGO_TRAMO, PASO, EVENTO, FECHA) VALUES(" +
                    "'" + codigo + "'" +
                    ",'" + paso + "'" +
                    ",'" + evento + "'" +
                    ",'" + fechaFormat.format(Calendar.getInstance().getTime()) + "'" +
                    ")";

            bd.getWritableDatabase().execSQL(sql);
        }catch (Exception e)
        {
            Log.d(TAG, "Error al insertar log. Evento: " + evento);
            evento = "Error de inserción en logs: Evento con algun caracter erróneo";

            String sql = "INSERT INTO LOG_MANGAS (CODIGO_TRAMO, PASO, EVENTO, FECHA) VALUES(" +
                    "'" + codigo + "'" +
                    ",'" + paso + "'" +
                    ",'" + evento + "'" +
                    ",'" + fechaFormat.format(Calendar.getInstance().getTime()) + "'" +
                    ")";

            bd.getWritableDatabase().execSQL(sql);
        }

    }
    
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<LogManga> ObtenerLogs()
    {
        List<LogManga> listado = new ArrayList<>();

        int cuenta = ObtenerNumLogs();
        int id = ObtenerPrimerIdLog();

        if(cuenta == -1 || id == -1)
        {
            InsertarLog(TAG + " - LOGS", "1.0", String.format(Locale.getDefault(),"Cuenta (%d) o id (%d) nulos", cuenta, id));
        }

        while(id <= cuenta)
        {
            if(!listado.addAll(ObtenerLogs(id)))
            {
                InsertarLog(TAG + " - LOGS", "1.1", "Error al obtener logs de la select");
            }

            id += 20;
        }

        return listado;
    }

    private static int ObtenerPrimerIdLog() {
        int primerId = -1;
        String sql = "SELECT ID FROM LOG_MANGAS ORDER BY ID";

        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if(c.moveToFirst())
        {
            primerId = c.getInt(0);
        }
        c.close();
        return primerId;
    }

    private static int ObtenerNumLogs() {
        int num = -1;
        String sql = "SELECT COUNT(ID) FROM LOG_MANGAS";

        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if(c.moveToFirst())
        {
            num = c.getInt(0);
        }
        c.close();

        return num;
    }

    public static List<LogManga> ObtenerLogs(int id)
    {

        String sql = "SELECT TOP 20 CODIGO_TRAMO, PASO, EVENTO, FECHA FROM LOG_MANGAS WHERE ID >= " + id + " ORDER BY ID";
        List<LogManga> listado = new ArrayList<>();

        Cursor c = bd.getReadableDatabase().rawQuery(sql, null);

        if (c.moveToFirst()) //Si hay al menos un dato
        {
            do {
                String codigo_tramo = c.getString(0);
                String paso = c.getString(1);
                String evento = c.getString(2);
                String fecha = c.getString(3);
                listado.add(new LogManga(paso, codigo_tramo, fecha, evento));
            } while (c.moveToNext());
        }
        c.close();

        return listado;
    }

    public static void BorrarLogs()
    {
        String sql = "DELETE FROM LOG_MANGAS";
        bd.getWritableDatabase().execSQL(sql);
    }
    //endRegion
}
