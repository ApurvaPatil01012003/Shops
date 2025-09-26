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
        setupPinField(null,    edtPin1, edtPin2);
        setupPinField(edtPin1, edtPin2, edtPin3);
        setupPinField(edtPin2, edtPin3, edtPin4);
        setupPinField(edtPin3, edtPin4, null);


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
private void setupPinField(EditText prev, EditText cur, EditText next) {
    // numeric, 1 char max
    cur.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    cur.setFilters(new android.text.InputFilter[]{ new android.text.InputFilter.LengthFilter(1) });

    cur.addTextChangedListener(new TextWatcher() {
        private boolean selfUpdate = false;
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            if (selfUpdate) return;

            String text = s.toString();

            // Handle paste of multiple digits
            if (text.length() > 1) {
                selfUpdate = true;
                char[] arr = text.replaceAll("\\D", "").toCharArray();
                int i = 0;

                if (arr.length > 0) cur.setText(String.valueOf(arr[i++]));
                if (next != null && arr.length > 1) next.setText(String.valueOf(arr[i++]));
                // focus next if it exists and is empty, else current
                if (next != null && next.getText().length() == 0) next.requestFocus();
                else cur.requestFocus();

                selfUpdate = false;
                return;
            }

            // Normal single char -> move forward
            if (text.length() == 1 && next != null) {
                next.requestFocus();
                next.setSelection(next.getText().length());
            }
        }
    });

    // Backspace on empty -> move back
    cur.setOnKeyListener((v, keyCode, event) -> {
        if (keyCode == android.view.KeyEvent.KEYCODE_DEL
                && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
            if (cur.getText().length() == 0 && prev != null) {
                prev.requestFocus();
                prev.setSelection(prev.getText().length());
                return true;
            }
        }
        return false;
    });

    cur.setOnFocusChangeListener((v, hasFocus) -> {
        if (hasFocus) cur.setSelection(cur.getText().length());
    });
}

}
