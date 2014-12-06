package org.gtlp.yasb;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class SoundActivity extends ActionBarActivity {

    InitHelper ih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        TextView lt = (TextView) findViewById(R.id.textView1);
        lt.setText(getText(R.string.text_loading).toString().replace("%x", "0").replace("%y", "0"));
        if (!BuildConfig.DEBUG) {
            AdView adView = (AdView) this.findViewById(R.id.adView);
            AdRequest.Builder adRequest = new AdRequest.Builder();
            adRequest.addTestDevice("E31615C89229AEDC2A9763B4301C3196");
            adView.loadAd(adRequest.build());
        }
        SoundPlayer.init();
        ih = new InitHelper();
        ih.execute();
    }

    @Override
    protected void onDestroy() {
        ih.cancel(true);
        SoundPlayer.mediaPlayer.release();
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public View finalView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //return inflater.inflate(R.layout.fragment_sound, container, false);
            return finalView;
        }
    }

    private class InitHelper extends AsyncTask<Void, Integer, Void> {

        final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
        List<File> localFiles = new ArrayList<File>();
        List<String> localHashes = new ArrayList<String>();
        List<String[]> remoteHashes = new ArrayList<String[]>();
        List<String> downloadQueue = new ArrayList<String>();
        MessageDigest md;

        @Override
        protected Void doInBackground(Void... params) {
            checkLocalAssets();
            downloadMissingAssets();
            if (BuildConfig.DEBUG) {
                Log.d("YASB", Arrays.toString(localHashes.toArray()));
                Log.d("YASB", Arrays.deepToString(remoteHashes.toArray()));
            }
            publishProgress(2);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            switch (params[0]) {
                case 0:
                    findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
                case 1:
                    ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
                    pb.setProgress(pb.getProgress() + (params[1] / downloadQueue.size()));
                    TextView tv = (TextView) findViewById(R.id.textView1);
                    tv.setText(getText(R.string.text_loading).toString().replace("%x", Integer.toString(params[1])).replace("%y", Integer.toString(downloadQueue.size())));
                case 2:
                    findViewById(R.id.textView1).setVisibility(View.INVISIBLE);
                    findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            Collections.sort(localFiles);
            PlaceholderFragment pf = new PlaceholderFragment();
            RelativeLayout layout = new RelativeLayout(getApplicationContext());
            layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            PlaySoundButton[] psb = new PlaySoundButton[localFiles.size()];
            for (int i = 0; i < localFiles.size(); i++) {
                psb[i] = new PlaySoundButton(getApplicationContext());
                psb[i].setId(UniqueID.getNext());
                psb[i].setIndex(i);
                psb[i].setText(remoteHashes.get(i)[2]);
                psb[i].setSound(localFiles.get(i));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i % 2 != 0 && i != 0) {
                    params.addRule(RelativeLayout.RIGHT_OF, i > 0 ? psb[i - 1].getId() : RelativeLayout.ALIGN_LEFT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.addRule(RelativeLayout.BELOW, i > 1 ? psb[i - 2].getId() : RelativeLayout.ALIGN_LEFT);
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    if (i > 0) params.addRule(RelativeLayout.BELOW, psb[i - 1].getId());
                    else params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                }
                psb[i].setLayoutParams(params);
                layout.addView(psb[i], params);
                if (BuildConfig.DEBUG)
                    Log.d("YASB", new StringBuilder().append("Below ").append(i > 0 ? psb[i - 1].getId() : "Nothing").toString());
            }
            pf.finalView = layout;
            getSupportFragmentManager().beginTransaction().add(R.id.container, pf).commit();
        }

        private void downloadMissingAssets() {
            if (downloadQueue.size() > 0) {
                int index = 0;
                for (String s : downloadQueue) {
                    publishProgress(1, index);
                    index++;
                    if (BuildConfig.DEBUG) Log.d("YASB", "Downloading: " + s);
                    File f = new File(getExternalFilesDir("sounds"), s);
                    try {
                        FileOutputStream fos = new FileOutputStream(f);
                        BufferedInputStream is = new BufferedInputStream(new URL("http://gtlp.4b4u.com/assets/sounds/" + f.getName()).openStream(), 8192);
                        byte[] buffer = new byte[8192];
                        int byteCount;

                        while ((byteCount = is.read(buffer)) != -1) {
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
        }

        private void checkLocalAssets() {
            for (File f : getExternalFilesDir("sounds").listFiles()) {
                if (f.exists() && f.isFile() && f.canRead()) {
                    localFiles.add(f);
                    generateHash(f);
                }
            }
            if (BuildConfig.DEBUG)
                Log.d("YASB", "Found: " + Arrays.deepToString(localFiles.toArray()));
            try {
                Scanner sc = new Scanner(new URL("http://gtlp.4b4u.com/assets/sounds/info").openStream());
                while (sc.hasNext()) {
                    remoteHashes.add(sc.nextLine().split(";"));
                }
                sc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String[] strings : remoteHashes) {
                boolean found = false;
                for (File file : localFiles)
                    if (file.getName().equals(strings[1]) && localHashes.contains(strings[0])) {
                        found = true;
                        break;
                    }
                if (!found) downloadQueue.add(strings[1]);
            }
            if (BuildConfig.DEBUG)
                Log.d("YASB", "To download: " + Arrays.deepToString(downloadQueue.toArray()));
        }

        private void generateHash(File f) {
            try {
                FileInputStream fis = new FileInputStream(f);
                byte[] buffer = new byte[8192];
                int byteCount;
                md = MessageDigest.getInstance("SHA-1");
                while ((byteCount = fis.read(buffer)) != -1) md.update(buffer, 0, byteCount);
                fis.close();
                localHashes.add(bytesToHex(md.digest()));
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

    }

}
