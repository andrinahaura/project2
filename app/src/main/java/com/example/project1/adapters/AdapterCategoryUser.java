package com.example.project1.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1.PdfListAdminActivity;
import com.example.project1.databinding.RowCategoryBinding;
import com.example.project1.databinding.RowCategoryUserBinding;
import com.example.project1.models.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategoryUser extends RecyclerView.Adapter<AdapterCategoryUser.HolderCategoryUser>{
    private Context context;
    private ArrayList<ModelCategory> categoryArrayListUser;
    private RowCategoryUserBinding binding;
    public AdapterCategoryUser(Context context, ArrayList<ModelCategory> categoryArrayListUser) {
        this.context = context;
        this.categoryArrayListUser = categoryArrayListUser;
    }

    @NonNull
    @Override
    public HolderCategoryUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       binding = RowCategoryUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategoryUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategoryUser holder, int position) {
        //get data
        ModelCategory model = categoryArrayListUser.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        //set data
        holder.categoryTv.setText(category);


        //handle item click, goto PdfListAdminActivity, also pass pdf category and categoryId
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryID", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayListUser.size();
    }

    class HolderCategoryUser extends RecyclerView.ViewHolder {
        TextView categoryTv;

        public HolderCategoryUser(@NonNull View itemView) {
            super(itemView);
            categoryTv = binding.categoryTv;
        }
    }
}
