package org.gtlp.yasb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.util.Scanner;

public class InitHelper extends AsyncTask<Void, Integer, Void> {

	public static final String HTTP_SOUNDS_INFO_FILE = "http://gtlp.4b4u.com/getsoundsinfo.php";
	final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static File infoFile;
	private static File soundsDir;
	public ArrayList<FileInfo> fileInfos = new ArrayList<>();
	public boolean fileInfoSupplied = false;
	private ActionBarActivity actionBarActivity;
	private int numberOfFilesToDownload = 0;

	public InitHelper(ActionBarActivity actionBarActivity) {
		this.actionBarActivity = actionBarActivity;
		soundsDir = actionBarActivity.getExternalFilesDir("sounds");
		infoFile = new File(actionBarActivity.getExternalFilesDir("info"), "info.json");
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (!fileInfoSupplied) {
			checkLocalAssets();
			downloadMissingAssets();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		PlaceholderFragment placeholderFragment = new PlaceholderFragment();
		RelativeLayout layout = new RelativeLayout(actionBarActivity.getApplicationContext());
		layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView dummy = new TextView(actionBarActivity.getApplicationContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		dummy.setId(UniqueID.counter++);
		dummy.setLayoutParams(params);
		layout.addView(dummy, dummy.getLayoutParams());
		ArrayList<PlaySoundButton> psb = new ArrayList<>(fileInfos.size());
		for (int i = 0; i < fileInfos.size(); i++) {
			if (!fileInfos.get(i).localFile.exists())
				continue;
			psb.add(i, new PlaySoundButton(actionBarActivity.getApplicationContext(), fileInfos, dummy, i));
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

	@Override
	protected void onProgressUpdate(Integer... params) {
		switch (params[0]) {
			case 0:
				actionBarActivity.findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
			case 1:
				ProgressBar pb = (ProgressBar) actionBarActivity.findViewById(R.id.progressBar1);
				pb.setMax(numberOfFilesToDownload);
				pb.setProgress(params[1]);
				TextView tv = (TextView) actionBarActivity.findViewById(R.id.textView1);
				tv.setText(actionBarActivity.getString(R.string.text_loading).replace("%x", Integer.toString(params[1])).replace("%y", Integer.toString(numberOfFilesToDownload)));
		}
	}

	private void checkLocalAssets() {
		boolean download = false;
		StringBuilder s = new StringBuilder();
		if (infoFile.exists()) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(infoFile.lastModified());
			cal.add(GregorianCalendar.HOUR, Integer.parseInt(SoundActivity.preferences.getString("update_interval", "24")));

			SoundActivity.Log(cal.getTime().toString());
			SoundActivity.Log(cal.getTimeInMillis() + "<" + System.currentTimeMillis());

			if (System.currentTimeMillis() < cal.getTimeInMillis()) {
				try {
					Scanner scanner = new Scanner(infoFile);
					while (scanner.hasNextLine()) {
						s = s.append(scanner.nextLine());
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				download = true;
			}
		} else {
			download = true;
		}
		SoundActivity.Log("Download? " + (download));
		if (download) {
			s = downloadInfoFile();
			if (s != null) {
				SoundActivity.Log(s.toString());
			}
		}
		try {
			if (s != null) {
				JSONArray jsonArray = new JSONArray(s.toString());
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject array = jsonArray.getJSONObject(i);
					File file = new File(soundsDir, array.getString("path"));
					FileInfo fi = new FileInfo(array.getString("hash"), generateHash(file), array.getString("url"), array.getString("name"), file.getPath(), array.getInt("id"), file);
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

		SoundActivity.Log("To download: " + Arrays.toString(FluentIterable.from(fileInfos).filter(new Predicate<FileInfo>() {
			@Override
			public boolean apply(FileInfo input) {
				return input.needsToBeDownloaded;
			}
		}).toArray(FileInfo.class)));
	}

	private void downloadMissingAssets() {
		if (((ConnectivityManager) (actionBarActivity.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo().isConnected()) {
			if (numberOfFilesToDownload > 0) {
				int index = 0;
				for (FileInfo info : fileInfos) {
					if (info.needsToBeDownloaded) {
						publishProgress(1, index);
						index++;

						SoundActivity.Log("Downloading: " + info.filePath);

						try {
							FileOutputStream fos = new FileOutputStream(info.localFile);
							BufferedInputStream is = OpenHttpConnection("http://gtlp.4b4u.com/assets/sounds/" + info.filePath);
							byte[] buffer = new byte[8192];
							int byteCount;

							if (is != null) {
								while ((byteCount = is.read(buffer)) > 0) {
									fos.write(buffer, 0, byteCount);
								}
								is.close();
								fos.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					publishProgress(1, index);
				}
			}
		}
	}

	public static StringBuilder downloadInfoFile() {
		try {
			StringBuilder s = new StringBuilder();
			BufferedInputStream bis = OpenHttpConnection(HTTP_SOUNDS_INFO_FILE);
			if (bis != null) {
				Scanner scanner = new Scanner(bis);
				while (scanner.hasNextLine()) {
					s = s.append(scanner.nextLine()).append("\n");
				}
				SoundActivity.Log(s.toString());
				FileWriter fos = new FileWriter(infoFile);
				fos.write(s.toString());
				fos.close();
				return s;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
				SoundActivity.Log(f.getName() + ": " + hash);
				return hash;
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static BufferedInputStream OpenHttpConnection(String strURL)
			throws IOException {
		HttpURLConnection httpConn = (HttpURLConnection) new URL(strURL).openConnection();
		httpConn.connect();
		if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			SoundActivity.Log(httpConn.getResponseMessage());
			return new BufferedInputStream(httpConn.getInputStream());
		}
		return null;
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
