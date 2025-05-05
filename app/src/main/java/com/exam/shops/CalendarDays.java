package com.exam.shops;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import android.graphics.Color;
import android.widget.GridLayout;
import android.widget.TextView;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarDays extends AppCompatActivity {

    private GridLayout calendarGrid;
    private TextView monthText;
    private Calendar calendar;
    private List<String> publicHolidays;
    private List<String> highPerfPreDays;
    private String shopHoliday;
    String HighPerDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar_days);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        calendarGrid = findViewById(R.id.calendarGrid);
        monthText = findViewById(R.id.monthText);
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        calendar = Calendar.getInstance();
        btnPrev.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            drawCalendar();
        });


        btnNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            drawCalendar();
        });

        SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        shopHoliday = sharedPref.getString("Shop_Holiday", "");
        publicHolidays = HolidayUtils.loadHolidayDates(this);
        highPerfPreDays = HolidayUtils.getPreHolidayHighPerformanceDates(publicHolidays, shopHoliday);
        HighPerDay = sharedPref.getString("selected_days", "");
        Log.d("Publicdays", "IS : " + publicHolidays);
        Log.d("Publicdays", "IS : " + highPerfPreDays);
        Log.d("Publicdays", "IS : " + shopHoliday);
        Log.d("Publicdays", "IS : " + HighPerDay);
        drawCalendar();

    }

    private void drawCalendar() {
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthText.setText(sdf.format(calendar.getTime()));

        calendarGrid.removeAllViews();


        calendarGrid.setColumnCount(7);


        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            TextView header = createDayLabel(day, true);
            header.setBackgroundColor(Color.DKGRAY);
            header.setTextColor(Color.WHITE);
            calendarGrid.addView(header);
        }

        for (int i = 0; i < firstDayOfWeek; i++) {
            TextView empty = createDayLabel("", false);
            empty.setBackgroundColor(Color.TRANSPARENT);
            calendarGrid.addView(empty);
        }

        for (int day = 1; day <= maxDays; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime());
            String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());

            TextView tv = createDayLabel(String.valueOf(day), false);

            boolean isHoliday = dayName.equalsIgnoreCase(shopHoliday);
            boolean isHighPerf = HighPerDay != null && HighPerDay.toLowerCase().contains(dayName.toLowerCase());
            boolean isPreHighPerf = highPerfPreDays.contains(dateStr);
            boolean isPublicHoliday = publicHolidays.contains(dateStr);


            if (isHoliday || isPublicHoliday) {
                tv.setBackgroundColor(Color.RED);
            } else if (isHighPerf || isPreHighPerf) {
                tv.setBackgroundColor(Color.GREEN);
//            } else if (isPreHighPerf) {
//                tv.setBackgroundColor(Color.YELLOW);
//            } else if (isPublicHoliday) {
//                tv.setBackgroundColor(Color.MAGENTA);
 }
               else {
                tv.setBackgroundColor(Color.LTGRAY);
            }

            tv.setOnClickListener(v -> {
                StringBuilder message = new StringBuilder();

                if (isHoliday) message.append("Holiday\n");
                if (isPublicHoliday) message.append("Public Holiday\n");
                if (isHighPerf) message.append("High Performance Day\n");
                if (isPreHighPerf) message.append("Pre-High Performance Day\n");

                if (message.length() == 0) {
                    message.append("Working Day");
                }

                new android.app.AlertDialog.Builder(CalendarDays.this)
                        .setTitle("Day Information")
                        .setMessage(message.toString().trim())
                        .setPositiveButton("OK", null)
                        .show();
            });

            calendarGrid.addView(tv);
        }

    }

    private TextView createDayLabel(String text, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 24, 0, 24);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(params);

        tv.setBackgroundColor(isHeader ? Color.LTGRAY : Color.TRANSPARENT);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(16);
        return tv;
    }




}