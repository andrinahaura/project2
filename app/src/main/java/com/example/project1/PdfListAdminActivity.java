package com.example.project1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.project1.adapters.AdapterPdfAdmin;
import com.example.project1.databinding.ActivityPdfListAdminBinding;
import com.example.project1.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {

    //viewbinding
    private ActivityPdfListAdminBinding binding;

    //Arraylist to hold list of data of type ModelPdf
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfAdmin adapterPdfAdmin;
    private String categoryId, categoryTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        //get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryID");
        categoryTitle = intent.getStringExtra("categoryTitle");
        //Toast.makeText(getApplicationContext(), "Id : " + categoryId, Toast.LENGTH_LONG).show();

        //set pdf category
        binding.subtitleTv.setText(categoryTitle);

        loadPdfList();

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadPdfList() {
        //init list before adding data
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        //Toast.makeText(getApplicationContext(), "Id : " + categoryId, Toast.LENGTH_LONG).show();

        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //Toast.makeText(getApplicationContext(), "Title : " + model.getTitle(), Toast.LENGTH_LONG).show();
                            //add to list
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                        binding.bookRv.setAdapter (adapterPdfAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}