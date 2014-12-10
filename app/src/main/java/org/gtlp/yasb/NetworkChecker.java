package org.gtlp.yasb;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NetworkChecker extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        publishProgress();
        return null;
    }

    protected void onProgressUpdate(Void... params) {
        try {
            if (BuildConfig.DEBUG) Log.d("YASB", "Checking network");
            /*URL url = new URL("http://gtlp.lima-city.de/");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            http.setRequestMethod("GET");
            http.connect();
            if (http.getResponseCode() == HTTP_OK) { */
            SoundActivity.webView.loadUrl("http://gtlp.lima-city.de");
            if (BuildConfig.DEBUG) Log.d("YASB", "Check #1: PASS");
            downloadInfoFile();
            /*}
            url = new URL("http://www.4b42.com/");
            http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            http.setRequestProperty("Referer", "http://gtlp.lima-city.de/");
            http.setRequestMethod("GET");
            http.connect();
            if (http.getResponseCode() == HTTP_OK) { */
            Map<String, String> headers = new HashMap<>();
            headers.put("Referer", "http://gltp.lima-city.de");
            SoundActivity.webView.loadUrl("http://www.4b42.com/", headers);
            if (BuildConfig.DEBUG) Log.d("YASB", "Check #2: PASS");
            //int byteCount = http.getInputStream().read(new byte[http.getContentLength()]);
            //if (BuildConfig.DEBUG) Log.d("YASB", "Bytes: " + byteCount);
            //}
        } catch (Exception ignored) {

        }
    }

    private void downloadInfoFile() {
        try {
            Scanner sc = new Scanner(InitHelper.OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/info"));
            FileOutputStream fos = new FileOutputStream(InitHelper.infoFile);
            while (sc.hasNext()) {
                String s = sc.nextLine() + "\n";
                fos.write(s.getBytes());
            }
            fos.close();
            sc.close();
            if (BuildConfig.DEBUG) Log.d("YASB", "Loaded info.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
