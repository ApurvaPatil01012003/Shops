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

                float achieved = getSafeFloat(prefs, "Achieved_" + date, 0f);
                float qty      = getSafeFloat(prefs, "Quantity_" + date, 0f);
                float nob      = getSafeFloat(prefs, "NOB_" + date, 0f);

                boolean expectedOnly = prefs.getBoolean("expected_only_" + date, false);
                boolean fromYOY      = prefs.getBoolean("from_yoy_" + date, false);


                if ((expectedOnly && achieved == 0f && qty == 0f && nob == 0f)
                        || (!fromYOY && achieved == 0f && qty == 0f && nob == 0f)) {
                    continue;
                }

                allEntries.add(new DailyEntry(date, achieved, qty, nob));
            }
        }


        // Sort by date descending (latest first)
        allEntries.sort((e1, e2) -> {
            try {
                Date d1 = dateFormat.parse(e1.getDate());
                Date d2 = dateFormat.parse(e2.getDate());
                return d2.compareTo(d1);
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

    private float getSafeFloat(SharedPreferences prefs, String key, float defaultValue) {
        try {
            return prefs.getFloat(key, defaultValue);
        } catch (ClassCastException e) {
            Object value = prefs.getAll().get(key);
            if (value instanceof Integer) {
                return ((Integer) value).floatValue();
            } else if (value instanceof Long) {
                return ((Long) value).floatValue();
            } else if (value instanceof String) {
                try {
                    return Float.parseFloat((String) value);
                } catch (NumberFormatException ex) {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }
    }
}
