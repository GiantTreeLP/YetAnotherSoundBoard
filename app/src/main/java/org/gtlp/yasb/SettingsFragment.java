package org.gtlp.yasb;

import android.os.Bundle;

import com.github.machinarius.preferencefragment.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
