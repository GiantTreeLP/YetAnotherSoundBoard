package org.gtlp.yasb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
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
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class InitHelper extends AsyncTask<Void, Integer, Void> {

    final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static File infoFile;
    final String HTTP_SOUNDS_INFO_FILE = "http://gtlp.4b4u.com/assets/sounds/info";
    ArrayList<File> localFiles = new ArrayList<>();
    ArrayList<String> localHashes = new ArrayList<>();
    ArrayList<String[]> remoteHashes = new ArrayList<>();
    ArrayList<String> downloadQueue = new ArrayList<>();
    MessageDigest md;
    private ActionBarActivity actionBarActivity;

    public InitHelper(ActionBarActivity actionBarActivity) {
        this.actionBarActivity = actionBarActivity;
        infoFile = new File(actionBarActivity.getExternalFilesDir("info"), "info");
    }

    public static BufferedInputStream OpenHttpConnection(String strURL)
            throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) new URL(strURL).openConnection();
        httpConn.setRequestMethod("GET");
        httpConn.connect();
        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return new BufferedInputStream(httpConn.getInputStream(), 1024);
        }
        return null;
    }

    @Override
    protected Void doInBackground(Void... params) {
        checkLocalAssets();
        downloadMissingAssets();
        if (BuildConfig.DEBUG) {
            Log.d(SoundActivity.YASB, Arrays.toString(localHashes.toArray()));
            Log.d(SoundActivity.YASB, Arrays.toString(localFiles.toArray()));
            Log.d(SoundActivity.YASB, Arrays.deepToString(remoteHashes.toArray()));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        switch (params[0]) {
            case 0:
                actionBarActivity.findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
            case 1:
                ProgressBar pb = (ProgressBar) actionBarActivity.findViewById(R.id.progressBar1);
                pb.setMax(downloadQueue.size());
                pb.setProgress(params[1]);
                TextView tv = (TextView) actionBarActivity.findViewById(R.id.textView1);
                tv.setText(actionBarActivity.getString(R.string.text_loading).replace("%x", Integer.toString(params[1])).replace("%y", Integer.toString(downloadQueue.size())));
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (BuildConfig.DEBUG)
            Log.d(SoundActivity.YASB, localFiles.size() + ";" + remoteHashes.size());

        PlaceholderFragment placeholderFragment = new PlaceholderFragment();
        RelativeLayout layout = new RelativeLayout(actionBarActivity.getApplicationContext());
        layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView dummy = new TextView(actionBarActivity.getApplicationContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dummy.setId(UniqueID.counter++);
        dummy.setLayoutParams(params);
        layout.addView(dummy, dummy.getLayoutParams());
        ArrayList<PlaySoundButton> psb = new ArrayList<>(localFiles.size());
        for (int i = 0; i < remoteHashes.size(); i++) {
            if (!new File(SoundActivity.soundsDir, remoteHashes.get(i)[1]).exists())
                continue;
            psb.add(i, new PlaySoundButton(actionBarActivity.getApplicationContext(), remoteHashes, localFiles, dummy, i));
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

        actionBarActivity.getSupportFragmentManager().beginTransaction().add(R.id.container, placeholderFragment).commit();
        actionBarActivity.findViewById(R.id.textView1).setVisibility(View.INVISIBLE);
        actionBarActivity.findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
    }

    private void downloadMissingAssets() {
        if (((ConnectivityManager) (actionBarActivity.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo().isConnected()) {
            if (downloadQueue.size() > 0) {
                int index = 0;
                for (String s : downloadQueue) {
                    publishProgress(1, index);
                    index++;
                    if (BuildConfig.DEBUG)
                        Log.d(SoundActivity.YASB, "Downloading: " + s.split(";")[0]);
                    File f = new File(SoundActivity.soundsDir, s.split(";")[0]);
                    try {
                        FileOutputStream fos = new FileOutputStream(f);
                        BufferedInputStream is = OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/" + f.getName());
                        byte[] buffer = new byte[8192];
                        int byteCount;

                        if (is != null) {
                            while ((byteCount = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, byteCount);
                            }
                            fos.flush();
                            is.close();
                            fos.close();
                            localFiles.add(f);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                publishProgress(1, index);
            }
        } else {
            Toast.makeText(actionBarActivity.getApplicationContext(), actionBarActivity.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLocalAssets() {
        if (BuildConfig.DEBUG)
            Log.d(SoundActivity.YASB, Arrays.toString(SoundActivity.soundsDir.listFiles()));
        for (File f : SoundActivity.soundsDir.listFiles()) {
            if (f.exists() && f.isFile() && f.canRead()) {
                localFiles.add(f);
                generateHash(f);
            }
        }
        boolean download = false;
        FileOutputStream fos = null;
        Scanner sc = null;
        if (infoFile.exists()) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(infoFile.lastModified());
            cal.add(GregorianCalendar.HOUR, Integer.parseInt(SoundActivity.preferences.getString("update_interval", "24")));

            if (BuildConfig.DEBUG) {
                Log.d(SoundActivity.YASB, cal.getTime().toString());
                Log.d(SoundActivity.YASB, cal.getTimeInMillis() + "<" + System.currentTimeMillis());
            }

            if (System.currentTimeMillis() < cal.getTimeInMillis()) {
                try {
                    sc = new Scanner(infoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                download = true;
            }
        } else {
            download = true;
        }
        if (download) {
            InfoFileDownloader infoFileDownloader = new InfoFileDownloader().invoke();
            sc = infoFileDownloader.getSc();
            fos = infoFileDownloader.getFos();
        }
        while (sc != null && sc.hasNext()) {
            String s = sc.nextLine() + "\n";
            remoteHashes.add(s.split(";"));
            if (download && fos != null) {
                try {
                    fos.write(s.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (sc != null) {
            sc.close();
        }

        /*Collections.sort(localFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        Collections.sort(remoteHashes, new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                return lhs[1].compareTo(rhs[1]);
            }
        });*/

        LinkedHashSet<String> localHashesSet = new LinkedHashSet<>(localHashes);
        localHashes.clear();
        localHashes.addAll(localHashesSet);

        LinkedHashSet<File> localFilesSet = new LinkedHashSet<>(localFiles);
        localFiles.clear();
        localFiles.addAll(localFilesSet);


        for (String[] strings : remoteHashes) {
            downloadQueue.add(strings[1]);
        }
        if (localFiles.size() > 0) {
            for (String[] strings : remoteHashes) {
                for (File file : localFiles) {
                    if (file.getName().equals(strings[1]) && localHashes.contains(strings[0])) {
                        downloadQueue.remove(strings[1]);
                        break;
                    }
                }
            }
        }

        if (BuildConfig.DEBUG)
            Log.d(SoundActivity.YASB, "To download: " + Arrays.toString(downloadQueue.toArray()));
    }

    private void generateHash(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] buffer = new byte[8192];
            int byteCount;
            md = MessageDigest.getInstance("SHA-1");
            while ((byteCount = fis.read(buffer)) != -1) md.update(buffer, 0, byteCount);
            fis.close();
            String hash = bytesToHex(md.digest());
            localHashes.add(hash);
            if (BuildConfig.DEBUG) Log.d(SoundActivity.YASB, f.getName() + ": " + hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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

    private class InfoFileDownloader {
        private FileOutputStream fos;
        private Scanner sc;

        public FileOutputStream getFos() {
            return fos;
        }

        public Scanner getSc() {
            return sc;
        }

        public InfoFileDownloader invoke() {
            try {
                BufferedInputStream infoInputStream = OpenHttpConnection(HTTP_SOUNDS_INFO_FILE);

                if (infoInputStream != null) {
                    sc = new Scanner(infoInputStream);
                }
                fos = new FileOutputStream(infoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }
    }
}
