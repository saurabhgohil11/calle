package com.evadroid.calle.help;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evadroid.calle.AppGlobals;
import com.evadroid.calle.R;


public class HelpActivity extends AppCompatActivity {
    LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (!AppGlobals.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        rootLayout = (LinearLayout) findViewById(R.id.help_root_layout);

        initQuetions();
    }

    private void initQuetions() {
        String[] questions;
        String[] answers;
        questions = getResources().getStringArray(R.array.help_questions);
        answers = getResources().getStringArray(R.array.help_answers);
        LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView question;
        TextView answer;
        for (int i = 1; i <= questions.length; i++) {
            View v = li.inflate(R.layout.help_item, null);
            // fill in any details dynamically here
            question = (TextView) v.findViewById(R.id.question);
            answer = (TextView) v.findViewById(R.id.answer);
            question.setText((i) + ". " + questions[i - 1]);
            answer.setText(answers[i - 1]);
            rootLayout.addView(v);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
