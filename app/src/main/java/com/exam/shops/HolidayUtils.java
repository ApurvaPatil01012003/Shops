package com.exam.shops;



import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class HolidayUtils {

    public static List<String> loadHolidayDates(Context context) {
        List<String> holidays = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            InputStream is = context.getAssets().open("public_holidays.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray array = jsonObj.getJSONArray("public_holidays");

            for (int i = 0; i < array.length(); i++) {
                String rawDate = array.getString(i);
                try {
                    String formatted = outputFormat.format(inputFormat.parse(rawDate));
                    holidays.add(formatted);
                } catch (Exception e) {
                    Log.e("HolidayUtils", "Error parsing date: " + rawDate);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return holidays;
    }

    public static List<String> getPreHolidayHighPerformanceDates(List<String> publicHolidays, String weeklyHolidayName) {
        List<String> preDates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);

        for (String holidayStr : publicHolidays) {
            try {
                Calendar holiday = Calendar.getInstance();
                holiday.setTime(dateFormat.parse(holidayStr));

                for (int i = 1; i <= 7; i++) {
                    Calendar preDay = (Calendar) holiday.clone();
                    preDay.add(Calendar.DAY_OF_MONTH, -i);
                    String preDateStr = dateFormat.format(preDay.getTime());
                    String preDayName = dayFormat.format(preDay.getTime());

                    if (!weeklyHolidayName.equalsIgnoreCase(preDayName) && !publicHolidays.contains(preDateStr)) {
                        preDates.add(preDateStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return preDates;
    }
}

