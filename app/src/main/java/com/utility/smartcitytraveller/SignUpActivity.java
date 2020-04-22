package com.utility.smartcitytraveller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etUserName;
    private TextInputEditText etPassword;
    private Button btnCreateProfile;
    Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up);
        etUserName = findViewById(R.id.et_signup_username);
        etPassword = findViewById(R.id.et_signup_password);
        btnCreateProfile = findViewById(R.id.btn_create);
        utility = new Utility();
        btnCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (utility.isValidUserName(getApplicationContext(), etUserName.getText().toString())) {
                    if(!etPassword.getText().toString().trim().equals("") && etPassword.length() >= 4) {
                        utility.saveUserNamePassword(getApplicationContext(), etUserName.getText().toString(), etPassword.getText().toString());
                        utility.markLoggedIn(getApplicationContext());
                        utility.makeDecisionOfWhereToGo(getApplicationContext());
                    } else {
                        Toast.makeText(SignUpActivity.this, "Invalid Password. Create a password of minimum 4 characters.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "User Name Already Exist. Try a new one.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
