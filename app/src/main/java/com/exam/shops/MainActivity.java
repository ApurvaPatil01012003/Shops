package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;
import android.provider.Settings;



public class MainActivity extends AppCompatActivity {
    EditText edtName, edtMobile, edtActcode;
    Button btnActive;
    String deviceId;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

//        SharedPreferences sharedPref = getSharedPreferences("ShopData", MODE_PRIVATE);
//        boolean isFirstTime = sharedPref.getBoolean("isFirstTime", true);
//
//        if (!isFirstTime) {
//            Intent intent = new Intent(MainActivity.this,MpinLogin.class);
//            startActivity(intent);
//            finish();
//            return;
//        }


        edtName = findViewById(R.id.edtName);
        edtMobile = findViewById(R.id.edtMobile);
        edtActcode = findViewById(R.id.edtActcode);
        progressBar = findViewById(R.id.progressBar);


        CheckBox checkBox = findViewById(R.id.checkBoxTerms);
        MaterialButton btnActive = findViewById(R.id.btnActive);


        btnActive.setAlpha(0.5f);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Enable button only if checkbox is checked
                btnActive.setEnabled(isChecked);

// Set tick color programmatically
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_checked}, // unchecked
                                new int[]{android.R.attr.state_checked}   // checked
                        },
                        new int[]{
                                Color.GRAY,       // color when unchecked
                                Color.parseColor("#C55836") // orange when checked
                        }
                );

                checkBox.setButtonTintList(colorStateList);



                // Optional: change alpha for visual feedbackmobile
                if (isChecked) {
                    btnActive.setAlpha(1f);  // fully visible
                } else {
                    btnActive.setAlpha(0.5f);  // dimmed
                }
            }
        });


//        btnActive.setOnClickListener(v -> {
//            String name = edtName.getText().toString().trim();
//            String mobile = edtMobile.getText().toString().trim();
//            String actCode = edtActcode.getText().toString().trim();
//
//
//            if (!name.isEmpty() && !mobile.isEmpty() && !actCode.isEmpty()) {
//                if (mobile.length() == 10 && mobile.matches("\\d{10}")) {
//                    saveDataToSharedPref(name, mobile, actCode);
//                    Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(MainActivity.this, Mpin.class);
//                    //Intent intent = new Intent(MainActivity.this, TermsActivity.class);
//                    intent.putExtra("shop_name", name);
//                    intent.putExtra("Mobile_no",mobile);
//                    startActivity(intent);
//                    finish();
//                    Log.d("MainAct","Mobile no : "+mobile);
//                } else {
//                    Toast.makeText(this, "Please Enter 10 Digit Valid Mobile Number", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
//            }
//        });

        btnActive.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String name = edtName.getText().toString().trim();
            String mobile = edtMobile.getText().toString().trim();
            String actCode = edtActcode.getText().toString().trim();

            if (!name.isEmpty() && !mobile.isEmpty() && !actCode.isEmpty()) {
                if (mobile.length() == 10 && mobile.matches("\\d{10}")) {
                    validateActivation(name, mobile, actCode);
                } else {
                    Toast.makeText(this, "Please Enter 10 Digit Valid Mobile Number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void validateActivation(String name, String mobile, String actCode) {
        String url = "https://beyourbest.in/valid.php?mobile=" + mobile + "&activation=" + actCode;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("API_RESPONSE", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String validityTill = jsonObject.optString("validity_till");
                        int daysLeft = jsonObject.optInt("days_left");

                        if ("valid".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Activation valid till " + validityTill, Toast.LENGTH_SHORT).show();
                            registerDevice(name, mobile, actCode);
                        } else {
                            Toast.makeText(this, "Invalid activation code!", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unexpected response format", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }

    private void registerDevice(String name, String mobile, String actCode) {
        // Manually set a device ID (only for testing)
        //String manualDeviceId = "DEVICE002"; // <-- you can change this manually

        String url = "https://beyourbest.in/register_device.php?mobile=" + mobile
                + "&activation=" + actCode
                + "&mobileID=" + deviceId
                + "&shop_name=" + name;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("REGISTER_RESPONSE", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.optString("message");
                        String validityTill = jsonObject.optString("validity_till");
                        String serverDeviceId = jsonObject.optString("mobileID");

                        if ("success".equalsIgnoreCase(status)) {
                            saveDataToSharedPref(name, mobile, actCode, serverDeviceId);
                            Toast.makeText(this, message + "\nValid till: " + validityTill, Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(MainActivity.this, Mpin.class);
                            intent.putExtra("shop_name", name);
                            intent.putExtra("Mobile_no", mobile);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unexpected response format", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }


    private void saveDataToSharedPref(String name, String mobile, String actCode, String deviceId) {
        SharedPreferences sharedPref = getSharedPreferences("ShopData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("shop_name", name);
        editor.putString("mobile", mobile);
        editor.putString("activation_code", actCode);
        editor.putString("device_id", deviceId);  // Save the real device ID
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }


}