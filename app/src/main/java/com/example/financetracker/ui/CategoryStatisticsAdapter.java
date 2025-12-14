package com.example.financetracker.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.model.CategoryStatistics;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryStatisticsAdapter extends RecyclerView.Adapter<CategoryStatisticsAdapter.ViewHolder> {

    private List<CategoryStatistics> statistics;
    private final int[] colors;

    public CategoryStatisticsAdapter(List<CategoryStatistics> statistics, int[] colors) {
        this.statistics = statistics;
        this.colors = colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_statistics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStatistics stat = statistics.get(position);

        holder.percentageText.setText(String.format("%.0f%%", stat.getPercentage()));
        holder.categoryNameText.setText(stat.getCategory());

        String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(stat.getTotalAmount());
        holder.categoryAmountText.setText(formattedAmount + "원");

        // Set color for percentage badge
        int colorIndex = stat.getColorIndex();
        if (colorIndex >= 0 && colorIndex < colors.length) {
            holder.percentageText.setBackgroundColor(colors[colorIndex]);
        } else if (position < colors.length) {
            holder.percentageText.setBackgroundColor(colors[position]);
        } else {
            holder.percentageText.setBackgroundColor(Color.GRAY);
        }
        holder.percentageText.setTextColor(Color.BLACK);
    }

    @Override
    public int getItemCount() {
        return statistics != null ? statistics.size() : 0;
    }

    public void setStatistics(List<CategoryStatistics> statistics) {
        this.statistics = statistics;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView percentageText;
        TextView categoryNameText;
        TextView categoryAmountText;

        ViewHolder(View itemView) {
            super(itemView);
            percentageText = itemView.findViewById(R.id.percentageText);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            categoryAmountText = itemView.findViewById(R.id.categoryAmountText);
        }
    }
}
