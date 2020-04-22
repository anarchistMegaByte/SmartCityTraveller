package com.utility.smartcitytraveller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.hanks.htextview.base.AnimationListener;
import com.hanks.htextview.base.HTextView;
import com.hanks.htextview.fade.FadeTextView;
import com.hanks.htextview.typer.TyperTextView;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class IntroActivity extends AppCompatActivity {

    FadeTextView ftv;
    String nameY;
    Handler screenTimeHandler = new Handler();
    Utility utility = new Utility();
    String username = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_intro);
        username = utility.getLoggedInUserName(getApplicationContext());
        ftv = findViewById(R.id.textview);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        nameY = sharedPref.getString( username+"_Your_Name", null);
        if (nameY != null && nameY.trim().equals(""))  {
            nameY = "User";
        }
//        ftv.setText(nameY);


        TyperTextView ftvHeader = findViewById(R.id.textviewheader);
        ftvHeader.animateText("Hello");
        ftvHeader.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(HTextView hTextView) {
                ftv.animateText(nameY);
            }
        });

        ftv.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(HTextView hTextView) {
                screenTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        utility.goToMapsActivity(IntroActivity.this);
                        finish();
                    }
                }, 1500);

            }
        });

    }




}
