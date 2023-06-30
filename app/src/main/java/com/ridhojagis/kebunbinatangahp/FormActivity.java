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

        // Inisialisasi komponen dari layout XML
        TextView questionTextView1 = findViewById(R.id.questionTextView1);
        TextView questionTextView2 = findViewById(R.id.questionTextView2);
        TextView questionTextView3 = findViewById(R.id.questionTextView3);
        TextView questionTextView4 = findViewById(R.id.questionTextView4);
        TextView questionTextView5 = findViewById(R.id.questionTextView5);
        TextView questionTextView6 = findViewById(R.id.questionTextView6);

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
                // Mendapatkan teks RadioButton yang dipilih
                RadioButton selectedRadioButton1 = findViewById(radioButton1Id);
                String radioButton1Text = selectedRadioButton1.getText().toString();

                int seekBarValue1 = ratingSeekBar1.getProgress() + 1;
                Log.d("FormActivity", "RadioButton 1: " + radioButton1Text);
                Log.d("FormActivity", "SeekBar 1: " + seekBarValue1);
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