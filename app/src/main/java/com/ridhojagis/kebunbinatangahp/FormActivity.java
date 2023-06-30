package com.ridhojagis.kebunbinatangahp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class FormActivity extends AppCompatActivity {
    private double[][] pairwiseMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        RadioGroup importanceRadioGroup1 = findViewById(R.id.importanceRadioGroup1);
        RadioGroup importanceRadioGroup2 = findViewById(R.id.importanceRadioGroup2);
        RadioGroup importanceRadioGroup3 = findViewById(R.id.importanceRadioGroup3);
        RadioGroup importanceRadioGroup4 = findViewById(R.id.importanceRadioGroup4);
        RadioGroup importanceRadioGroup5 = findViewById(R.id.importanceRadioGroup5);
        RadioGroup importanceRadioGroup6 = findViewById(R.id.importanceRadioGroup6);

        SeekBar ratingSeekBar1 = findViewById(R.id.ratingSeekBar1);
        SeekBar ratingSeekBar2 = findViewById(R.id.ratingSeekBar2);
        SeekBar ratingSeekBar3 = findViewById(R.id.ratingSeekBar3);
        SeekBar ratingSeekBar4 = findViewById(R.id.ratingSeekBar4);
        SeekBar ratingSeekBar5 = findViewById(R.id.ratingSeekBar5);
        SeekBar ratingSeekBar6 = findViewById(R.id.ratingSeekBar6);

        Button saveButton = findViewById(R.id.saveButton);
        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int radioButton1Id = importanceRadioGroup1.getCheckedRadioButtonId();
                int radioButton2Id = importanceRadioGroup2.getCheckedRadioButtonId();
                int radioButton3Id = importanceRadioGroup3.getCheckedRadioButtonId();
                int radioButton4Id = importanceRadioGroup4.getCheckedRadioButtonId();
                int radioButton5Id = importanceRadioGroup5.getCheckedRadioButtonId();
                int radioButton6Id = importanceRadioGroup6.getCheckedRadioButtonId();

                int seekBarValue1 = ratingSeekBar1.getProgress() + 1;
                int seekBarValue2 = ratingSeekBar2.getProgress() + 1;
                int seekBarValue3 = ratingSeekBar3.getProgress() + 1;
                int seekBarValue4 = ratingSeekBar4.getProgress() + 1;
                int seekBarValue5 = ratingSeekBar5.getProgress() + 1;
                int seekBarValue6 = ratingSeekBar6.getProgress() + 1;

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

                Log.d("FormActivity", "RadioButton 1: " + radioButton1Text);
                Log.d("FormActivity", "SeekBar 1: " + seekBarValue1);
                Log.d("FormActivity", "RadioButton 2: " + radioButton2Text);
                Log.d("FormActivity", "SeekBar 2: " + seekBarValue2);
                Log.d("FormActivity", "RadioButton 3: " + radioButton3Text);
                Log.d("FormActivity", "SeekBar 3: " + seekBarValue3);
                Log.d("FormActivity", "RadioButton 4: " + radioButton4Text);
                Log.d("FormActivity", "SeekBar 4: " + seekBarValue4);
                Log.d("FormActivity", "RadioButton 5: " + radioButton5Text);
                Log.d("FormActivity", "SeekBar 5: " + seekBarValue5);
                Log.d("FormActivity", "RadioButton 6: " + radioButton6Text);
                Log.d("FormActivity", "SeekBar 6: " + seekBarValue6);
                String toastMessage = "Bobot Kriteria telah disimpan";
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Menambahkan listener untuk SeekBar
        ratingSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Menggunakan nilai progress untuk mengupdate nilai pada TextView atau variabel lainnya
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Tidak perlu melakukan apa-apa pada saat mulai menggeser seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Tidak perlu melakukan apa-apa pada saat berhenti menggeser seekbar
            }
        });
    }
}