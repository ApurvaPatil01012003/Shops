package com.exam.shops;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FAQ extends AppCompatActivity {
    ExpandableListView faqListView;
    Spinner languageSpinner;
    List<String> questionList;
    HashMap<String, String> answerMap;
    FAQAdapter faqAdapter;
    TextView faqTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faq);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        faqListView = findViewById(R.id.faqListView);
        questionList = new ArrayList<>();
        answerMap = new HashMap<>();
        languageSpinner = findViewById(R.id.languageSpinner);
        faqTitle = findViewById(R.id.faqTitle);


        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"हिंदी", "English"});
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(langAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boolean isHindi = i == 0;
                faqTitle.setText(isHindi ? "My Biz Tracker – सवाल और जवाब" : "My Biz Tracker – Questions and Answers");
                loadFAQs(isHindi);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        faqAdapter = new FAQAdapter(this, questionList, answerMap);
        faqListView.setAdapter(faqAdapter);

    }


    private void loadFAQs(boolean isHindi) {
        questionList = new ArrayList<>();
        answerMap = new HashMap<>();

        if (isHindi) {
            addFAQ("सवाल 1: My Biz Tracker क्या है? ", "जवाब: यह एक आसान ऐप है जो खासतौर पर कपड़े के व्यापारियों के लिए बनाया गया है। इसकी मदद से आप रोज की बिक्री लिख सकते हैं, महीने और साल का हिसाब देख सकते हैं, और अपना टार्गेट तय करके उसे पूरा करने की योजना बना सकते हैं।\n");

            addFAQ("सवाल 2: मैं रोज़ की बिक्री कैसे दर्ज करूं?",
                    "जवाब: ऐप के Daily Sales Entry भाग में जाएं, तारीख चुनें, दिन की कुल बिक्री लिखें और सेव करें।");

            addFAQ("सवाल 3: क्या मैं अपनी बिक्री बाद में बदल सकता हूँ?",
                    "जवाब:हाँ, आप किसी भी तारीख की बिक्री को बाद में बदल या ठीक कर सकते हैं।");


            addFAQ("सवाल 4: महीने की कुल बिक्री कहाँ दिखेगी?",
                    "जवाब: आपकी रोज की एंट्री से महीने की कुल बिक्री अपने-आप बनती है। यह MTD नाम के भाग में दिखाई देती है।");


            addFAQ("सवाल 5: साल की कुल बिक्री कैसे देख सकते हैं?",
                    "जवाब:YTD भाग में जाकर आप पूरे साल की बिक्री और अपने सालाना टार्गेट का पूरा हुआ प्रतिशत देख सकते हैं।");


            addFAQ("सवाल 6: कैसे पता चलेगा कि मेरा व्यापार बढ़ रहा है या घट रहा है?",
                    "जवाब: Growth Analysis में पिछले दो साल की कुल बिक्री भरें। ऐप आपको साफ-साफ दिखाएगा कि इस साल आपका व्यापार बढ़ा है या घटा है।");


            addFAQ("सवाल 7: साल का टार्गेट कैसे तय करें?",
                    "जवाब: पिछले दो साल की बिक्री भरने के बाद ऐप आपको एक सही सालाना टार्गेट सजेस्ट करेगा और उसे हर महीने के लिए बाँट देगा।");


            addFAQ("सवाल 8: क्या मैं जान सकता हूँ कि मैं अपने टार्गेट पर हूँ या नहीं?",
                    "जवाब: हाँ, ऐप का डैशबोर्ड आपको दिखाएगा कि आपने अपने महीने के टार्गेट के मुकाबले कितनी बिक्री की है।");


            addFAQ("सवाल 9: अगर मैं कुछ दिन बिक्री लिखना भूल जाऊँ तो क्या होगा?",
                    "जवाब: आप किसी भी तारीख पर जाकर पिछली बिक्री बाद में भी दर्ज कर सकते हैं।");


            addFAQ("सवाल 10: क्या इस ऐप से मैं कई दुकानों का हिसाब रख सकता हूँ?",
                    "जवाब: फिलहाल यह ऐप एक ही दुकान के लिए है। आने वाले समय में इसमें कई दुकानों का ऑप्शन जोड़ा जा सकता है।");


            addFAQ("सवाल 11: क्या मेरा डेटा सुरक्षित है? ",
                    "जवाब: हाँ, आपका पूरा डेटा आपके मोबाइल में सुरक्षित रहता है। यह ऐप ऑफलाइन भी काम करता है।");


            addFAQ("सवाल 12: क्या मैं अपनी बिक्री की रिपोर्ट निकाल सकता हूँ?",
                    "जवाब: हाँ, Reports वाले भाग में जाकर आप किसी भी समय की रिपोर्ट PDF में डाउनलोड कर सकते हैं।");


            addFAQ("सवाल 13: अगर मैं ऐप का इस्तेमाल करना भूल जाऊँ तो?",
                    "जवाब: आप ऐप में डेली रिमाइंडर या नोटिफिकेशन चालू कर सकते हैं, ताकि आप रोज समय पर बिक्री भर सकें।");


            addFAQ("सवाल 14: क्या यह ऐप सिर्फ कपड़े के व्यापारियों के लिए है?  ",
                    "जवाब: हाँ, यह खासतौर पर कपड़े के व्यापारियों के लिए बनाया गया है, लेकिन दूसरे दुकानवाले भी इसे उपयोग में ले सकते हैं।");


            addFAQ("सवाल 15: अगर मुझे ऐप चलाने में कोई दिक्कत हो तो मैं मदद कहाँ से लूं? ", "जवाब: आप हमारी टीम से ईमेल पर संपर्क कर सकते हैं: rm@coachamol.com");
        } else {

            addFAQ("Question 1: What is My Biz Tracker?  ", "Answer:My Biz Tracker is a simple app designed especially for garment retailers. With this app, you can record daily sales, track your monthly and yearly performance, and set realistic targets to grow your business.");
            addFAQ("Question 2: How do I enter my daily sales?", "Answer: Go to the \"Daily Sales Entry\" section, select the date, enter your total sales for the day, and save it.");
            addFAQ("Question 3: Can I edit or change my sales later?", "Answer: Yes, you can select any date and update or correct your sales data at any time.");
            addFAQ(" Question 4: Where can I see my monthly sales total?", "Answer: Your monthly total is automatically calculated from daily entries and shown in the \"MTD\" (Month-To-Date) section.\n");
            addFAQ("Question 5: How can I view my yearly sales?", "Answer: You can view your total yearly sales and how much of your annual target is completed in the \"YTD\" (Year-To-Date) section.");
            addFAQ("Question 6: How will I know if my business is growing or declining?", "Answer: In the \"Growth Analysis\" section, enter your last two years’ sales figures. The app will clearly show whether your business has grown or declined.\n");
            addFAQ(" Question 7: How do I set a sales target for the year? ", "Answer: After you enter your past two years’ sales data, the app will suggest a practical yearly target and automatically divide it month-wise.\n");
            addFAQ("Question 8: Can I track whether I am meeting my target or not? ", "Answer: Yes, the dashboard will compare your actual monthly sales with your set target and show whether you are on track or falling behind.\n");
            addFAQ("Question 9: What if I forget to enter sales for a few days?  ", "Answer: You can go back to any date and enter the missed data. However, entering sales regularly is the best habit for accuracy.\n");
            addFAQ("Question 10: Can I use this app for multiple shops?", "Answer: Currently, the app supports only one store. Multi-store support may be added in future versions.");
            addFAQ("Question 11: Is my data safe? ", "Answer: Yes, your data is completely safe. It is stored securely on your phone and works even without an internet connection.\n");
            addFAQ("Question 12: Can I download or print my sales reports?", "Answer: Yes, go to the \"Reports\" section, choose the desired time period, and download a PDF summary of your sales.");
            addFAQ("Question 13: What if I forget to use the app daily?  ", "Answer: You can set daily reminders and notifications in the app so that you stay consistent with your entries.\n");
            addFAQ("Question 14: Is this app only for garment shop owners?", "Answer: Yes, it is specially designed for garment business owners, but other retail shop owners can also use it.");
            addFAQ("Question 15: Where can I get help if I face any issues? ", "Answer: You can contact our support team by email at: rm@coachamol.com");


        }

        faqAdapter = new FAQAdapter(this, questionList, answerMap);
        faqListView.setAdapter(faqAdapter);
    }

    private void addFAQ(String question, String answer) {
        questionList.add(question);
        answerMap.put(question, answer);
    }
}


