package com.ridhojagis.kebunbinatangahp;

public class AHP {
    double[][] pairwiseMatrix = {
            {1.0, 3.0, 0.2, 5.0},   // Matriks perbandingan kriteria jarak
            {0.3333333333, 1.0,	0.1428571429, 3.0},   // Matriks perbandingan kriteria jenis
            {5.0, 7.0, 1.0, 7.0},  // Matriks perbandingan kriteria status buka
            {0.2, 0.3333333333, 0.1428571429, 1.0}  // Matriks perbandingan kriteria minat
    };

    int matrix_size = pairwiseMatrix.length;
    double nilaiMatrix[][] = new double[matrix_size][matrix_size];

    double sum_pairwiseMatrix_jarak = 0.0;
    double sum_pairwiseMatrix_jenis = 0.0;
    double sum_pairwiseMatrix_statusBuka = 0.0;
    double sum_pairwiseMatrix_minat = 0.0;
    double sum_nilai_jarak = 0.0;
    double sum_nilai_jenis = 0.0;
    double sum_nilai_statusBuka = 0.0;
    double sum_nilai_minat = 0.0;
    double prioritas_jarak = 0.0;
    double prioritas_jenis = 0.0;
    double prioritas_statusBuka = 0.0;
    double prioritas_minat = 0.0;
    double eigen_value_jarak = 0.0;
    double eigen_value_jenis = 0.0;
    double eigen_value_statusBuka = 0.0;
    double eigen_value_minat = 0.0;
    double total_eigen_value = 0.0;

    for(int i=0;i<matrix_size;i++) {
        for(int j=0;j<matrix_size;j++) {
            if(j == 0) {
                sum_pairwiseMatrix_jarak += pairwiseMatrix[i][j];
            }
            else if(j == 1) {
                sum_pairwiseMatrix_jenis += pairwiseMatrix[i][j];
            }
            else if(j == 2) {
                sum_pairwiseMatrix_statusBuka += pairwiseMatrix[i][j];
            }
            else if(j == 3) {
                sum_pairwiseMatrix_minat += pairwiseMatrix[i][j];
            }
        }
    }

    for(int i=0;i<matrix_size;i++) {
        for(int j=0;j<matrix_size;j++) {
            if(j == 0) {
                nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_jarak;
            }
            else if(j == 1) {
                nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_jenis;
            }
            else if(j == 2) {
                nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_statusBuka;
            }
            else if(j == 3) {
                nilaiMatrix[i][j] = pairwiseMatrix[i][j]/sum_pairwiseMatrix_minat;
            }
        }
    }

    for(int i=0;i<matrix_size;i++) {
        for(int j=0;j<matrix_size;j++) {
            if(i == 0) {
                sum_nilai_jarak += nilaiMatrix[i][j];
            }
            else if(i == 1) {
                sum_nilai_jenis += nilaiMatrix[i][j];
            }
            else if(i == 2) {
                sum_nilai_statusBuka += nilaiMatrix[i][j];
            }
            else if(i == 3) {
                sum_nilai_minat += nilaiMatrix[i][j];
            }
        }
    }

    prioritas_jarak = sum_nilai_jarak/matrix_size;
    prioritas_jenis = sum_nilai_jenis/matrix_size;
    prioritas_statusBuka = sum_nilai_statusBuka/matrix_size;
    prioritas_minat = sum_nilai_minat/matrix_size;

    eigen_value_jarak = prioritas_jarak * sum_pairwiseMatrix_jarak;
    eigen_value_jenis = prioritas_jenis * sum_pairwiseMatrix_jenis;
    eigen_value_statusBuka = prioritas_statusBuka * sum_pairwiseMatrix_statusBuka;
    eigen_value_minat = prioritas_minat * sum_pairwiseMatrix_minat;

    total_eigen_value = eigen_value_jarak + eigen_value_jenis + eigen_value_statusBuka + eigen_value_minat;

}
