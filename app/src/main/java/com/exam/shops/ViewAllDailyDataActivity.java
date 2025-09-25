package com.exam.shops;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewAllDailyDataActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_daily_data);

        RecyclerView recyclerView = findViewById(R.id.recyclerAllEntries);
        TextView txtNoData = findViewById(R.id.txtNodata);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        Map<String, ?> allData = prefs.getAll();

        List<DailyEntry> allEntries = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (String key : allData.keySet()) {
            if (key.startsWith("Achieved_")) {
                String date = key.replace("Achieved_", "");
                int achieved = prefs.getInt(key, 0);
                int qty = prefs.getInt("Quantity_" + date, 0);
                int nob = prefs.getInt("NOB_" + date, 0);

                allEntries.add(new DailyEntry(date, achieved, qty, nob));
            }
        }

        // ✅ Sort entries by date descending
        allEntries.sort((e1, e2) -> {
            try {
                Date d1 = dateFormat.parse(e1.getDate());
                Date d2 = dateFormat.parse(e2.getDate());
                return d2.compareTo(d1); // latest first
            } catch (Exception e) {
                return 0;
            }
        });

        if (allEntries.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);

            DailyEntryAdapter adapter = new DailyEntryAdapter(allEntries);
            recyclerView.setAdapter(adapter);
        }


    }
}
