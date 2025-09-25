package com.exam.shops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeekEntryAdapter extends RecyclerView.Adapter<WeekEntryAdapter.WeekViewHolder> {

    private final List<WeekEntry> weeks;

    public WeekEntryAdapter(List<WeekEntry> weeks) {
        this.weeks = weeks;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_entry, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        WeekEntry week = weeks.get(position);
        holder.txtWeekTitle.setText(week.getWeekTitle());
        holder.txtWeekDate.setText(week.getDateRange());
        holder.txtWeekAmount.setText("₹ " + week.getAmount());
    }

    @Override
    public int getItemCount() {
        return weeks.size();
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView txtWeekTitle, txtWeekDate, txtWeekAmount;

        WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWeekTitle = itemView.findViewById(R.id.txtWeekTitle);
            txtWeekDate = itemView.findViewById(R.id.txtWeekDate);
            txtWeekAmount = itemView.findViewById(R.id.txtWeekAmount);
        }
    }
}
