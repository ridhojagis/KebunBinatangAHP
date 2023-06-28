package com.ridhojagis.kebunbinatangahp;

import java.util.Comparator;

public class KoleksiComparator implements Comparator<Koleksi> {
    @Override
    public int compare(Koleksi koleksi1, Koleksi koleksi2) {
        // Membandingkan berdasarkan ahp_score
        if (koleksi1.getAhp_score() > koleksi2.getAhp_score()) {
            return -1; // Mengembalikan nilai negatif jika ahp_score koleksi1 lebih besar
        } else if (koleksi1.getAhp_score() < koleksi2.getAhp_score()) {
            return 1; // Mengembalikan nilai positif jika ahp_score koleksi1 lebih kecil
        } else {
            return 0; // Mengembalikan nilai 0 jika ahp_score kedua koleksi sama
        }
    }
}
