package com.utility.smartcitytraveller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT = 190;
    ImageButton imgBtn1;
    ImageButton imgBtn2;
    ImageButton imgBtn3;

    TextInputEditText etName1;
    TextInputEditText etName2;
    TextInputEditText etName3;

    TextInputEditText etNumber1;
    TextInputEditText etNumber2;
    TextInputEditText etNumber3;

    TextInputEditText etYoueName;

    int contact = 0;

    Button btnSve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnSve = findViewById(R.id.btn_save);
        btnSve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String YourNamr = etYoueName.getText().toString();
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Your_Name", YourNamr);
                editor.commit();
                finish();
            }
        });
        imgBtn1 = findViewById(R.id.img_btn_1);
        imgBtn2 = findViewById(R.id.img_btn_2);
        imgBtn3 = findViewById(R.id.img_btn_3);
        etYoueName = findViewById(R.id.et_your_name);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String nameY = sharedPref.getString( "Your_Name", null);
        if (nameY != null) {
            etYoueName.setText(nameY + "");
        }

        imgBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact = 1;
                pickContact();
            }
        });

        imgBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact = 2;
                pickContact();
            }
        });

        imgBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact = 3;
                pickContact();
            }
        });

        etName1 = findViewById(R.id.et_emc_name_1);
        etName2 = findViewById(R.id.et_emc_name_2);
        etName3 = findViewById(R.id.et_emc_name_3);

        etNumber1 = findViewById(R.id.et_emc_number_1);
        etNumber2 = findViewById(R.id.et_emc_number_2);
        etNumber3 = findViewById(R.id.et_emc_number_3);
        setTextViews();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.new_logo);
        getSupportActionBar().setHideOffset(0);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Smart City Traveller</font>"));
    }

    public void pickContact() {

        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    try {
                        String contactNumber = null;
                        String contactName = null;
                        // getData() method will have the
                        // Content Uri of the selected contact
                        Uri uri = data.getData();
                        //Query the content uri
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        // column index of the phone number
                        int phoneIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER);
                        // column index of the contact name
                        int nameIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        contactNumber = cursor.getString(phoneIndex).replaceAll(" ", "");
                        contactName = cursor.getString(nameIndex);
                        // Set the value to the textviews
                        if (contact ==1){
                            etNumber1.setText(contactNumber);
                            etName1.setText(contactName);
                            setSharedPref("1", contactName, contactNumber);
                        } else if (contact == 2) {
                            etNumber2.setText(contactNumber);
                            etName2.setText(contactName);
                            setSharedPref("2", contactName, contactNumber);
                        } else if (contact == 3) {
                            etNumber3.setText(contactNumber);
                            etName3.setText(contactName);
                            setSharedPref("3", contactName, contactNumber);
                        } else {
                            Log.e("Profile", "Invalid contact id "  +contact);
                        }
//                        tvContactName.setText("Contact Name : ".concat(contactName));
//                        tvContactNumber.setText("Contact Number : ".concat(contactNumber));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void setSharedPref(String key, String name, String number) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("name_"+key, name);
        editor.putString("number_"+key, number);
        editor.commit();
    }

    public void setTextViews() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String name1 = sharedPref.getString( "name_1", null);
        String name2 = sharedPref.getString( "name_2", null);
        String name3 = sharedPref.getString( "name_3", null);

        String num1 = sharedPref.getString( "number_1", null);
        String num2 = sharedPref.getString( "number_2", null);
        String num3 = sharedPref.getString( "number_3", null);

        if (name1 != null) {
            etName1.setText(name1 + "");
        }
        if (name2 != null) {
            etName2.setText(name2 + "");
        }
        if (name3 != null) {
            etName3.setText(name3 + "");
        }

        if (num1 != null) {
            etNumber1.setText(num1 + "");
        }

        if (num2 != null) {
            etNumber2.setText(num2 + "");
        }

        if (num3 != null) {
            etNumber3.setText(num3 + "");
        }

    }
}
