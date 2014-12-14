package org.gtlp.yasb;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
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
            SoundActivity.webView.loadUrl("http://gtlp.lima-city.de");
            SoundActivity.webView.setWebViewClient(new WebViewClient() {
                boolean done = false;

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (!done) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Referer", "http://gtlp.lima-city.de");
                        SoundActivity.webView.loadUrl("http://www.4b42.com/", headers);
                        if (BuildConfig.DEBUG) Log.d("YASB", "Check #2: PASS");
                        done = true;
                    }
                }
            });
            if (BuildConfig.DEBUG) Log.d("YASB", "Check #1: PASS");
            downloadInfoFile();
        } catch (Exception ignored) {

        }
    }

    private void downloadInfoFile() {
        try {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(InitHelper.infoFile.lastModified());
            cal.add(GregorianCalendar.HOUR, Integer.parseInt(SoundActivity.preferences.getString("update_interval", "24")));
            if (BuildConfig.DEBUG) {
                Log.d("YASB", cal.getTime().toString());
                Log.d("YASB", cal.getTimeInMillis() + "<" + System.currentTimeMillis());
            }
            if (cal.getTimeInMillis() > System.currentTimeMillis()) {
                Scanner sc = new Scanner(InitHelper.OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/info"));
                FileOutputStream fos = new FileOutputStream(InitHelper.infoFile);
                while (sc.hasNext()) {
                    String s = sc.nextLine() + "\n";
                    fos.write(s.getBytes());
                }
                fos.close();
                sc.close();
            }
            if (BuildConfig.DEBUG) Log.d("YASB", "Loaded info.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
