package com.example.wot.childapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

SharedPreferences sharedPreferences;
    boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("CHILD",MODE_PRIVATE);
        flag=sharedPreferences.getBoolean("flag",false);
        thread.start();
    }
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                if(flag)
                {
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(MainActivity.this, SignupActivity.class));
                    finish();
                }

            }
        }
    });
}
