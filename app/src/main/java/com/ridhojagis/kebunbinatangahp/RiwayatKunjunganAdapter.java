package com.ridhojagis.kebunbinatangahp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RiwayatKunjunganAdapter extends RecyclerView.Adapter<RiwayatKunjunganAdapter.ViewHolder> {
    private ArrayList<Koleksi> riwayatKunjungan;

    public RiwayatKunjunganAdapter(ArrayList<Koleksi> riwayatKunjungan) {
        this.riwayatKunjungan = riwayatKunjungan;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout untuk setiap item dalam RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_riwayat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Set data untuk tampilan dalam ViewHolder sesuai dengan posisi item
        Koleksi koleksi = riwayatKunjungan.get(position);

        holder.textViewNama.setText(koleksi.getNama());
        holder.textViewWaktu.setText("Waktu Kunjungan: " + koleksi.getWaktuKunjungan());

        String statusVisited = koleksi.isVisited() ? "Sudah Dikunjungi" : "Belum Dikunjungi";
        holder.textViewVisited.setText("Status: " + statusVisited);

        // Respon ketike item diklik
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ambil koordinat dari item yang diklik
                double latitude = koleksi.getLatitude();
                double longitude = koleksi.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);

                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                intent.putExtra("COORDINATE_RIWAYAT", latLng);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Kembalikan jumlah item dalam riwayatKunjungan
        return riwayatKunjungan.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Deklarasikan elemen-elemen tampilan di dalam ViewHolder
        public TextView textViewNama;
        public TextView textViewWaktu;
        public TextView textViewVisited;

        public ViewHolder(View itemView) {
            super(itemView);

            // Inisialisasi elemen-elemen tampilan di dalam ViewHolder
            textViewNama = itemView.findViewById(R.id.textViewNama);
            textViewWaktu = itemView.findViewById(R.id.textViewWaktu);
            textViewVisited = itemView.findViewById(R.id.textViewVisited);
        }
    }
}

