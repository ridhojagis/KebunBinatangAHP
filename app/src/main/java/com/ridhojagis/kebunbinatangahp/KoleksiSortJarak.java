package com.ridhojagis.kebunbinatangahp;

import java.util.Comparator;

public class KoleksiSortJarak implements Comparator<Koleksi> {
    @Override
    public int compare(Koleksi koleksi1, Koleksi koleksi2) {
        // Membandingkan berdasarkan jarak
        if (koleksi1.getJarak() < koleksi2.getJarak() ) {
            return -1; // Mengembalikan nilai negatif jika jarak koleksi1 lebih kecil
        } else if (koleksi1.getJarak() > koleksi2.getJarak() ) {
            return 1; // Mengembalikan nilai positif jika jarak koleksi1 lebih besar
        } else {
            return 0; // Mengembalikan nilai 0 jika jarak kedua koleksi sama
        }
    }
}
