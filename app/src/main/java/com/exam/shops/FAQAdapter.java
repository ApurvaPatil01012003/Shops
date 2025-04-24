package com.exam.shops;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class FAQAdapter extends BaseExpandableListAdapter {

    Context context;
    List<String> questions;
    HashMap<String, String> answers;

    public FAQAdapter(Context context, List<String> questions, HashMap<String, String> answers) {
        this.context = context;
        this.questions = questions;
        this.answers = answers;
    }

    @Override
    public int getGroupCount() {
        return questions.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1; // Each question has one answer
    }

    @Override
    public Object getGroup(int groupPosition) {
        return questions.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return answers.get(questions.get(groupPosition));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String question = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
        }
        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(question);
        tv.setTextSize(16);
        tv.setPadding(32, 24, 24, 24);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        String answer = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(answer);
        tv.setPadding(64, 16, 16, 16);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
