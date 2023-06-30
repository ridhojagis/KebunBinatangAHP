package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.TextView;
import android.widget.Toast;

public class FormActivity extends AppCompatActivity {
    private double[][] pairwiseMatrix;
    private String HELP_MESSAGE="Rute Navigasi pada Aplikasi Kebun Binatang dibuat dengan mempertimbangkan beberapa kriteria:\n\n"+
            "1) Jarak, jarak antara pengunjung dengan lokasi tujuan.\n 2) Jenis Koleksi, prioritas jenis seperti Mamalia, " +
            "Aves, Reptil, dan Fasilitas \n 3) Status Buka, ketersediaan jam buka fasilitas kebun binatang.\n 4)Minat, tingkat kepeminatan fasilitas atau hewan." +
            "\n\nAnda dapat menggunakan pengaturan bobot default atau mengaturnya sesuai dengan preferensi anda." +
            "\n\nKeterangan Nilai Bobot:\n1 -Kedua elemen sama penting\n3 -Sedikit lebih penting dari elemen lainnya\n5 -Lebih penting dari elemen lainnya" +
            "\n7 -Sangat penting dari elemen lainnya\n2,4,6 -Merupakan nilai tengah di antara dua tingkat kepentingan yang berdekatan";

    RadioGroup importanceRadioGroup1;
    RadioGroup importanceRadioGroup2;
    RadioGroup importanceRadioGroup3;
    RadioGroup importanceRadioGroup4;
    RadioGroup importanceRadioGroup5;
    RadioGroup importanceRadioGroup6;

    SeekBar ratingSeekBar1;
    SeekBar ratingSeekBar2;
    SeekBar ratingSeekBar3;
    SeekBar ratingSeekBar4;
    SeekBar ratingSeekBar5;
    SeekBar ratingSeekBar6;


    Button saveButton;
    Button backButton;
    ImageButton helpButton;

    private int radioButton1Id;
    private int radioButton2Id;
    private int radioButton3Id;
    private int radioButton4Id;
    private int radioButton5Id;
    private int radioButton6Id;

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

        importanceRadioGroup1 = findViewById(R.id.importanceRadioGroup1);
        importanceRadioGroup2 = findViewById(R.id.importanceRadioGroup2);
        importanceRadioGroup3 = findViewById(R.id.importanceRadioGroup3);
        importanceRadioGroup4 = findViewById(R.id.importanceRadioGroup4);
        importanceRadioGroup5 = findViewById(R.id.importanceRadioGroup5);
        importanceRadioGroup6 = findViewById(R.id.importanceRadioGroup6);

        ratingSeekBar1 = findViewById(R.id.ratingSeekBar1);
        ratingSeekBar2 = findViewById(R.id.ratingSeekBar2);
        ratingSeekBar3 = findViewById(R.id.ratingSeekBar3);
        ratingSeekBar4 = findViewById(R.id.ratingSeekBar4);
        ratingSeekBar5 = findViewById(R.id.ratingSeekBar5);
        ratingSeekBar6 = findViewById(R.id.ratingSeekBar6);

        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        helpButton = findViewById(R.id.btnHelp);

        // Set save button disabled
        saveButton.setEnabled(false);

        importanceRadioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton1Id = importanceRadioGroup1.getCheckedRadioButtonId();
                checkSaveButtonEnabled();
            }
        });
        importanceRadioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton2Id = importanceRadioGroup2.getCheckedRadioButtonId();
                checkSaveButtonEnabled();
            }
        });
        importanceRadioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton3Id = importanceRadioGroup3.getCheckedRadioButtonId();
                checkSaveButtonEnabled();
            }
        });
        importanceRadioGroup4.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton4Id = importanceRadioGroup4.getCheckedRadioButtonId();
                checkSaveButtonEnabled();
            }
        });
        importanceRadioGroup5.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton5Id = importanceRadioGroup5.getCheckedRadioButtonId();
                checkSaveButtonEnabled();
            }
        });
        importanceRadioGroup6.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton6Id = importanceRadioGroup6.getCheckedRadioButtonId();
                checkSaveButtonEnabled();
            }
        });

        ratingSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue1 = ratingSeekBar1.getProgress() + 1;
                checkSaveButtonEnabled();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });
        ratingSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue2 = ratingSeekBar2.getProgress() + 1;
                checkSaveButtonEnabled();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });
        ratingSeekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue3 = ratingSeekBar3.getProgress() + 1;
                checkSaveButtonEnabled();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });
        ratingSeekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue4 = ratingSeekBar4.getProgress() + 1;
                checkSaveButtonEnabled();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });
        ratingSeekBar5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue5 = ratingSeekBar5.getProgress() + 1;
                checkSaveButtonEnabled();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });
        ratingSeekBar6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue6 = ratingSeekBar6.getProgress() + 1;
                checkSaveButtonEnabled();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this implementation
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThis("Bantuan",HELP_MESSAGE);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mendapatkan teks RadioButton yang dipilih
                RadioButton selectedRadioButton1 = findViewById(radioButton1Id);
                RadioButton selectedRadioButton2 = findViewById(radioButton2Id);
                RadioButton selectedRadioButton3 = findViewById(radioButton3Id);
                RadioButton selectedRadioButton4 = findViewById(radioButton4Id);
                RadioButton selectedRadioButton5 = findViewById(radioButton5Id);
                RadioButton selectedRadioButton6 = findViewById(radioButton6Id);

                // Mengambil value dalam bentuk string
                String radioButton1Text = selectedRadioButton1.getText().toString();
                String radioButton2Text = selectedRadioButton2.getText().toString();
                String radioButton3Text = selectedRadioButton3.getText().toString();
                String radioButton4Text = selectedRadioButton4.getText().toString();
                String radioButton5Text = selectedRadioButton5.getText().toString();
                String radioButton6Text = selectedRadioButton6.getText().toString();

                // Contoh: Menampilkan nilai-nilai pada logcat
                Log.d("FormActivity", "RadioButton 1: " + radioButton1Text);
                Log.d("FormActivity", "RadioButton 2: " + radioButton2Text);
                Log.d("FormActivity", "RadioButton 3: " + radioButton3Text);
                Log.d("FormActivity", "RadioButton 4: " + radioButton4Text);
                Log.d("FormActivity", "RadioButton 5: " + radioButton5Text);
                Log.d("FormActivity", "RadioButton 6: " + radioButton6Text);

                Log.d("FormActivity", "SeekBar 1: " + seekBarValue1);
                Log.d("FormActivity", "SeekBar 2: " + seekBarValue2);
                Log.d("FormActivity", "SeekBar 3: " + seekBarValue3);
                Log.d("FormActivity", "SeekBar 4: " + seekBarValue4);
                Log.d("FormActivity", "SeekBar 5: " + seekBarValue5);
                Log.d("FormActivity", "SeekBar 6: " + seekBarValue6);

                String toastMessage = "Bobot Kriteria telah disimpan";
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSaveButtonEnabled() {
        boolean isRadioGroup1Selected = importanceRadioGroup1.getCheckedRadioButtonId() != -1;
        boolean isRadioGroup2Selected = importanceRadioGroup2.getCheckedRadioButtonId() != -1;
        boolean isRadioGroup3Selected = importanceRadioGroup3.getCheckedRadioButtonId() != -1;
        boolean isRadioGroup4Selected = importanceRadioGroup4.getCheckedRadioButtonId() != -1;
        boolean isRadioGroup5Selected = importanceRadioGroup5.getCheckedRadioButtonId() != -1;
        boolean isRadioGroup6Selected = importanceRadioGroup6.getCheckedRadioButtonId() != -1;

        boolean isSeekBar1Selected = ratingSeekBar1.getProgress() > 0;
        boolean isSeekBar2Selected = ratingSeekBar2.getProgress() > 0;
        boolean isSeekBar3Selected = ratingSeekBar3.getProgress() > 0;
        boolean isSeekBar4Selected = ratingSeekBar4.getProgress() > 0;
        boolean isSeekBar5Selected = ratingSeekBar5.getProgress() > 0;
        boolean isSeekBar6Selected = ratingSeekBar6.getProgress() > 0;

        saveButton.setEnabled(isRadioGroup1Selected && isSeekBar1Selected &&
                isRadioGroup2Selected && isSeekBar2Selected &&
                isRadioGroup3Selected && isSeekBar3Selected &&
                isRadioGroup4Selected && isSeekBar4Selected &&
                isRadioGroup5Selected && isSeekBar5Selected &&
                isRadioGroup6Selected && isSeekBar6Selected);
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