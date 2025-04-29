package com.exam.shops;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class SetRevenueFigures extends AppCompatActivity {
    EditText ResetFirstTurnOver,ResetSecondTurnOver,edtResetGrowth;
    TextView txtResetFirstTurnOver,txtResetSecondTurnOver,txtResetGrowth,ResetEGFY,txtResetGrowthShow,ResetShopName;
    Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_revenue_figures);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ResetFirstTurnOver = findViewById(R.id.ResetFirstTurnOver);
        ResetSecondTurnOver = findViewById(R.id.ResetSecondTurnOver);
        edtResetGrowth = findViewById(R.id.edtResetGrowth);
        txtResetFirstTurnOver = findViewById(R.id.txtResetFirstTurnOver);
        txtResetSecondTurnOver = findViewById(R.id.txtResetSecondTurnOver);
        txtResetGrowth = findViewById(R.id.txtResetGrowth);
        ResetEGFY = findViewById(R.id.ResetEGFY);
        txtResetGrowthShow = findViewById(R.id.txtResetGrowthShow);
        btnReset = findViewById(R.id.btnReset);
        ResetShopName = findViewById(R.id.ResetShopName);

        String name= getIntent().getStringExtra("ShopName");
        ResetShopName.setText(name);

        txtResetFirstTurnOver.setText("FY "+getSecondLastFinancialYear()+" Revenue");
        txtResetSecondTurnOver.setText("FY "+getLastFinancialYear()+" Revenue");
        ResetEGFY.setText("Expected Growth "+getCurrentFinancialYear());



        btnReset.setOnClickListener(v -> {

            String fturnoverStr = ResetFirstTurnOver.getText().toString().trim();
            String sturnoverStr = ResetSecondTurnOver.getText().toString().trim();
            String editGrowthStr = edtResetGrowth.getText().toString().trim();
            String growthShowStr = txtResetGrowthShow.getText().toString().replace("%", "").trim();
            String growthPercentStr = txtResetGrowth.getText().toString().replace("%", "").trim();
            if (TextUtils.isEmpty(fturnoverStr) || TextUtils.isEmpty(sturnoverStr) ||
                    TextUtils.isEmpty(editGrowthStr) || TextUtils.isEmpty(growthShowStr)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(SetRevenueFigures.this);
            builder.setMessage("Do You Want To Change The Revanue ? ");
            builder.setTitle("Alert");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                try {
                    int firstTurnover = Integer.parseInt(fturnoverStr);
                    int secondTurnover = Integer.parseInt(sturnoverStr);
                    int edtGrowth = Integer.parseInt(editGrowthStr);
                    int result = Math.round(Float.parseFloat(growthShowStr));
                    int growthPercent = Math.round(Float.parseFloat(growthPercentStr));


                    SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("Fturnover", firstTurnover);
                    editor.putInt("Sturnover", secondTurnover);
                    editor.putInt("Growth_Per", growthPercent);
                    editor.putInt("editGrowth", edtGrowth);
                    editor.putInt("result", result);
                    editor.apply();

                    SharedPreferences yoyPrefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
                    SharedPreferences.Editor yoyEditor = yoyPrefs.edit();

                    String[] monthsShort = {"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    int startYear = (Calendar.getInstance().get(Calendar.MONTH) >= Calendar.APRIL) ? year : year - 1;

                    int monthlyTarget = edtGrowth / 12;

                    for (String month : monthsShort) {
                        int dataYear = (month.equals("Jan") || month.equals("Feb") || month.equals("Mar")) ? startYear + 1 : startYear;
                        String key = "expected_" + month + "_" + dataYear;
                        yoyEditor.putFloat(key, monthlyTarget);
                    }

                    yoyEditor.apply();





                    Toast.makeText(SetRevenueFigures.this, "Revenue Reset Successfully", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(SetRevenueFigures.this, "Invalid input values", Toast.LENGTH_SHORT).show();
                }
            });


            builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                dialog.cancel();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        });


        ResetFirstTurnOver.addTextChangedListener(new SecondActivity.SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrowth();
            }
        });

        ResetSecondTurnOver.addTextChangedListener(new SecondActivity.SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrowth();

            }
        });



        ResetSecondTurnOver.addTextChangedListener(new SecondActivity.SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                calculateGrowthResult();
            }
        });
        edtResetGrowth.addTextChangedListener(new SecondActivity.SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                calculateGrowthResult();
            }
        });

    }



    public abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }



    private String getSecondLastFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // Log.d("Year","Current6 year" +year + ""+month);

        year = year - 2;
        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
            // Log.d("startyear","start year is in if : "+startYear+" "+endYear);
        } else {
            startYear = year - 1;
            endYear = year;
            // Log.d("startyear","start year is in else : "+startYear+" "+endYear);

        }
        return startYear + "_" + String.valueOf(endYear).substring(2);


    }


    private String getLastFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // Log.d("Year","Current6 year" +year + ""+month);

        year = year - 1;
        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
            //  Log.d("startyear","start year is in if : "+startYear+" "+endYear);
        } else {
            startYear = year - 1;
            endYear = year;
            // Log.d("startyear","start year is in else : "+startYear+" "+endYear);

        }
        return startYear + "_" + String.valueOf(endYear).substring(2);


    }

    private String getCurrentFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // Log.d("Year","Current6 year" +year + ""+month);

        //year = year - 1;
        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
            //  Log.d("startyear","start year is in if : "+startYear+" "+endYear);
        } else {
            startYear = year - 1;
            endYear = year;
            // Log.d("startyear","start year is in else : "+startYear+" "+endYear);

        }
        return startYear + "_" + String.valueOf(endYear).substring(2);


    }


    private void calculateGrowth() {
        String first = ResetFirstTurnOver.getText().toString().trim();
        String second = ResetSecondTurnOver.getText().toString().trim();

        if (!first.isEmpty() && !second.isEmpty()) {
            try {
                int firstValue = Integer.parseInt(first);
                int secondValue = Integer.parseInt(second);
                int growth = secondValue - firstValue;

                //  Log.d("Growth", "Growth is : " + growth);

                if (firstValue != 0) {
                    float percentage = ((float) growth / firstValue) * 100;
                    String formatted = String.format("%.2f", percentage);
                    txtResetGrowth.setText(formatted + " %");

                    if (percentage >= 0) {
                        txtResetGrowth.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        txtResetGrowth.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    txtResetGrowth.setText("∞");
                    txtResetGrowth.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            } catch (NumberFormatException e) {
                txtResetGrowth.setText("0 %");
                txtResetGrowth.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            txtResetGrowth.setText("0 %");
            txtResetGrowth.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }


    public void calculateGrowthResult() {


        String second = ResetSecondTurnOver.getText().toString().trim();
        String  Ex_Growth_FY = edtResetGrowth.getText().toString().trim();

        if (!Ex_Growth_FY.isEmpty() && !Ex_Growth_FY.isEmpty()) {
            try {
                int firstValue = Integer.parseInt(second);
                int secondValue = Integer.parseInt(Ex_Growth_FY);
                int growth = secondValue - firstValue;

                if (firstValue != 0) {
                    float percentage = ((float) growth / firstValue) * 100;
                    String formatted = String.format("%.2f", percentage);
                    txtResetGrowthShow.setText(formatted + " %");

                    if (percentage >= 0) {
                        txtResetGrowthShow.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        txtResetGrowthShow.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    txtResetGrowthShow.setText("∞");
                    txtResetGrowthShow.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            } catch (NumberFormatException e) {
                txtResetGrowthShow.setText("0 %");
                txtResetGrowthShow.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            txtResetGrowthShow.setText("0 %");
            txtResetGrowthShow.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

    }



}