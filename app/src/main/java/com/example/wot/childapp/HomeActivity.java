package com.example.wot.childapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

public class HomeActivity extends AppCompatActivity {

    Button start,stop;
    TrackGPS trackGPS;
    String lon,lat,status,cid;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        start = (Button)findViewById(R.id.startbtn);
        stop = (Button)findViewById(R.id.stopbtn);
        sharedPreferences = getSharedPreferences("CHILD",MODE_PRIVATE);

        cid = sharedPreferences.getString("CID","no cid");

        new Task().execute(cid);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackGPS = new TrackGPS(HomeActivity.this);
                if(trackGPS.canGetLocation())
                {
                    lon=String.valueOf(trackGPS.getLongitude());
                    lat=String.valueOf(trackGPS.getLatitude());
                }
                else
                {
                    trackGPS.showSettingsAlert();
                }
                Intent serviceIntent = new Intent(HomeActivity.this, SendService.class);

             //   Bundle data = new Bundle();
              //  data.putInt("OperationType", 99);
              //  data.putString("LAT", lat);
               // serviceIntent.putExtras(data);

                startService(serviceIntent);
                start.setEnabled(false);
                stop.setEnabled(true);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start.setEnabled(true);
                stop.setEnabled(false);
                stopService(new Intent(HomeActivity.this,SendService.class));
            }
        });
    }

    private class Task extends AsyncTask<String,String,String>
    {
        ProgressDialog pd = new ProgressDialog(HomeActivity.this);
        HttpURLConnection connection;
        URL url;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Loading...");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                url=new URL("https://app-1503993646.000webhostapp.com/parentchild/getaccess.php");

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
            try {
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder =new Uri.Builder();
                builder.appendQueryParameter("cid",strings[0]);



                String query = builder.build().getEncodedQuery();

                OutputStream os=connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                connection.connect();

                int rc = connection.getResponseCode();
                if(rc == HTTP_OK)
                {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb=new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null)
                    {
                        sb.append(line);
                    }
                    return sb.toString();
                }
                else{
                    Log.i("Error","Unsuccessfulcode"+rc);
                    return "unsuccessfull";
                }


            } catch (IOException e1) {
                e1.printStackTrace();
                return "Exception";
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            Log.i("HomeResult",s);
            if(s.equals("0 results"))
            {
                Toast.makeText(HomeActivity.this, "Data not available", Toast.LENGTH_SHORT).show();
                finish();
            }

            else if(s.equals("true")) {
                Toast.makeText(HomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                status = "true";
            }
            else if(s.equals("false")) {
                status = "false";
                Toast.makeText(HomeActivity.this, "You have no permission to open this app", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if(s.equalsIgnoreCase("exception") || s.equalsIgnoreCase("unsuccessful")){

                Toast.makeText(HomeActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
                finish();

            }
            else {
                finish();
            }


        }
    }
}
