package com.exam.shops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DailyEntryAdapter extends RecyclerView.Adapter<DailyEntryAdapter.EntryViewHolder> {

    private List<DailyEntry> entryList;

    public DailyEntryAdapter(List<DailyEntry> entryList) {
        this.entryList = entryList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_entry, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        DailyEntry entry = entryList.get(position);
        holder.txtDate.setText(entry.getDate());
        //holder.txtAchieved.setText("₹" + String.format("%.2f", entry.getAchieved()));
        holder.txtAchieved.setText("₹" + entry.getAchieved());
        holder.txtQty.setText("Qty: " + entry.getQuantity());
        holder.txtNob.setText("Bills: " + entry.getNob());
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtAchieved, txtQty, txtNob;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtAchieved = itemView.findViewById(R.id.txtAchieved);
            txtQty = itemView.findViewById(R.id.txtQty);
            txtNob = itemView.findViewById(R.id.txtNob);
        }
    }
}
