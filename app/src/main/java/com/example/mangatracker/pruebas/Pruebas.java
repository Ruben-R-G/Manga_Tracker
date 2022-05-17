package com.example.mangatracker.pruebas;

import com.example.mangatracker.clases.Manga;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pruebas {
    static Manga[] mangas;
    public static Manga[] getMangas() {
        return mangas;
    }

    static Set<Manga> mangasAdded = new HashSet<>();
    public static Set<Manga> getMangasAdded()
    {
        return mangasAdded;
    }

    public static void llenarMangas()
    {

        mangas = new Manga[]{
                new Manga("prueba 1", 0, 3, 0),
                new Manga("prueba 2", 3, 2, 1),
                new Manga("prueba 3", 6, 1, 2),
                new Manga("prueba 4", 9, 0, 3, 3)
        };
    }

    public static boolean addManga(Manga manga)
    {
        return mangasAdded.add(manga);
    }

    public static Manga[] PruebaNuevosLanzamientos()
    {
//        mangas = new Manga[]{
//                new Manga(1,"prueba 1", "12/03/21"),
//                new Manga(2,"prueba 2", "12/03/21"),
//                new Manga(3,"prueba 3", "12/03/21"),
//                new Manga(4,"prueba 4", "14/06/97")
//        };

        return null;
    }
}
