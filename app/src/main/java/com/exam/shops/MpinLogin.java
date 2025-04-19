package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MpinLogin extends AppCompatActivity {
    EditText edtPin;
    Button btnNextPin;

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
        edtPin = findViewById(R.id.edtPin);
        btnNextPin = findViewById(R.id.btnNextPin);

        SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        String savedPin = sharedPref.getString("mpin", "");

        Log.d("Mpin", "Mpin of Login is: " + savedPin);

        btnNextPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPin = edtPin.getText().toString().trim();

                if (enteredPin.equals(savedPin)) {
                    startActivity(new Intent(MpinLogin.this, GoToMAndD.class));

                } else {
                    Toast.makeText(MpinLogin.this, "Invalid MPIN", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}