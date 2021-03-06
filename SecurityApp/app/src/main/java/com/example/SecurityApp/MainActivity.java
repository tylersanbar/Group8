package com.example.SecurityApp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.util.regex.Pattern;

import SecurityApp.R;

//CREDITS
//https://github.com/aritraroy/PatternLockView
//https://stackoverflow.com/questions/32534076/what-is-the-best-way-to-do-a-button-group-that-can-be-selected-and-activate-inde/32545086
public class MainActivity extends AppCompatActivity {
    DataBase db;
    Button pattern_lock_button, scrabble_lock_button, add_user_button;
    Spinner user_spinner;
    EditText add_user_text;
    int currentUserID;
    Gson gson;
    String json;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //Network permission stuff
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Create the database
        db = new DataBase();
        gson = new Gson();
        json = gson.toJson(db);

        //Create the widgets
        pattern_lock_button = (Button)findViewById(R.id.pattern_lock_button);
        scrabble_lock_button = (Button)findViewById(R.id.scrabble_lock_button);
        add_user_button = (Button)findViewById(R.id.add_user_button);
        user_spinner = findViewById(R.id.user_spinner);
        add_user_text = findViewById(R.id.add_user_text) ;
        //Create adapter for spinner
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        user_spinner.setAdapter(adapter);
        adapter.addAll(db.getUserNames());
        user_spinner.setSelection(0);



        // Button Listeners
        pattern_lock_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUserID = db.findUserByName(user_spinner.getSelectedItem().toString()).getId();
                Intent startPattern = new Intent(MainActivity.this, PatternLock.class);
                startPattern.putExtra("Database", json);
                startPattern.putExtra("UID", currentUserID);
                startActivity(startPattern);
            }
        });
        scrabble_lock_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUserID = db.findUserByName(user_spinner.getSelectedItem().toString()).getId();
                Intent startScrabble = new Intent(MainActivity.this, ScrabbleLock.class);;
                startScrabble.putExtra("Database", json);
                startScrabble.putExtra("UID", currentUserID);
                startActivity(startScrabble);
            }
        });
        add_user_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.addUser(add_user_text.getText().toString())){
                    adapter.clear();
                    adapter.addAll(db.getUserNames());
                }
            }
        });


    }
    /** Called when the user taps the Lock button */
    public void displayLockScreen(View view) {
      setContentView(view);
    }



}