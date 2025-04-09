package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;


public class ChartActivity extends AppCompatActivity {
BarChart barChart;
PieChart pieChart;
    Button btnMonthlyYOY, btnDailyYOY ,btnlogout;
    String TURNOVER;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barChart=findViewById(R.id.barChart);
        pieChart=findViewById(R.id.pieChart);
        btnMonthlyYOY=findViewById(R.id.btnMonthlyYOY);
        btnDailyYOY=findViewById(R.id.btnDailyYOY);
        setUpBarchart();
        setupPieChart();



    SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);


        String turnoverFromIntent = getIntent().getStringExtra("TurnOver");
        if (turnoverFromIntent != null && !turnoverFromIntent.isEmpty()) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("TurnOver", turnoverFromIntent);
            editor.apply();
        }
        TURNOVER = sharedPref.getString("TurnOver", "0");

        Log.d("TURNOVER", "Turnover used (hiddenly): " + TURNOVER);





        btnlogout=findViewById(R.id.btnlogout);
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("ShopData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ChartActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnMonthlyYOY.setOnClickListener(v -> openYOYActivity());
        btnDailyYOY.setOnClickListener(v -> openYOYSECONDActivity());


    }

    private void setUpBarchart() {
        float[][] salesData = {
                {120000, 50000},
                {130000, 70000},
                {110000, 60000},
                {115000, 65000},
                {125000, 55000},
                {140000, 80000},
                {118000, 60000},

        };

        String[] months = {"April", "August", "December", "February", "January", "July", "June", "March", "May", "November", "October", "September"};

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < salesData.length; i++) {
            entries.add(new BarEntry(i, salesData[i]));
        }

        // Creating dataset for stacked bars
        BarDataSet barDataSet = new BarDataSet(entries, "Sales Data");
        barDataSet.setColors(new int[]{Color.BLUE, Color.parseColor("#FFA500")});
        barDataSet.setStackLabels(new String[]{"Monthly Target", "Actual Sales"});
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(12f);
        barDataSet.setValueTextColor(Color.WHITE);

        // Custom Value Formatter to Display Values Inside Bars
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarStackedLabel(float value, BarEntry entry) {
                return String.valueOf((int) value);
            }
        });


        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        Description description = new Description();
        description.setText("Monthly Target vs Actual Sales");
        description.setTextSize(14f);
        description.setTextColor(Color.BLACK);
        description.setPosition(barChart.getWidth() / 2f, 50f);
        barChart.setDescription(description);

        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(0);
        xAxis.setDrawGridLines(false);


        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(20000f);
        leftAxis.setDrawGridLines(true);
        barChart.getAxisRight().setEnabled(false); // Disable right Y-axis

        // Customize Legend
          Legend legend = barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
       legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
       legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);

        barChart.invalidate();
    }



    private void setupPieChart() {
        String[] months = {"October", "February", "January", "July", "September",
                "December", "November", "May", "August", "April", "March", "June"};
        float[] actualSalesPercentage = {7.0f, 9.3f, 8.6f, 9.3f, 8.9f,
                9.0f, 6.9f, 8.5f, 9.8f, 7.8f, 7.4f, 7.4f};


        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < months.length; i++) {
            entries.add(new PieEntry(actualSalesPercentage[i], months[i]));
        }

        // Set PieDataSet
        PieDataSet dataSet = new PieDataSet(entries, "Monthly Sales Distribution");
        dataSet.setColors(new int[]{
                Color.parseColor("#FFD700"),
                Color.parseColor("#228B22"),
                Color.parseColor("#FF69B4"),
                Color.parseColor("#DC143C"),
                Color.parseColor("#8B4513"),
                Color.parseColor("#32CD32"),
                Color.parseColor("#4B0082"),
                Color.parseColor("#FFA500"),
                Color.parseColor("#4682B4"),
                Color.parseColor("#ADD8E6"),
                Color.parseColor("#FF4500"),
                Color.parseColor("#F4A460")
        });

        // Ensure labels are outside
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(100.f);
        dataSet.setValueLinePart1Length(0.6f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        // Set Pie Data
        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Customize Pie Chart
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(0f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.animateY(1000);

        // Customize Legend
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);

        // Refresh Chart
        pieChart.invalidate();
    }

  public void openYOYActivity()
    {
//        Intent intent = new Intent(ChartActivity.this,YOYActivity.class);
//        intent.putExtra("TurnOver",TURNOVER);
//        startActivity(intent);
//        Log.d("TURNOVER","turn over pass to yoyactivity"+TURNOVER);


    }

    public void openYOYSECONDActivity()
    {
//        Intent intent = new Intent(ChartActivity.this, YOYSecondActivity.class);
//        startActivity(intent);
    }


}