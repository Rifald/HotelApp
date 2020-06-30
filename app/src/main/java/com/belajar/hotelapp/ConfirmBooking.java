package com.belajar.hotelapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ConfirmBooking extends AppCompatActivity {

    private EditText tanggal;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextView namaPemsan;

    private TextView namaHotel;
    private TextView harga;
    private TextView total;
    private EditText pembayaranHtl;
    private EditText jumlahPemsan;
    private FirebaseFirestore db;
    StorageReference storageReference;
    private Button bookingBtn;
    ImageView imgH;
    String userId;
    String email;
    String phone;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("hotel");
        namaHotel = findViewById(R.id.nama_hotel_tv);
        harga = findViewById(R.id.harga_tv);
        total = findViewById(R.id.total_tv);
        tanggal = findViewById(R.id.tanggal_pemesan);

        namaPemsan = findViewById(R.id.nama_pemsan);
        jumlahPemsan = findViewById(R.id.jumlah_pemsan);
        pembayaranHtl = findViewById(R.id.pembayaran_htl);
        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();


        imgH = findViewById(R.id.img_htl);
        bookingBtn = findViewById(R.id.booking_btn);

        final Hotel hotel = (Hotel) getIntent().getSerializableExtra("data");

        if (hotel != null) {
            final int totalKamar = hotel.getJumlah();
            namaHotel.setText(hotel.getNama());
            harga.setText("Rp. "+hotel.getHarga().toString());


            DocumentReference documentReference = db.collection("users").document(userId);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    assert documentSnapshot != null;
                    if(documentSnapshot.exists()){
                        namaPemsan.setText(documentSnapshot.getString("fName"));
                        email = documentSnapshot.getString("email");
                        phone = documentSnapshot.getString("phone");
                    }else {
                        Log.d("tag", "onEvent: Document do not exists");
                    }
                }
            });


            tanggal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dialog = new DatePickerDialog(ConfirmBooking.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                            mDateSetListener,year,month,day);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            });

            mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    String date =  day+ "/" +  month+ "/" + year;
                    tanggal.setText(date);
                }
            };


            jumlahPemsan.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    if (charSequence.length() > 0){
                        int jlhPesan2 = Integer.parseInt(jumlahPemsan.getText().toString());
                        int harga1 = Integer.parseInt(String.valueOf(hotel.getHarga()));
                        String totalnya = String.valueOf(jlhPesan2 * harga1);

                        total.setText(totalnya);

                    }else{
                        total.setText("0");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            Picasso.get().load(hotel.getUrl()).fit().centerCrop().into(imgH);


            bookingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(jumlahPemsan.getText())){
                        jumlahPemsan.setError("Jumlah Pemesanan Tidak Boleh Kosong");
                        return;
                    }else if (TextUtils.isEmpty(pembayaranHtl.getText())){
                        pembayaranHtl.setError("Pembayaran Tidak Boleh Kosong");
                        return;
                    }else if (Integer.parseInt(total.getText().toString()) != Integer.parseInt(pembayaranHtl.getText().toString())){
                        pembayaranHtl.setError("Pembayaran Harus Sesuai dengan Total Harga");
                        return;
                    }else if (totalKamar < Integer.parseInt(String.valueOf(jumlahPemsan.getText()))){
                        jumlahPemsan.setError("Kamar Hotel yang Tersedia Hanya " + totalKamar);
                        return;
                    }else if (tanggal.getText().toString().equals("")){
                        tanggal.setError("Silahkan Tentukan Tanggal Pemesanan");
                        return;
                    }else if (Integer.parseInt(String.valueOf(jumlahPemsan.getText())) == 0){
                        jumlahPemsan.setError("Harap Memasukkan Jumlah Pemesanan");
                        return;
                    }else{
                        int ubahKamar = totalKamar - Integer.parseInt(String.valueOf(jumlahPemsan.getText()));
                        hotel.setJumlah(ubahKamar);

                        final Map<String, Object> pesan = new HashMap<>();
                        pesan.put("hotel",namaHotel.getText().toString());
                        pesan.put("nama",namaPemsan.getText().toString());
                        pesan.put("jumlah",Integer.parseInt(String.valueOf(jumlahPemsan.getText())));
                        pesan.put("total",Integer.parseInt(String.valueOf(total.getText())));
                        pesan.put("tanggal",tanggal.getText().toString());

                        final Map<String, Object> bio = new HashMap<>();
                        bio.put("fName",namaPemsan.getText().toString());
                        bio.put("email",email);
                        bio.put("phone",phone);

                        db.collection("hotel").document(namaHotel.getText().toString())
                                .set(hotel)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("pesan").document(email).set(bio).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                db.collection("pesan").document(email).collection("pemesanan").document(String.valueOf(namaHotel.getText()))
                                                        .set(pesan)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(ConfirmBooking.this, "Pemesanan Berhasil. Silahkan Lihat di Menu Tiket", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(ConfirmBooking.this, "Pemesanan Gagal", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ConfirmBooking.this, "Gagal Memasukkan biodata", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


                    }

                }
            });
        }
    }


    public static Intent getActIntent(Activity activity) {
        // kode untuk pengambilan Intent
        return new Intent(activity, ConfirmBooking.class);
    }
}
