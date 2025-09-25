package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MpinLogin extends AppCompatActivity {

    EditText edtPin1, edtPin2, edtPin3, edtPin4;
    MaterialButton btnNextPin;
    TextView txtReset;

    private static final String TAG = "MpinLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mpin_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind Views
        edtPin1 = findViewById(R.id.edtPin1);
        edtPin2 = findViewById(R.id.edtPin2);
        edtPin3 = findViewById(R.id.edtPin3);
        edtPin4 = findViewById(R.id.edtPin4);
        btnNextPin = findViewById(R.id.btnNextPin);
        txtReset = findViewById(R.id.txtReset);

        setupAutoMove(edtPin1, edtPin2);
        setupAutoMove(edtPin2, edtPin3);
        setupAutoMove(edtPin3, edtPin4);

        SharedPreferences sharedPref = getSharedPreferences("ShopData", MODE_PRIVATE);
        String savedPin = sharedPref.getString("mpin", "");

        Log.d(TAG, "Saved MPIN: " + savedPin);

        btnNextPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPin = edtPin1.getText().toString().trim() +
                        edtPin2.getText().toString().trim() +
                        edtPin3.getText().toString().trim() +
                        edtPin4.getText().toString().trim();

                if (enteredPin.length() != 4) {
                    Toast.makeText(MpinLogin.this, "Please enter 4 digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (enteredPin.equals(savedPin)) {
                    Log.d(TAG, "Login Success! PIN matched.");
                    startActivity(new Intent(MpinLogin.this, GoToMAndD.class));
                    finish(); // optional, so user cannot go back to login screen
                } else {
                    Log.e(TAG, "Login Failed! Entered PIN: " + enteredPin + " does not match saved PIN.");
                    Toast.makeText(MpinLogin.this, "Invalid MPIN", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("shop_data", MODE_PRIVATE).edit();
                editor.putBoolean("from_reset_click", true); // mark that reset was clicked
                editor.apply();

                startActivity(new Intent(MpinLogin.this, ResetMpin.class));
                finish();
            }
        });
    }
    private void setupAutoMove(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    next.requestFocus();
                }
            }
        });
    }
}
