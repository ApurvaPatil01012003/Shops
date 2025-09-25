package com.exam.shops;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String shopName = getIntent().getStringExtra("shop_name");

        Button btnAccept = findViewById(R.id.btnAccept);
        CheckBox checkAccept = findViewById(R.id.checkAccept);

        checkAccept.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAccept.setEnabled(isChecked);
        });

        btnAccept.setOnClickListener(v -> {
            Toast.makeText(this, "Accepted All term", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TermsActivity.this, Privacy_Policy.class);
            intent.putExtra("shop_name", shopName);
            startActivity(intent);
            finish();
        });
    }
}