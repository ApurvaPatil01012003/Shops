package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class Mpin extends AppCompatActivity {

    EditText edtPin1, edtPin2, edtPin3, edtPin4;
    EditText reEdtPin1, reEdtPin2, reEdtPin3, reEdtPin4;
    MaterialButton btnNextPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mpin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String mobile = getIntent().getStringExtra("Mobile_no");

        edtPin1 = findViewById(R.id.edtPin1);
        edtPin2 = findViewById(R.id.edtPin2);
        edtPin3 = findViewById(R.id.edtPin3);
        edtPin4 = findViewById(R.id.edtPin4);

        reEdtPin1 = findViewById(R.id.reEdtPin1);
        reEdtPin2 = findViewById(R.id.reEdtPin2);
        reEdtPin3 = findViewById(R.id.reEdtPin3);
        reEdtPin4 = findViewById(R.id.reEdtPin4);
        btnNextPin = findViewById(R.id.btnNextPin);

        setupAutoMove(edtPin1, edtPin2);
        setupAutoMove(edtPin2, edtPin3);
        setupAutoMove(edtPin3, edtPin4);

        setupAutoMove(reEdtPin1, reEdtPin2);
        setupAutoMove(reEdtPin2, reEdtPin3);
        setupAutoMove(reEdtPin3, reEdtPin4);

        String Name = getIntent().getStringExtra("shop_name");

        addWatcher(edtPin1);
        addWatcher(edtPin2);
        addWatcher(edtPin3);
        addWatcher(edtPin4);
        addWatcher(reEdtPin1);
        addWatcher(reEdtPin2);
        addWatcher(reEdtPin3);
        addWatcher(reEdtPin4);

        btnNextPin.setEnabled(false);
        btnNextPin.setAlpha(0.5f);

        btnNextPin.setOnClickListener(v -> {

            String pin = edtPin1.getText().toString().trim() +
                    edtPin2.getText().toString().trim() +
                    edtPin3.getText().toString().trim() +
                    edtPin4.getText().toString().trim();

            String confirmPin = reEdtPin1.getText().toString().trim() +
                    reEdtPin2.getText().toString().trim() +
                    reEdtPin3.getText().toString().trim() +
                    reEdtPin4.getText().toString().trim();

            if (pin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(Mpin.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pin.length() != 4) {
                Toast.makeText(Mpin.this, "PIN should be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pin.equals(confirmPin)) {
                Toast.makeText(Mpin.this, "PINs do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("ShopData", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("mpin", pin);
            editor.putBoolean("isFirstTime", false);
            editor.putString("confirmMpin", confirmPin);
            editor.apply();

            Intent intent = new Intent(Mpin.this, Holi_High_Day.class);
            intent.putExtra("shop_name", getIntent().getStringExtra("shop_name"));
            intent.putExtra("Mobile_no",mobile);
            startActivity(intent);
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
    private void addWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFields();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void checkFields() {
        boolean allFilled =
                !edtPin1.getText().toString().trim().isEmpty() &&
                        !edtPin2.getText().toString().trim().isEmpty() &&
                        !edtPin3.getText().toString().trim().isEmpty() &&
                        !edtPin4.getText().toString().trim().isEmpty() &&
                        !reEdtPin1.getText().toString().trim().isEmpty() &&
                        !reEdtPin2.getText().toString().trim().isEmpty() &&
                        !reEdtPin3.getText().toString().trim().isEmpty() &&
                        !reEdtPin4.getText().toString().trim().isEmpty();

        btnNextPin.setEnabled(allFilled);
        btnNextPin.setAlpha(allFilled ? 1f : 0.5f);
    }
}