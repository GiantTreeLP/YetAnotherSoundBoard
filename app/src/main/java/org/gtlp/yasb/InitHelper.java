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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

public class InitHelper extends AsyncTask<Void, Integer, Void> {

    public static final String SERVER_HOST = "http://gianttree.bplaced.net/";
    public static final String HTTP_SOUNDS_INFO_FILE = SERVER_HOST + "assets/getsoundsinfo.php";
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
        BufferedWriter fos = null;
        try {
            StringBuilder s = new StringBuilder();
            BufferedInputStream bis = openHttpConnection(HTTP_SOUNDS_INFO_FILE);
            if (bis != null) {
                Scanner scanner = new Scanner(bis, Charsets.UTF_8.name());
                while (scanner.hasNextLine()) {
                    s = s.append(scanner.nextLine()).append("\n");
                }
                scanner.close();
                SoundActivity.log(s.toString());
                fos = Files.newWriter(infoFile, Charsets.UTF_8);
                fos.write(s.toString());
                isNetworkAvailable = true;
                return s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static BufferedInputStream openHttpConnection(String strURL)
            throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) new URL(strURL).openConnection();
        httpConn.connect();
        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            SoundActivity.log(httpConn.getResponseMessage());
            return new BufferedInputStream(httpConn.getInputStream());
        }
        return null;
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
        TextView dummy = new TextView(activity.getApplicationContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dummy.setId(SoundActivity.uniqueId++);
        dummy.setLayoutParams(params);
        layout.addView(dummy, dummy.getLayoutParams());
        List<PlaySoundButton> psb = new ArrayList<>(fileInfos.size());
        for (int i = 0; i < fileInfos.size(); i++) {
            if (!fileInfos.get(i).localFile.exists())
                continue;
            psb.add(i, new PlaySoundButton(activity.getApplicationContext(), fileInfos, dummy, i));
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
        activity.findViewById(R.id.textView1).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        switch (params[0]) {
            case 0:
                activity.findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
                return;
            case 1:
                ProgressBar pb = (ProgressBar) activity.findViewById(R.id.progressBar1);
                pb.setMax(numberOfFilesToDownload);
                pb.setProgress(params[1]);
                TextView tv = (TextView) activity.findViewById(R.id.textView1);
                tv.setText(activity.getString(R.string.text_loading).replace("%x", Integer.toString(params[1])).replace("%y", Integer.toString(numberOfFilesToDownload)));
                return;
            default:
                super.onProgressUpdate(params);
                return;
        }
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
                builder.create();
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
                    File file = new File(soundsDir, array.getString("path").substring(array.getString("path").lastIndexOf("/") + 1));
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

        SoundActivity.log("To shouldDownload: " + Arrays.toString(FluentIterable.from(fileInfos).filter(new Predicate<FileInfo>() {
            @Override
            public boolean apply(FileInfo input) {
                return input.needsToBeDownloaded;
            }
        }).toArray(FileInfo.class)));
    }

    private void downloadMissingAssets() {
        if (isNetworkAvailable) {
            if (numberOfFilesToDownload > 0) {
                int index = 0;
                for (FileInfo info : fileInfos) {
                    if (info.needsToBeDownloaded) {
                        publishProgress(1, index);
                        index++;

                        SoundActivity.log("Downloading: " + info.localpath);

                        try {
                            FileOutputStream fos = new FileOutputStream(info.localFile);
                            BufferedInputStream is = openHttpConnection(info.remotePath);
                            byte[] buffer = new byte[8192];
                            int byteCount;

                            if (is != null) {
                                while ((byteCount = is.read(buffer)) > 0) {
                                    fos.write(buffer, 0, byteCount);
                                }
                                is.close();
                            }
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    publishProgress(1, index);
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
