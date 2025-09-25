package com.exam.shops;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile extends AppCompatActivity {
LinearLayout resetRevenue,setHoliHigh,changePin,faq;
TextView txtShopName ,txtShopPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
      resetRevenue = findViewById(R.id.resetRevenue);
        txtShopName  = findViewById(R.id.txtShopName);
        txtShopPhone = findViewById(R.id.txtShopPhone);
        setHoliHigh=findViewById(R.id.setHoliHigh);
        changePin = findViewById(R.id.changePin);
        faq = findViewById(R.id.faq);
        String shopName = getIntent().getStringExtra("shop_name");
        txtShopName.setText(shopName);
        String ShopPhone = getIntent().getStringExtra("Mobile_no");
        txtShopPhone.setText(ShopPhone);


        resetRevenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, SetRevenueFigures.class);
                i.putExtra("ShopName", shopName);
                startActivity(i);
                Log.d("ShopName", "ShopName is : " + shopName);
            }
        });
        setHoliHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, SetHoliHighDay.class);
                   startActivity(i);
            }
        });
        changePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, ResetMpin.class);
                    startActivity(i);
            }
        });

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Profile.this, FAQ.class);
                    startActivity(i);
            }
        });


    }
}