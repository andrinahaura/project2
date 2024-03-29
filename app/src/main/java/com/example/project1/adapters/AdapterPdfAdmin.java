package com.example.project1.adapters;

import static com.example.project1.Constans.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1.PdfEditActivity;
import com.example.project1.databinding.RowPdfAdminBinding;
import com.example.project1.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> {
    //context
    private Context context;
    //arraylist to hold list of data of type ModelPdf
    private ArrayList<ModelPdf> pdfArrayList;

    //view binding row_pdf_admin.xml
    private RowPdfAdminBinding binding;

    private static final String TAG = "PDF_ADAPTER_TAG";

    //progress
    private ProgressDialog progressDialog;

    //constructure
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Buat instance binding baru untuk setiap item
        RowPdfAdminBinding itemBinding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(itemBinding);
    }


    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        /*get data, set data, handle click etc*/

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        long timestamp = model.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        //load further details category, pdf from url
        loadCategory(model, holder);
        loadPdfFromUrl(model, holder);

        //handle click, show dialog with options 1) Edit, 2) Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptiopnsdialog(model,holder);
            }
        });
    }

    private void moreOptiopnsdialog(ModelPdf model, HolderPdfAdmin holder){

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();
        //options to show in dialog
        String[] options = {"Edit", "Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle dialog option click
                        if (which==0){
                            //Edit clicked, Open PdfEditActivity to edit the book info
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("BookId", bookId);
                            context.startActivity(intent);


                        }
                        else if (which==1) {
                            //Delete clicked
                            deleteBook(model, holder);
                        }

                    }
                })
                .show();
    }

    private void deleteBook(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        Log.d(TAG, "deleteBook:Deleting...");
        progressDialog.setMessage("Deleting"+bookTitle); //e.g. Deleting Book ABC ...
        progressDialog.show();

        Log.d(TAG, "deleteBook:Deleting from storage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "deleteBook:Deleting from storage");
                        Toast.makeText(context, "ID : " + bookId, Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "onSuccess:Now deleting info from db");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        reference.child("Books").child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Succesfully...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to deleted from db due to "+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to " +e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        //using url we can get file and its metadata from firebase storage
        String pdfUrl = model.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+model.getTitle()+"successfully get the file");


                        //set to pdfview
                        holder.pdfView.fromBytes(bytes)
                                .pages(0) //show only first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError"+t.getMessage());
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure failed getting file from url due to "+e.getMessage());

                    }
                });
    }

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
        //get category using categoryId
        String categoryId = model.getCategoryId();

        if (categoryId != null) { // Pastikan categoryId tidak null
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
            ref.child(categoryId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //get category
                            String category = "" + snapshot.child("category").getValue();

                            //set to category text view
                            holder.categoryTv.setText(category);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }



    @Override
    public int getItemCount() {
        return pdfArrayList.size();
        //return number of records / list size
    }

    /*view holder class for fow_pdf_admin.xml*/
    class HolderPdfAdmin extends RecyclerView.ViewHolder {
        // UI Views dari row_pdf_admin.xml
        PDFView pdfView;
        TextView titleTv, descriptionTv, categoryTv;
        ImageView moreBtn;

        public HolderPdfAdmin(RowPdfAdminBinding itemBinding) {
            super(itemBinding.getRoot());

            // inisialisasi UI views
            pdfView = itemBinding.pdfView;
            titleTv = itemBinding.titleTv;
            descriptionTv = itemBinding.descriptionTv;
            categoryTv = itemBinding.categoryTv;
            moreBtn = itemBinding.moreBtn;
        }
    }
}