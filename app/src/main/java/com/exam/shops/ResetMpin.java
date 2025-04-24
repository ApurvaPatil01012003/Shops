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

public class ResetMpin extends AppCompatActivity {
    EditText etResetPin,etResetConfirmPin;
    Button btnResetNextPin;


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
        etResetPin = findViewById(R.id.etResetPin);
        etResetConfirmPin= findViewById(R.id.etResetConfirmPin);
        btnResetNextPin = findViewById(R.id.btnResetNextPin);

        btnResetNextPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = etResetPin.getText().toString().trim();
                String confirmPin = etResetConfirmPin.getText().toString().trim();




                if (pin.isEmpty() || confirmPin.isEmpty()) {
                    Toast.makeText(ResetMpin.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pin.length() != 4) {
                    Toast.makeText(ResetMpin.this, "PIN should be 4 digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pin.equals(confirmPin)) {
                    Toast.makeText(ResetMpin.this, "PINs do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPreferences = getSharedPreferences("shop_data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("mpin", pin);
                editor.putString("confirmMpin",confirmPin);
                editor.apply();

                Toast.makeText(ResetMpin.this, "Reset Successfully", Toast.LENGTH_SHORT).show();
                Log.d("Mpin","Mpin is : "+pin);
                Log.d("Mpin","confirmPin is : "+confirmPin);

            }
        });



    }
}