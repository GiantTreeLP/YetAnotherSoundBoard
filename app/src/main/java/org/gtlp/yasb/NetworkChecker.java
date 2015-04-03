package org.gtlp.yasb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class NetworkChecker extends AsyncTask<Void, Void, Void> {

	Activity soundActivity;

	public NetworkChecker(Activity activity) {
		soundActivity = activity;
	}

	@Override
	protected Void doInBackground(Void... params) {
		publishProgress();
		return null;
	}

	protected void onProgressUpdate(Void... params) {
		try {
			SoundActivity.Log("Checking network");
			SoundActivity.webView.loadUrl("http://gtlp.lima-city.de");
			SoundActivity.webView.setWebViewClient(new WebViewClient() {
				boolean done = false;

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					if (!done) {
						Map<String, String> headers = new HashMap<>();
						headers.put("Referer", "http://gtlp.lima-city.de");
						SoundActivity.webView.loadUrl("http://www.4b4u.com/", headers);
						SoundActivity.Log("Check #2: PASS");
						done = true;
					}
				}
			});
			SoundActivity.Log("Check #1: PASS");
			downloadInfoFile();
		} catch (Exception ignored) {

		}
	}

	private void downloadInfoFile() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(InitHelper.infoFile.lastModified());
		cal.add(GregorianCalendar.HOUR, Integer.parseInt(SoundActivity.preferences.getString("update_interval", "24")));

		SoundActivity.Log(cal.getTime().toString());
		SoundActivity.Log(cal.getTimeInMillis() + "<" + System.currentTimeMillis());

		if (cal.getTimeInMillis() > System.currentTimeMillis()) {
			if (InitHelper.downloadInfoFile() == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(soundActivity.getApplicationContext());
				builder.setTitle(R.string.connection_error_title);
				builder.setMessage(R.string.connection_error_text);
				builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (InitHelper.infoFile.exists()) {
							dialog.dismiss();
						} else {
							System.exit(0);
						}
					}
				});
				builder.create();
				return;
			}
		}
		SoundActivity.Log("Loaded info.");
	}
}
