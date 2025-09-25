package com.exam.shops;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Privacy_Policy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String shopName = getIntent().getStringExtra("shop_name");
Button btnAccept = findViewById(R.id.btnAccept);


        btnAccept.setOnClickListener(v -> {
            Toast.makeText(this, "Accepted All terms", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Privacy_Policy.this, Mpin.class);
            intent.putExtra("shop_name", shopName);
            startActivity(intent);
            finish();
        });
    }
}
