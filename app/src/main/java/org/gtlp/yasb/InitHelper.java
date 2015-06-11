package org.gtlp.yasb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InitHelper extends AsyncTask<Void, Integer, Void> {

    public static final String SERVER_HOST = "http://giant.111mb.de/";
    public static final String HTTP_SOUNDS_INFO_FILE = SERVER_HOST + "getsoundsinfo.php";
    final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static boolean isNetworkAvailable = false;
    public List<FileInfo> fileInfos = new ArrayList<>();
    public boolean fileInfoSupplied = false;
    protected File infoFile;
    private File soundsDir;
    private AppCompatActivity activity;
    private int numberOfFilesToDownload = 0;

    public InitHelper(AppCompatActivity activity) {
        this.activity = activity;
        soundsDir = activity.getDir("sounds", Context.MODE_PRIVATE);
        infoFile = new File(activity.getDir("info", Context.MODE_PRIVATE), "info.json");
    }

    public static StringBuilder downloadInfoFile(File infoFile) {
        try {
            StringBuilder s = new StringBuilder();
            downloadFile(HTTP_SOUNDS_INFO_FILE, infoFile);
            for (String line : Files.readLines(infoFile, Charsets.UTF_8)) {
                s.append(line).append('\n');
            }
            SoundActivity.log(s.toString());
            isNetworkAvailable = true;
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void downloadFile(String strURL, File dest) {
        InputStream inputStream = null;
        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection) new URL(strURL).openConnection();
            httpConn.connect();
            SoundActivity.log(httpConn.getResponseMessage());
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
            if (inputStream != null) {
                FileOutputStream fos = new FileOutputStream(dest);
                int numRead;
                byte[] buffer = new byte[1024];
                try {
                    while ((numRead = inputStream.read(buffer)) >= 0) {
                        fos.write(buffer, 0, numRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    fos.close();
                }
                inputStream.close();
            }
            httpConn.disconnect();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        SoundActivity.log("InitHelper start");
        if (!fileInfoSupplied) {
            checkLocalAssets();
            downloadMissingAssets();
        }
        SoundActivity.log("InitHelper end");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        PlaceholderFragment placeholderFragment = new PlaceholderFragment();
        RelativeLayout layout = new RelativeLayout(activity.getApplicationContext());
        layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        View dummy = new android.support.v4.widget.Space(activity.getApplicationContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dummy.setId(SoundActivity.uniqueId++);
        dummy.setLayoutParams(params);
        layout.addView(dummy, dummy.getLayoutParams());
        List<PlaySoundButton> psb = new ArrayList<>(fileInfos.size());
        for (int i = 0; i < fileInfos.size(); i++) {
            if (!fileInfos.get(i).localFile.exists())
                continue;
            psb.add(i, new PlaySoundButton(activity.getApplicationContext(), fileInfos.get(i), dummy, i));
        }
        for (int i = 1; i < psb.size(); i += 2) {
            int tempI = (psb.get(i).getText().length() < psb.get(i - 1).getText().length() ? i : i - 1);
            RelativeLayout.LayoutParams temp = (RelativeLayout.LayoutParams) psb.get(tempI).getLayoutParams();
            temp.addRule(RelativeLayout.ALIGN_BOTTOM, psb.get((tempI == i ? i - 1 : i)).getId());
            psb.get(tempI).setLayoutParams(temp);
        }

        for (int i = 0; i < psb.size(); i++) {
            layout.addView(psb.get(i), psb.get(i).getLayoutParams());
        }
        placeholderFragment.finalView = layout;

        activity.getSupportFragmentManager().beginTransaction().add(R.id.container, placeholderFragment, "placeholderFragment").commit();
        activity.findViewById(R.id.textProgress).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        super.onProgressUpdate(params);
        ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progressBar1);
        TextView textView = (TextView) activity.findViewById(R.id.textProgress);
        progressBar.setMax(params[1]);
        progressBar.setProgress(params[0]);
        textView.setText(activity.getString(R.string.text_loading).replace("%x", Integer.toString(params[0])).replace("%y", Integer.toString(params[1])));
    }

    private void checkLocalAssets() {
        boolean shouldDownload = false;
        StringBuilder s = null;
        if (infoFile.exists() && !BuildConfig.DEBUG) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(infoFile.lastModified());
            cal.add(GregorianCalendar.HOUR, Integer.parseInt(SoundActivity.preferences.getString("update_interval", "24")));

            SoundActivity.log(cal.getTime().toString());
            SoundActivity.log(cal.getTimeInMillis() + "<" + System.currentTimeMillis());

            if (System.currentTimeMillis() < cal.getTimeInMillis()) {
                try {
                    Scanner scanner = new Scanner(infoFile, Charsets.UTF_8.name());
                    s = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        s = s.append(scanner.nextLine());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                shouldDownload = true;
            }
        } else {
            shouldDownload = true;
        }
        SoundActivity.log("Download? " + shouldDownload);
        if (shouldDownload) {
            s = downloadInfoFile(infoFile);
            if (s != null) {
                SoundActivity.log(s.toString());
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity.getApplicationContext());
                builder.setTitle(R.string.connection_error_title);
                builder.setMessage(R.string.connection_error_text);
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (infoFile.exists()) {
                            dialog.dismiss();
                        } else {
                            activity.finish();
                        }
                    }
                });
                builder.create().show();
                if (infoFile.exists()) {
                    try {
                        Scanner scanner = new Scanner(infoFile, Charsets.UTF_8.name());
                        s = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            s = s.append(scanner.nextLine());
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            if (s != null) {
                JSONArray jsonArray = new JSONArray(s.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject array = jsonArray.getJSONObject(i);
                    publishProgress(i, jsonArray.length());
                    File file = new File(soundsDir, array.getString("path").substring(array.getString("path").lastIndexOf(File.separatorChar) + 1));
                    FileInfo fi = new FileInfo(array.getString("hash"), generateHash(file), array.getString("url"), array.getString("name"), array.getString("path"), file.getPath(), array.getInt("id"), file);
                    fileInfos.add(fi);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(fileInfos, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo lhs, FileInfo rhs) {
                return lhs.name.compareToIgnoreCase(rhs.name);

            }
        });
        for (FileInfo info : fileInfos) {
            if (!info.localFile.exists() || !info.remoteHash.equals(info.localHash)) {
                info.needsToBeDownloaded = true;
                numberOfFilesToDownload++;
            }
        }

        SoundActivity.log("To download:" + FluentIterable.from(fileInfos).filter(new Predicate<FileInfo>() {
            @Override
            public boolean apply(FileInfo input) {
                return input.needsToBeDownloaded;
            }
        }).toString());
    }

    private void downloadMissingAssets() {
        if (isNetworkAvailable) {
            if (numberOfFilesToDownload > 0) {
                final int[] index = {0};
                ExecutorService threads = Executors.newFixedThreadPool(4);
                List<Future> futures = new ArrayList<>();
                for (final FileInfo info : fileInfos) {
                    if (info.needsToBeDownloaded) {
                        futures.add(threads.submit(new Runnable() {
                            @Override
                            public void run() {
                                SoundActivity.log("Downloading: " + info.localpath);
                                downloadFile(info.remotePath, info.localFile);
                                index[0]++;
                                SoundActivity.log("Thread no. " + index[0] + " done");
                                publishProgress(index[0], numberOfFilesToDownload);
                            }
                        }));
                    }
                }
                try {
                    for (Future f : futures) {
                        try {
                            SoundActivity.log(f.get() == null ? f.toString() : "Not successful");
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String generateHash(File f) {
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] buffer = new byte[8192];
                int byteCount;
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                while ((byteCount = fis.read(buffer)) != -1) md.update(buffer, 0, byteCount);
                fis.close();
                String hash = bytesToHex(md.digest());
                SoundActivity.log(f.getName() + ": " + hash);
                return hash;
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
