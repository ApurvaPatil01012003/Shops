package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class ResetMpin extends AppCompatActivity {

    EditText edtPin1, edtPin2, edtPin3, edtPin4;
    EditText reEdtPin1, reEdtPin2, reEdtPin3, reEdtPin4;
    MaterialButton btnResetNextPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_mpin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find all EditTexts
        edtPin1 = findViewById(R.id.edtPin1);
        edtPin2 = findViewById(R.id.edtPin2);
        edtPin3 = findViewById(R.id.edtPin3);
        edtPin4 = findViewById(R.id.edtPin4);

        reEdtPin1 = findViewById(R.id.reEdtPin1);
        reEdtPin2 = findViewById(R.id.reEdtPin2);
        reEdtPin3 = findViewById(R.id.reEdtPin3);
        reEdtPin4 = findViewById(R.id.reEdtPin4);

        btnResetNextPin = findViewById(R.id.btnResetNextPin);


        setupAutoMove(edtPin1, edtPin2);
        setupAutoMove(edtPin2, edtPin3);
        setupAutoMove(edtPin3, edtPin4);

        setupAutoMove(reEdtPin1, reEdtPin2);
        setupAutoMove(reEdtPin2, reEdtPin3);
        setupAutoMove(reEdtPin3, reEdtPin4);

        SharedPreferences sharedPreferences = getSharedPreferences("shop_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("mpin_reset_done", true);
        editor.apply();

        btnResetNextPin.setOnClickListener(v -> {
            // Get PIN from all four boxes
            String pin = edtPin1.getText().toString().trim()
                    + edtPin2.getText().toString().trim()
                    + edtPin3.getText().toString().trim()
                    + edtPin4.getText().toString().trim();

            String confirmPin = reEdtPin1.getText().toString().trim()
                    + reEdtPin2.getText().toString().trim()
                    + reEdtPin3.getText().toString().trim()
                    + reEdtPin4.getText().toString().trim();

            if (pin.length() < 4 || confirmPin.length() < 4) {
                Toast.makeText(ResetMpin.this, "Please enter all 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pin.equals(confirmPin)) {
                Toast.makeText(ResetMpin.this, "PINs do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save PIN in SharedPreferences
            editor.putString("mpin", pin);
            editor.putString("confirmMpin", confirmPin);
            editor.apply();

            boolean fromResetClick = sharedPreferences.getBoolean("from_reset_click", false);

            if (fromResetClick) {
                editor.putBoolean("from_reset_click", false);
                editor.apply();
                Toast.makeText(ResetMpin.this, "Reset Successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ResetMpin.this, MpinLogin.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ResetMpin.this, "Reset Successfully", Toast.LENGTH_SHORT).show();
            }

            Log.d("ResetMpin", "New MPIN set: " + pin);
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
                    next.requestFocus(); // Move to next box when 1 digit is entered
                }
            }
        });
    }
}
