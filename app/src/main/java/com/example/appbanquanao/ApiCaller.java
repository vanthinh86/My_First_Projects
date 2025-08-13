package com.example.appbanquanao;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiCaller {

    private static final String BASE_URL = "http://10.0.2.2:5000/api/";

    public interface ApiResponseListener {
        void onApiResponse(String response);
    }

    public static void callApi(String endpoint, String method, JSONObject body, ApiResponseListener listener) {
        new ApiTask(listener).execute(endpoint, method, body != null ? body.toString() : null);
    }

    public static void callApi(String endpoint, String method, JSONObject body, String token, ApiResponseListener listener) {
        new ApiTask(listener).execute(endpoint, method, body != null ? body.toString() : null, token);
    }

    private static class ApiTask extends AsyncTask<String, Void, String> {
        private ApiResponseListener listener;

        ApiTask(ApiResponseListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            String endpoint = params[0];
            String method = params[1];
            String jsonBody = params[2];
            String token = params.length > 3 ? params[3] : null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(BASE_URL + endpoint);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(method);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");

                if (token != null && !token.isEmpty()) {
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (jsonBody != null && (method.equals("POST") || method.equals("PUT"))) {
                    urlConnection.setDoOutput(true);
                    OutputStream os = urlConnection.getOutputStream();
                    os.write(jsonBody.getBytes("UTF-8"));
                    os.close();
                }

                int code = urlConnection.getResponseCode();
                Log.d("ApiCaller", "Response Code: " + code);

                BufferedReader br;
                if (code >= 200 && code < 300) {
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                }

                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
                br.close();

                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (listener != null) {
                listener.onApiResponse(result);
            }
        }
    }
}