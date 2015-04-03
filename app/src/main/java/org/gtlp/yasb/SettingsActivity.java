package org.gtlp.yasb;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends ActionBarActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSupportActionBar((Toolbar) findViewById(R.id.bar));
		getSupportFragmentManager().beginTransaction().add(R.id.container_settings, new SettingsFragment()).commit();
	}
}
