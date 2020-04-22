package com.utility.smartcitytraveller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Utility {

    public String getLoggedInUserName(Context context) {
        SharedPreferences sharedPref =  context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String UserName = sharedPref.getString( "logged_in_user", null);
        return UserName;
    }

    public void markLoggedIn(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.is_logged_in), true);
        editor.commit();
    }

    public void makeDecisionOfWhereToGo(Context context) {
        if (isLoggedIn(context)) {
//            goToMapsActivity();
            if (isProfileCompleted(context)) {
                goToUIntroActivity(context);
            } else {
                goToProfileActivity(context, "login");
            }

        }
    }

    public void saveUserNamePassword(Context context, String username, String password) {
        SharedPreferences sharedPref =  context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String allUserNames = sharedPref.getString( "all_user_names", null);
        if (allUserNames == null ) {
            allUserNames = "" + username;
        } else {
            allUserNames += "," + username;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("all_user_names", allUserNames);
        editor.commit();

        editor.putString(username, password);
        editor.commit();

        editor.putString("logged_in_user", username);
        editor.commit();

    }

    public boolean isValidUserName(Context context, String username){
        SharedPreferences sharedPref =  context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String allUserNames = sharedPref.getString( "all_user_names", null);
        if(allUserNames != null){
            String[] arrUserNames = allUserNames.split(",");
            for (String uName : arrUserNames) {
                if (uName.equals(username)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isLoggedIn(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean( context.getString(R.string.is_logged_in), false);
    }

    public boolean isProfileCompleted(Context context) {
        String username = getLoggedInUserName(context);
        SharedPreferences sharedPref =  context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String name1 = sharedPref.getString( username + "_name_1", null);
        String name2 = sharedPref.getString( username + "_name_2", null);
        String name3 = sharedPref.getString( username + "_name_3", null);

        String num1 = sharedPref.getString( username + "_number_1", null);
        String num2 = sharedPref.getString( username + "_number_2", null);
        String num3 = sharedPref.getString( username + "_number_3", null);

        String nameY = sharedPref.getString( username + "_Your_Name", null);

        if (name1 != null && name1.trim() != "" && name2 != null && name2.trim() != "" && name3 != null && name3.trim() != "" &&
                num1 != null && num1.trim() != "" && num2 != null && num2.trim() != "" && num3 != null && num3.trim() != "" &&
                nameY != null && nameY.trim() != "") {
            return true;
        }
        return false;
    }

    public void goToUIntroActivity(Context packageContext) {
        Intent mapsScreen = new Intent(packageContext, IntroActivity.class);
        mapsScreen.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        packageContext.startActivity(mapsScreen);
    }

    public void goToProfileActivity(Context packageContext, String from) {
        Intent mapsScreen = new Intent(packageContext, ProfileActivity.class);
        mapsScreen.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        mapsScreen.putExtra("coming_from", from);
        packageContext.startActivity(mapsScreen);
    }

    public void goToLoginActivity(Context packageContext) {
        Intent mapsScreen = new Intent(packageContext, LoginActivity.class);
        mapsScreen.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        packageContext.startActivity(mapsScreen);
    }

    public void goToMapsActivity(Context packageContext) {
        Intent mapsScreen = new Intent(packageContext, MapsActivity.class);
        mapsScreen.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        packageContext.startActivity(mapsScreen);
    }
}
