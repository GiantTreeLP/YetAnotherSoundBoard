package org.gtlp.yasb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class SoundActivity extends ActionBarActivity {

    private static File soundsDir;
    InitHelper ih;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundsDir = getExternalFilesDir("sounds");
        setContentView(R.layout.activity_sound);
        TextView lt = (TextView) findViewById(R.id.textView1);
        lt.setText(getText(R.string.text_loading).toString().replace("%x", "0").replace("%y", "0"));
        if (!BuildConfig.DEBUG) {
            AdView adView = (AdView) this.findViewById(R.id.adView);
            AdRequest.Builder adRequest = new AdRequest.Builder();
            adRequest.addTestDevice("E31615C89229AEDC2A9763B4301C3196");
            adView.loadAd(adRequest.build());
        }
        SoundPlayer.setInstance(new SoundPlayer(this));
        ih = new InitHelper();
        ih.execute();

        findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPlayer.getInstance().start();
            }
        });

        findViewById(R.id.pauseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPlayer.getInstance().pause();
            }
        });
        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean oldState;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && SoundPlayer.player != null) SoundPlayer.player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (SoundPlayer.player != null) {
                    oldState = SoundPlayer.player.isPlaying();
                    SoundPlayer.player.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (oldState && SoundPlayer.player != null) SoundPlayer.player.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        ih.cancel(true);
        SoundPlayer.getInstance().release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sound, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about:
                new AboutDialogFragment().show(getFragmentManager(), "AboutDialogFragment");
        }
        return super.onOptionsItemSelected(item);
    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
                analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            }
            Tracker t = analytics.newTracker(R.xml.app_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    public enum TrackerName {
        APP_TRACKER
    }

    private class InitHelper extends AsyncTask<Void, Integer, Void> {

        final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
        TreeSet<File> localFiles = new TreeSet<>(new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        List<String> localHashes = new ArrayList<>();
        TreeSet<String[]> remoteHashes = new TreeSet<>(new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                return lhs[1].compareTo(rhs[1]);
            }
        });
        List<String> downloadQueue = new ArrayList<>();
        MessageDigest md;

        @Override
        protected Void doInBackground(Void... params) {
            checkLocalAssets();
            downloadMissingAssets();
            if (BuildConfig.DEBUG) {
                Log.d("YASB", Arrays.toString(localHashes.toArray()));
                Log.d("YASB", Arrays.toString(localFiles.toArray()));
                Log.d("YASB", Arrays.deepToString(remoteHashes.toArray()));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            switch (params[0]) {
                case 0:
                    findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
                case 1:
                    ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
                    pb.setMax(downloadQueue.size());
                    pb.setProgress(params[1]);
                    TextView tv = (TextView) findViewById(R.id.textView1);
                    tv.setText(getString(R.string.text_loading).replace("%x", Integer.toString(params[1])).replace("%y", Integer.toString(downloadQueue.size())));
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if (BuildConfig.DEBUG) Log.d("YASB", localFiles.size() + ";" + remoteHashes.size());
            PlaceholderFragment pf = new PlaceholderFragment();
            RelativeLayout layout = new RelativeLayout(getApplicationContext());
            layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView dummy = new TextView(getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            dummy.setId(UniqueID.counter++);
            dummy.setLayoutParams(params);
            layout.addView(dummy, params);

            PlaySoundButton[] psb = new PlaySoundButton[localFiles.size()];
            for (int i = 0; i < localFiles.size(); i++) {
                if (!new File(soundsDir, remoteHashes.toArray(new String[0][3])[i][1]).exists())
                    continue;
                psb[i] = new PlaySoundButton(getApplicationContext());
                psb[i].setId(UniqueID.counter++);
                psb[i].info = remoteHashes.toArray(new String[0][3])[i];
                psb[i].setText(remoteHashes.toArray(new String[0][3])[i][2]);
                psb[i].setSound(localFiles.toArray(new File[2])[i]);
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i % 2 == 0) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    params.addRule(RelativeLayout.ALIGN_RIGHT, dummy.getId());
                } else {
                    params.addRule(RelativeLayout.RIGHT_OF, dummy.getId());
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
                params.addRule(i > 0 ? RelativeLayout.BELOW : RelativeLayout.ALIGN_PARENT_TOP, i > 1 ? psb[i - 2].getId() : RelativeLayout.TRUE);

                psb[i].setLayoutParams(params);
                layout.addView(psb[i], params);
                if (BuildConfig.DEBUG)
                    Log.d("YASB", "Below " + (i > 0 ? psb[i - 1].getId() : "Nothing"));
            }

            for (int i = 1; i < psb.length; i += 2) {
                int tempI = (psb[i].getText().length() < psb[i - 1].getText().length() ? i : i - 1);
                RelativeLayout.LayoutParams temp = (RelativeLayout.LayoutParams) psb[tempI].getLayoutParams();
                temp.addRule(RelativeLayout.ALIGN_BOTTOM, psb[(tempI == i ? i - 1 : i)].getId());
                psb[tempI].setLayoutParams(temp);
            }

            pf.finalView = layout;
            getSupportFragmentManager().beginTransaction().add(R.id.container, pf).commit();
            findViewById(R.id.textView1).setVisibility(View.INVISIBLE);
            findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
        }

        private void downloadMissingAssets() {
            if (((ConnectivityManager) (getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo().isConnected()) {
                if (downloadQueue.size() > 0) {
                    int index = 0;
                    for (String s : downloadQueue) {
                        publishProgress(1, index);
                        index++;
                        if (BuildConfig.DEBUG) Log.d("YASB", "Downloading: " + s.split(";")[0]);
                        File f = new File(soundsDir, s.split(";")[0]);
                        try {
                            f.createNewFile();
                            FileOutputStream fos = new FileOutputStream(f);
                            BufferedInputStream is = OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/" + f.getName());
                            byte[] buffer = new byte[8192];
                            int byteCount;

                            while ((byteCount = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, byteCount);
                            }
                            fos.flush();
                            is.close();
                            fos.close();
                            localFiles.add(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    publishProgress(1, index);
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            }
        }

        private void checkLocalAssets() {
            if (BuildConfig.DEBUG)
                Log.d("YASB", Arrays.toString(soundsDir.listFiles()));
            for (File f : soundsDir.listFiles()) {
                if (f.exists() && f.isFile() && f.canRead()) {
                    localFiles.add(f);
                    generateHash(f);
                }
            }
            boolean download = false;
            FileOutputStream fos = null;
            Scanner sc = new Scanner("");
            File infoFile = new File(getExternalFilesDir("info"), "info");
            if (infoFile.exists()) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeInMillis(infoFile.lastModified());
                cal.add(Calendar.HOUR, 24);
                if (BuildConfig.DEBUG) {
                    Log.d("YASB", cal.getTime().toString());
                    Log.d("YASB", cal.getTimeInMillis() + "<" + System.currentTimeMillis());
                }
                if (cal.getTimeInMillis() > System.currentTimeMillis()) {
                    try {
                        sc = new Scanner(infoFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        sc = new Scanner(OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/info"));
                        try {
                            fos = new FileOutputStream(infoFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        download = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    sc = new Scanner(OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/info"));
                    try {
                        fos = new FileOutputStream(infoFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    download = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while (sc.hasNext()) {
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
            sc.close();

            Set<String> setItems = new LinkedHashSet<>(localHashes);
            localHashes.clear();
            localHashes.addAll(setItems);

            Set<File> setItems1 = new LinkedHashSet<>(localFiles);
            localFiles.clear();
            localFiles.addAll(setItems1);


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
                Log.d("YASB", "To download: " + Arrays.toString(downloadQueue.toArray()));
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
                if (BuildConfig.DEBUG) Log.d("YASB", f.getName() + ": " + hash);
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

        private BufferedInputStream OpenHttpConnection(String strURL)
                throws IOException {
            URLConnection conn = null;
            BufferedInputStream inputStream = null;
            URL url = new URL(strURL);
            conn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = new BufferedInputStream(httpConn.getInputStream(), 8192);
            }
            return inputStream;
        }

    }

}