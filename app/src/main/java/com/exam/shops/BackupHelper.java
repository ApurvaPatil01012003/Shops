package com.exam.shops;
import android.content.ContentValues;
import android.content.Context;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

public class BackupHelper {

    public static final String[] PREF_NAMES = {
            "Shop Data",
            "MyPrefs",
            "YOY_PREFS",     // Monthly summaries
            "shop_data",     // GoToActivity configs
            "TodayData"      // Today's metrics
    };

    public static void exportBackup(Context context) {
        try {
            JSONObject backupJson = new JSONObject();

            for (String prefName : PREF_NAMES) {
                SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
                Map<String, ?> allEntries = prefs.getAll();
                JSONObject json = new JSONObject(allEntries);
                backupJson.put(prefName, json);
            }

            String fileName = "shop_data_backup.json";
            String mimeType = "application/json";

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Downloads.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            }

            if (uri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(backupJson.toString().getBytes());
                    outputStream.close();

                    Toast.makeText(context, "Backup saved to Downloads folder", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Failed to open output stream", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Failed to create file in Downloads", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void importBackup(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show();
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            JSONObject backupJson = new JSONObject(builder.toString());

            for (String prefName : PREF_NAMES) {
                JSONObject prefData = backupJson.optJSONObject(prefName);
                if (prefData != null) {
                    SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    Iterator<String> keys = prefData.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object value = prefData.get(key);
                        if (value instanceof Number) {
                            double num = ((Number) value).doubleValue();
                            if (Math.floor(num) == num && num <= Integer.MAX_VALUE && num >= Integer.MIN_VALUE) {
                                editor.putInt(key, (int) num);  // whole number within int range
                            } else {
                                editor.putFloat(key, (float) num);  // decimal or large number
                            }
                        } else if (value instanceof Boolean) {
                            editor.putBoolean(key, (Boolean) value);
                        } else if (value instanceof String) {
                            editor.putString(key, (String) value);
                        }

                    }
                    editor.apply();
                }
            }

            Toast.makeText(context, "Backup imported successfully", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }




}


