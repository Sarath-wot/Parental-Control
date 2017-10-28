package com.example.wot.childapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_OK;

public class SignupActivity extends AppCompatActivity {

    EditText ename,edob,email,ephone,epid;
    String name,dob,mail,phone,pid,gender;
    RadioGroup radioGroup;
    Button button;
    boolean flag = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ename = (EditText)findViewById(R.id.childname);
        edob = (EditText)findViewById(R.id.childdob);
        email = (EditText)findViewById(R.id.childmail);
        ephone = (EditText)findViewById(R.id.childphone);
        epid = (EditText)findViewById(R.id.childpid);
        radioGroup = (RadioGroup)findViewById(R.id.rg);
        button = (Button) findViewById(R.id.signupbtn);

        sharedPreferences = getSharedPreferences("CHILD",MODE_PRIVATE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = radioGroup.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton)findViewById(id);
                gender = rb.getText().toString();
                name = ename.getText().toString().trim();
                dob = edob.getText().toString().trim();
                mail = email.getText().toString().trim();
                phone = ephone.getText().toString().trim();
                pid = epid.getText().toString().trim();

                if(name.isEmpty())
                {



                    ename.setError("Enter your name");
                    return;
                }

                String regEx ="^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d{2}$";

                Matcher matcherObj = Pattern.compile(regEx).matcher(dob);
                if (!matcherObj.matches()||dob.isEmpty())
                {
                    Toast.makeText(SignupActivity.this, "Enter valid date of birth", Toast.LENGTH_SHORT).show();
                    return;
                }


                if(mail.isEmpty()|| !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches())
                {
                    email.setError("Enter valid email");
                    return;
                }

                if(phone.isEmpty()&& Patterns.PHONE.matcher(phone).matches())
                {
                    ephone.setError("Enter your phone number");
                    return;

                }


                new RegisterTask().execute(name,dob,gender,phone,mail,pid);
            }
        });
    }
    private class RegisterTask extends AsyncTask<String,String,String>
    {
        ProgressDialog pd = new ProgressDialog(SignupActivity.this);
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

                url=new URL("https://app-1503993646.000webhostapp.com/parentchild/childsignup.php");

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
                builder.appendQueryParameter("name",strings[0]);
                builder.appendQueryParameter("dob",strings[1]);
                builder.appendQueryParameter("gender",strings[2]);
                builder.appendQueryParameter("phone",strings[3]);
                builder.appendQueryParameter("email",strings[4]);
                builder.appendQueryParameter("pcode",strings[5]);



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
                else
                    return "unsuccessfull";

            } catch (IOException e1) {
                e1.printStackTrace();
                return "Exception";
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            if(s.equalsIgnoreCase("success"))
            {
                startActivity(new Intent(SignupActivity.this,HomeActivity.class));
            }

            else if (s.equalsIgnoreCase("exception") || s.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(SignupActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

            }
            else if(s.equals("Already registered"))
            {
                Toast.makeText(SignupActivity.this,"You are already registered",Toast.LENGTH_LONG).show();
            }
            else if(s.equals("parent not found"))
            {
                Toast.makeText(SignupActivity.this,"Parent not found",Toast.LENGTH_LONG).show();
            }
            else {
                flag=true;
               editor = sharedPreferences.edit();
                editor.putString("CID",s);
                editor.putBoolean("flag",flag);
                editor.apply();
                startActivity(new Intent(SignupActivity.this,HomeActivity.class));
                finish();
            }

        }
    }
}
