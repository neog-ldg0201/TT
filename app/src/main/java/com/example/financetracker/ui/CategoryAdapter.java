package com.example.financetracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;

import java.util.Collections;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> categories;
    private final OnCategoryDeleteListener deleteListener;
    private OnCategoryEditListener editListener;

    public interface OnCategoryDeleteListener {
        void onDelete(String category, int position);
    }

    public interface OnCategoryEditListener {
        void onEdit(String category, int position);
    }

    public interface OnCategoryMoveListener {
        void onMove(int fromPosition, int toPosition);
    }

    private OnCategoryMoveListener moveListener;

    public CategoryAdapter(List<String> categories, OnCategoryDeleteListener deleteListener) {
        this.categories = categories;
        this.deleteListener = deleteListener;
    }

    public void setEditListener(OnCategoryEditListener editListener) {
        this.editListener = editListener;
    }

    public void setMoveListener(OnCategoryMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryNameText.setText(category);

        holder.editCategoryButton.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(category, position);
            }
        });

        holder.deleteCategoryButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(category, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (categories != null && position < categories.size()) {
            categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (categories != null && fromPosition < categories.size() && toPosition < categories.size()) {
            Collections.swap(categories, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            if (moveListener != null) {
                moveListener.onMove(fromPosition, toPosition);
            }
        }
    }

    public List<String> getCategories() {
        return categories;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText;
        ImageButton editCategoryButton;
        ImageButton deleteCategoryButton;
        ImageView dragHandle;

        ViewHolder(View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            editCategoryButton = itemView.findViewById(R.id.editCategoryButton);
            deleteCategoryButton = itemView.findViewById(R.id.deleteCategoryButton);
            dragHandle = itemView.findViewById(R.id.dragHandle);
        }
    }
}
