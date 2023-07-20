package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

public class FormActivity extends AppCompatActivity {
    private double[][] pairwiseMatrix;
    private String HELP_MESSAGE="Rute Navigasi pada Aplikasi Kebun Binatang dibuat dengan mempertimbangkan beberapa kriteria:\n\n"+
            "1) Jarak, jarak antara pengunjung dengan objek sekitar.\n 2) Jenis Koleksi, prioritas jenis seperti Mamalia, " +
            "Aves, Reptil, dan Fasilitas \n 3) Status Buka, ketersediaan jam buka fasilitas kebun binatang.\n 4)Minat, tingkat kepeminatan fasilitas atau hewan." +
            "\n\nAnda dapat menggunakan pengaturan bobot default atau mengaturnya sesuai dengan preferensi anda." +
            "\n\nKeterangan Nilai Bobot:\n1 -Kedua elemen sama penting\n3 -Sedikit lebih penting dari elemen lainnya\n5 -Lebih penting dari elemen lainnya" +
            "\n7 -Sangat penting dari elemen lainnya\n9 -Ekstrim lebih penting dari elemen lainnya\nAnda juga dapat memilih nilai tengah di antara dua tingkat kepentingan yang berdekatan";
    private String SAVE_MESSAGE="Bobot kriteria telah berhasil diatur. Kembali ke Map dan mulai navigasi";

    SeekBar ratingSeekBar1;
    SeekBar ratingSeekBar2;
    SeekBar ratingSeekBar3;
    SeekBar ratingSeekBar4;
    SeekBar ratingSeekBar5;
    SeekBar ratingSeekBar6;


    Button saveButton;
    Button backButton;
    Button defaultButton;
    ImageButton helpButton;

    private int seekBarValue1;
    private int seekBarValue2;
    private int seekBarValue3;
    private int seekBarValue4;
    private int seekBarValue5;
    private int seekBarValue6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        // Inisiasi matrix AHP kriteria utama
        pairwiseMatrix = new double[][]{
                {1.0, 0.0, 0.0, 0.0}, // Matriks perbandingan kriteria jarak
                {0.0, 1.0, 0.0, 0.0}, // Matriks perbandingan kriteria jenis
                {0.0, 0.0, 1.0, 0.0}, // Matriks perbandingan kriteria status buka
                {0.0, 0.0, 0.0, 1.0}  // Matriks perbandingan kriteria minat
        };

        ratingSeekBar1 = findViewById(R.id.ratingSeekBar1);
        ratingSeekBar2 = findViewById(R.id.ratingSeekBar2);
        ratingSeekBar3 = findViewById(R.id.ratingSeekBar3);
        ratingSeekBar4 = findViewById(R.id.ratingSeekBar4);
        ratingSeekBar5 = findViewById(R.id.ratingSeekBar5);
        ratingSeekBar6 = findViewById(R.id.ratingSeekBar6);

        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        helpButton = findViewById(R.id.btnHelp);
        defaultButton = findViewById(R.id.defaultButton);


        // Set save button disabled
//        saveButton.setEnabled(false);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThis("Bantuan",HELP_MESSAGE);
            }
        });

        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Berhasil");
                builder.setMessage("Bobot Kriteria berhasil diatur ke Default. Kembali ke Map dan mulai navigasi");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pairwiseMatrix = new double[][]{
                                {1.0, 0.5, 7.0, 3.0},   // Matriks perbandingan kriteria jarak
                                {2.0, 1.0, 8.0, 3.0},   // Matriks perbandingan kriteria jenis
                                {0.1428571429, 0.125, 1.0, 0.2},  // Matriks perbandingan kriteria status buka
                                {0.3333333333, 0.3333333333, 5.0, 1.0}  // Matriks perbandingan kriteria minat
                        };
//                        pairwiseMatrix = new double[][]{
//                                {1.0, 3.0, 0.2, 3.0},   // Matriks perbandingan kriteria jarak
//                                {0.3333333333, 1.0, 0.1428571429, 1.0},   // Matriks perbandingan kriteria jenis
//                                {5.0, 7.0, 1.0, 7.0},  // Matriks perbandingan kriteria status buka
//                                {0.3333333333, 1.0, 0.1428571429, 1.0}  // Matriks perbandingan kriteria minat
//                        };
                        navigateToMapsActivity(pairwiseMatrix);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBarValue1 = ratingSeekBar1.getProgress() + 1;
                seekBarValue2 = ratingSeekBar2.getProgress() + 1;
                seekBarValue3 = ratingSeekBar3.getProgress() + 1;
                seekBarValue4 = ratingSeekBar4.getProgress() + 1;
                seekBarValue5 = ratingSeekBar5.getProgress() + 1;
                seekBarValue6 = ratingSeekBar6.getProgress() + 1;

                double weightJarak_Jenis = seekBarValue1;
                double weightJarak_Status = seekBarValue2;
                double weightJarak_Minat = seekBarValue3;
                double weightJenis_Status = seekBarValue4;
                double weightJenis_Minat = seekBarValue5;
                double weightStatus_Minat = seekBarValue6;

                // Menampilkan nilai-nilai pada logcat
                Log.d("FormActivity", "SeekBar 1: " + weightJarak_Jenis);

                setPairWiseMatrix(weightJarak_Jenis,weightJarak_Status,weightJarak_Minat,weightJenis_Status,weightJenis_Minat,weightStatus_Minat);

                double CR = consistencyRatioMatrix(pairwiseMatrix);

                if(CR <= 0.1){
                    String toastMessage = "Rasio Konsistensi: " + CR;
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Berhasil");
                    builder.setMessage(SAVE_MESSAGE);
                    Log.i("CR_PAIRWISE_CUSTOM", "CR konsisten = " + CR);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            navigateToMapsActivity(pairwiseMatrix);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    String toastMessage = "Rasio Konsistensi: " + CR;
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Alert");
                    builder.setMessage("Bobot kriteriamu tidak konsisten, silahkan atur kembali.");
                    Log.i("CR_PAIRWISE_CUSTOM", "CR tidak konsisten = " + CR);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private double consistencyRatioMatrix(double[][] pairwiseMatrix) {
        int matrix_size = pairwiseMatrix.length;
        double nilaiMatrix[][] = new double[matrix_size][matrix_size];

        //    Variabel matriks kriteria
        double sum_pairwiseMatrix_jarak = 0.0;
        double sum_pairwiseMatrix_jenis = 0.0;
        double sum_pairwiseMatrix_statusBuka = 0.0;
        double sum_pairwiseMatrix_minat = 0.0;
        double sum_nilai_jarak = 0.0;
        double sum_nilai_jenis = 0.0;
        double sum_nilai_statusBuka = 0.0;
        double sum_nilai_minat = 0.0;
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

        double prioritas_jarak = sum_nilai_jarak/matrix_size;
        double prioritas_jenis = sum_nilai_jenis/matrix_size;
        double prioritas_statusBuka = sum_nilai_statusBuka/matrix_size;
        double prioritas_minat = sum_nilai_minat/matrix_size;

        eigen_value_jarak = prioritas_jarak * sum_pairwiseMatrix_jarak;
        eigen_value_jenis = prioritas_jenis * sum_pairwiseMatrix_jenis;
        eigen_value_statusBuka = prioritas_statusBuka * sum_pairwiseMatrix_statusBuka;
        eigen_value_minat = prioritas_minat * sum_pairwiseMatrix_minat;

        total_eigen_value = eigen_value_jarak + eigen_value_jenis + eigen_value_statusBuka + eigen_value_minat;

        double CI = (total_eigen_value-matrix_size)/(matrix_size-1);
        double RI = 0.9;
        double CR = CI/RI;

        return CR;
    }

    private void navigateToMapsActivity(double[][] pairwiseMatrix) {
        // Mendefinisikan Intent
        Intent intent = new Intent(FormActivity.this, MapsActivity.class);

        // Mengirim dengan Bundle
        Bundle extras = new Bundle();
        extras.putSerializable("pairwiseMatrix", pairwiseMatrix);

        // Menyimpan Bundle ke dalam Intent
        intent.putExtras(extras);

        // Memulai Activity Maps dengan Intent yang telah dikonfigurasi
        startActivity(intent);
    }

    private void setPairWiseMatrix(double weightJarak_jenis, double weightJarak_status, double weightJarak_minat, double weightJenis_status, double weightJenis_minat, double weightStatus_minat) {

        // Set bobot perbandingan jarak dan jenis koleksi
        if (weightJarak_jenis >= 1 && weightJarak_jenis <= 9) {
            pairwiseMatrix[0][1] = 10 - weightJarak_jenis;
            pairwiseMatrix[1][0] = 1.0 / pairwiseMatrix[0][1];
        } else if (weightJarak_jenis >= 10 && weightJarak_jenis <= 17) {
            pairwiseMatrix[0][1] = 1.0 / (weightJarak_jenis - 8);
            pairwiseMatrix[1][0] = 1.0 / pairwiseMatrix[0][1];
        }

        // Set bobot perbandingan jarak dan status buka
        if (weightJarak_status >= 1 && weightJarak_status <= 9) {
            pairwiseMatrix[0][2] = 10 - weightJarak_status;
            pairwiseMatrix[2][0] = 1.0 / pairwiseMatrix[0][2];
        } else if (weightJarak_status >= 10 && weightJarak_status <= 17) {
            pairwiseMatrix[0][2] = 1.0 / (weightJarak_status - 8);
            pairwiseMatrix[2][0] = 1.0 / pairwiseMatrix[0][2];
        }

        // Set bobot perbandingan jarak dan minat
        if (weightJarak_minat >= 1 && weightJarak_minat <= 9) {
            pairwiseMatrix[0][3] = 10 - weightJarak_minat;
            pairwiseMatrix[3][0] = 1.0 / pairwiseMatrix[0][3];
        } else if (weightJarak_minat >= 10 && weightJarak_minat <= 17) {
            pairwiseMatrix[0][3] = 1.0 / (weightJarak_minat - 8);
            pairwiseMatrix[3][0] = 1.0 / pairwiseMatrix[0][3];
        }

        // Set bobot perbandingan jenis koleksi dan status buka
        if (weightJenis_status >= 1 && weightJenis_status <= 9) {
            pairwiseMatrix[1][2] = 10 - weightJenis_status;
            pairwiseMatrix[2][1] = 1.0 / pairwiseMatrix[1][2];
        } else if (weightJenis_status >= 10 && weightJenis_status <= 17) {
            pairwiseMatrix[1][2] = 1.0 / (weightJenis_status - 8);
            pairwiseMatrix[2][1] = 1.0 / pairwiseMatrix[1][2];
        }

        // Set bobot perbandingan jenis koleksi dan minat
        if (weightJenis_minat >= 1 && weightJenis_minat <= 9) {
            pairwiseMatrix[1][3] = 10 - weightJenis_minat;
            pairwiseMatrix[3][1] = 1.0 / pairwiseMatrix[1][3];
        } else if (weightJenis_minat >= 10 && weightJenis_minat <= 17) {
            pairwiseMatrix[1][3] = 1.0 / (weightJenis_minat - 8);
            pairwiseMatrix[3][1] = 1.0 / pairwiseMatrix[1][3];
        }

        // Set bobot perbandingan status buka dan minat
        if (weightStatus_minat >= 1 && weightStatus_minat <= 9) {
            pairwiseMatrix[2][3] = 10 - weightStatus_minat;
            pairwiseMatrix[3][2] = 1.0 / pairwiseMatrix[2][3];
        } else if (weightStatus_minat >= 10 && weightStatus_minat <= 17) {
            pairwiseMatrix[2][3] = 1.0 / (weightStatus_minat - 8);
            pairwiseMatrix[3][2] = 1.0 / pairwiseMatrix[2][3];
        }

        int matrix_size = pairwiseMatrix.length;
        for(int i=0;i<matrix_size;i++) {
            for(int j=0;j<matrix_size;j++) {
                Log.i("INITIAL_MATRIX", "index " + i + "," +j + " " + pairwiseMatrix[i][j]);
            }
        }
    }

    private void showThis(String title, String help_message) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(help_message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}