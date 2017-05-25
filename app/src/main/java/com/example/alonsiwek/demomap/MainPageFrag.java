package com.example.alonsiwek.demomap;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dor on 1/11/2017.
 * This class is the Fragment calls of the main screen
 */

public class MainPageFrag extends Fragment {

    Boolean mIsRunning = false;
    String mdataOfUsers = null;
    public String lat = null;
    public String lng = null;
    public String userName = null;
    public String timeStamp = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.main_screen_frag, null);

        // get the widgets reference from Fragment XML layout
        ImageButton btn_go = (ImageButton) view.findViewById(R.id.go_walking_btn);

        //TODO: change the time....
        // call to service of GETTING the data from DB
        Intent intent = new Intent(getActivity(), UserAtApp.class);
        PendingIntent pintent = PendingIntent.getService(getActivity(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10*1000, pintent);

        // Receive data from UserAtApp service
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                broadcastReceiver, new IntentFilter("DATA_OF_USERS"));

        //TODO: decide of earse TOAST
        ///////////////   part of toast //////////////////////////////

        // Set a click listener for Fragment button
        btn_go.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {

                // Get the application context
                Toast toast = new Toast(getContext());
                // Set the Toast display position layout center
                toast.setGravity(Gravity.CENTER, 0, 0);

                LayoutInflater inflater = getLayoutInflater(savedInstanceState);
                View layout = inflater.inflate(R.layout.go_massage_toast, null);

                // Set the Toast duration
                toast.setDuration(Toast.LENGTH_LONG);

                // Set the Toast custom layout
                toast.setView(layout);
                toast.show();

                ///////////////  END part of toast //////////////////////////////

                ////////////// Part of UPDATE DB ////////////////////////////////
                mIsRunning = true;

                /* update DB only when mIsRunning == true.
                 * update DB only when mIsRunning == false will be with FINISH button.
                 */
                Log.d(MainPageFrag.class.toString(),"mIsRunning:" +  String.valueOf(mIsRunning));
                if (mIsRunning == true) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                updateRunningState(mIsRunning);
                            } catch (IOException e) {
                                Log.e(MainPageFrag.class.toString(), e.toString());
                                e.printStackTrace();
                                return;
                            }
                        }
                    }).start();
                }

                ////////////// END of UPDATE DB ////////////////////////////////
            }
        });

        return view;
    }

    static void updateRunningState(Boolean state) throws IOException {

        JSONObject json = new JSONObject();
        URL url;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            json.put("is_running", state);
        } catch (JSONException e) {
            Log.e(MainPageFrag.class.toString(), "json error: " + e.toString());
            e.printStackTrace();
            return;
        }

        try {
           url = new URL(Constants.SERVER_URL + Constants.LOC_STATUS_PATH + Constants.user_id);
        } catch (MalformedURLException e) {
            Log.e(MainPageFrag.class.toString(), "error at url: "  + e.toString());
            e.printStackTrace();
            return;
        }

        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        //set the time to read from url - in miliSec
        urlConnection.setReadTimeout(10000);
        //set time to be used when opening a communications link
        // to the resource referenced by this URLConnection connect to url
        urlConnection.setConnectTimeout(10000);
        urlConnection.setRequestMethod("PUT");

        // enable output
        urlConnection.setDoOutput(true);
        //set header
        urlConnection.setRequestProperty("Content-Type","application/json");

        urlConnection.connect();

        Log.d(MainPageFrag.class.toString(),"Connecting");

        //Post data to server
        OutputStream outputStream = null;

        outputStream = urlConnection.getOutputStream();
        bufferedWriter = new BufferedWriter((new OutputStreamWriter(outputStream)));
        bufferedWriter.write(json.toString());

        Log.d(MainPageFrag.class.toString(),"written to server");
        bufferedWriter.flush();

        if ( urlConnection.getResponseCode() != 200) {
            Log.e(MainPageFrag.class.toString()," response code error:" + urlConnection.getResponseCode());
            return;
        }

        // disconnect
        urlConnection.disconnect();
    }

    /**
     * Receive the JSON string from UserAtApp servic, and update the class member : mDataOfUsers
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the type of message from the service
            mdataOfUsers = intent.getStringExtra("DATA_OF_USERS");

            Log.d("MainPageFrag","mdataOfUsers: " + mdataOfUsers.toString());

        }
    };





    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this.getContext())
                .registerReceiver(broadcastReceiver, new IntentFilter("NOW"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}



