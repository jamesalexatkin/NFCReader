package com.example.nfcreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CallAPI extends AsyncTask<String, String, String> {

    Context context;
    Server server;
    TextView txtUnlock;
    String dataToPost;
    String room;
    String unlockText = "Room unlocked!";
    String lockText = "Room stays shut";

    public CallAPI(Context context, Server server, TextView txtUnlock, String dataToPost, String room) {
       this.context = context;
       this.server = server;
       this.txtUnlock = txtUnlock;
       this.dataToPost = dataToPost;
       this.room = room;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = "http://moot.samchatfield.com/api/room/" + room + "/unlock/" + dataToPost;

        String response = "";
        String outputText = "";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            InputStream in = conn.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in);

            int inputStreamData = inReader.read();
            while (inputStreamData != -1) {
                char currentData = (char) inputStreamData;
                inputStreamData = inReader.read();
                response += currentData;
            }

            JSONObject jsonResponse = new JSONObject(response);
            boolean unlock = jsonResponse.getBoolean("unlock");
            if (unlock) {
                outputText = unlockText;
                server.nfcUnlockNotify();
            } else {
                outputText = lockText;
            }

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            response = conn.getResponseCode() + ": " + conn.getResponseMessage();

            conn.disconnect();

        } catch (FileNotFoundException e) {
            Log.i("EXCEPTION", e.getMessage());
            outputText = "Booking not found";
        } catch (Exception e) {
            Log.i("EXCEPTION", e.getMessage());
            outputText = e.getMessage();
        }

        return outputText;
    }

    @Override
    protected void onPostExecute(String unlocked) {
        if (unlocked.equals(unlockText)) {
            txtUnlock.setTextColor(0xFF00FF00);
        } else {
            txtUnlock.setTextColor(0xFFFF0000);
        }
        txtUnlock.setText(unlocked);
    }
}