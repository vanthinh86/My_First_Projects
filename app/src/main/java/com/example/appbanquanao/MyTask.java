package com.example.appbanquanao;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String urlStr = params[0];
        String result = "";

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            result = sb.toString();
            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // xử lý kết quả ở UI thread
        System.out.println("onPostExecute: " + result);
        //Log.d("API_RESULT", result);
    }
}
