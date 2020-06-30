package com.belajar.hotelapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.HotelViewHolder> {

    private Context mCtx;
    private List<Hotel> hotelList;


    public BookingAdapter(Context mCtx, List<Hotel> hotelList) {
        this.mCtx = mCtx;
        this.hotelList = hotelList;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HotelViewHolder(
                LayoutInflater.from(mCtx).inflate(R.layout.layout_booking, parent, false)
        );
    }
    @Override
    public void onBindViewHolder(@NonNull final HotelViewHolder holder, final int position) {
        final Hotel hotel = hotelList.get(position);

        holder.textViewNama.setText(hotel.getNama());
        holder.textViewAlamat.setText("Alamat : " +hotel.getAlamat());
        holder.textViewHarga.setText("Harga : RP. " + hotel.getHarga());
        holder.textViewJumlah.setText("Tersedia " + hotel.getJumlah() + " Kamar");
        Picasso.get()
                .load(hotel.getUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(holder.imageview);
        if(hotel.getJumlah() == 0){
            holder.bookingbtn.setEnabled(false);
        }else {
            holder.bookingbtn.setEnabled(true);
        }

        holder.bookingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCtx.startActivity(ConfirmBooking.getActIntent((Activity) mCtx).putExtra("data", hotelList.get(position)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }


    class HotelViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewNama, textViewAlamat, textViewHarga, textViewJumlah ;
        public Button bookingbtn;
        public ImageView imageview;
        public CardView cvMain;

        public HotelViewHolder(View itemView) {
            super(itemView);

            textViewNama = itemView.findViewById(R.id.tv_nama);
            textViewAlamat = itemView.findViewById(R.id.tv_alamat);
            textViewHarga = itemView.findViewById(R.id.tv_harga);
            textViewJumlah = itemView.findViewById(R.id.tv_jlh);
            imageview = itemView.findViewById(R.id.imageView);
            cvMain = (CardView) itemView.findViewById(R.id.cv_hotel);
            bookingbtn = itemView.findViewById(R.id.btn_booking);



        }
    }
}
