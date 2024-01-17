package com.example.project1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.project1.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfEditBinding binding;


    //book id get from intent started from AdapterPdfAdmin
    private String bookId;

    //progress dialog
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //book id get from intent started from adapterPdfAdmin
        bookId = getIntent().getStringExtra("bookId");

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadcategories();
        loadBookInfo();

        //handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });



        //handle click, go to previous screen
       // binding.backBtn.setOnClickListener(new View.OnClickListener() {
           // @Override
            //public void onClick(View v)
           // {
                //onBackPressed();
           // }
       // });

        //handle click begin upload
        binding.submitBtn.setOnClickListener(new View. OnClickListener () {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void loadcategories() {
    }

    private void loadBookInfo() {
        Log. d(TAG, "loadBookInfo: Loading book info");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference ( "Books");
        refBooks.child (bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectedCategoryId = String.valueOf(snapshot.child("categoryId").getValue());
                        String description = String.valueOf(snapshot.child("description").getValue());
                        String title = String.valueOf(snapshot.child("title").getValue());

                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = String.valueOf(snapshot.child("category").getValue());
                                        //set to category text view
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                    private String selectedCategoryId="", selectedCategoryTitle="";
                    private void categoryDialog(){
                        //make string array from arrayList of string
                        String[] categoriesArray = new String[categoryTitleArrayList.size()];
                        for (int i=0; i<categoryTitleArrayList.size(); i++){
                            categoriesArray[i] = categoryTitleArrayList.get(i);
                        }

                        //Alert Dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(PdfEditActivity.this);
                        builder.setTitle("Choose Category")
                                .setItems(categoriesArray, new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which ){
                                        selectedCategoryId = categoryIdArrayList.get((which));
                                        selectedCategoryTitle = categoryTitleArrayList.get(which);

                                        //set to textview
                                        binding.categoryTv.setText (selectedCategoryTitle);
                                    }
                                })
                                .show();
                    }
                    private void loadcategories() {
                        Log.d(TAG, "deleteBook:Deleting from storage");
                        categoryIdArrayList = new ArrayList<>();
                        categoryTitleArrayList = new ArrayList<>();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                categoryIdArrayList.clear();
                                categoryTitleArrayList.clear();
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String id = ""+ds.child("id").getValue();
                                    String category = ""+ds.child("category").getValue();
                                    categoryIdArrayList.add(id);
                                    categoryTitleArrayList.add(category);

                                    Log.d(TAG, "onDataChange: ID "+id);
                                    Log.d(TAG, "onDataChange: Category: "+category);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });}
    private String title="", description ="";
    private void validateData(){
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "pick Category", Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }

    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: starting updating pdf info to due to db...");

        //show progress
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book updated...");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Book info updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed update due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String selectedCategoryId="", selectedCategoryTitle="";
    private void categoryDialog(){
        //make string array from arraylist
    }
}