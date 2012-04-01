/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.cmparts.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.utils.ShortcutPickHelper;

public class LongPressMenuActivity extends PreferenceActivity
        implements OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private static final String USER_DEFINED_LONG_PRESS_MENU = "pref_user_defined_long_press_menu";

    private static final String INPUT_CUSTOM_LONG_MENU = "pref_long_press_menu";

    private static final String DOWN_LOCK_PREF = "pref_down_lock";
    private CharSequence[] Menu_Content;

    private CheckBoxPreference mDownLockPref;

    private Preference mUserDefinedLongPressMenu;
    private ListPreference long_menu;

    private ShortcutPickHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.pref_down_lock_title);
        addPreferencesFromResource(R.xml.long_press_menu);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Down Lock */
        mDownLockPref = (CheckBoxPreference) prefSet.findPreference(DOWN_LOCK_PREF);
        Settings.System.putInt(getContentResolver(), Settings.System.DOWN_PRESS_LOCK_SCREEN,
                    mDownLockPref.isChecked() ? 1 : 0);

        long_menu = (ListPreference) prefSet.findPreference(INPUT_CUSTOM_LONG_MENU);

        long_menu.setOnPreferenceChangeListener(this);

        Menu_Content = long_menu.getEntries();

        if (!long_menu.getValue().equals("0")) {
            long_menu.setSummary(Menu_Content[long_menu.findIndexOfValue(long_menu.getValue())]);
		}

        mUserDefinedLongPressMenu = (Preference) prefSet.findPreference(USER_DEFINED_LONG_PRESS_MENU);

        mUserDefinedLongPressMenu.setEnabled((Settings.System.getInt(getContentResolver(),Settings.System.USE_CUSTOM_LONG_MENU, 0))==3);

        mPicker = new ShortcutPickHelper(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        String value = Settings.System.getString(getContentResolver(),
                Settings.System.USE_CUSTOM_LONG_MENU_APP_ACTIVITY);
        mUserDefinedLongPressMenu.setSummary(mPicker.getFriendlyNameForUri(value));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String val = newValue.toString();
        int index = long_menu.findIndexOfValue(val);
        if (index == 0) {
            Settings.System.putInt(getContentResolver(), Settings.System.DOWN_PRESS_LOCK_SCREEN,
                mDownLockPref.isChecked() ? 1 : 0);
        }
        else {
            mDownLockPref.setChecked(false);
            Settings.System.putInt(getContentResolver(), Settings.System.DOWN_PRESS_LOCK_SCREEN, 0);
		}
        Settings.System.putString(getContentResolver(), Settings.System.USE_CUSTOM_LONG_MENU, val);
        mUserDefinedLongPressMenu.setEnabled((index==3) ? true : false);
        if (index == 0) {
            long_menu.setSummary(R.string.input_custom_down_lock_summary);
		} else {
            long_menu.setSummary(Menu_Content[index]);
		}
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDownLockPref) {
            Settings.System.putInt(getContentResolver(), Settings.System.DOWN_PRESS_LOCK_SCREEN,
                    mDownLockPref.isChecked() ? 1 : 0);
            if (mDownLockPref.isChecked()){
                long_menu.setValueIndex(0);
                long_menu.setSummary(R.string.input_custom_down_lock_summary);
                Settings.System.putInt(getContentResolver(), Settings.System.USE_CUSTOM_LONG_MENU, 0);
            }
		}
        else if (preference == mUserDefinedLongPressMenu)
            mPicker.pickShortcut();
		mUserDefinedLongPressMenu.setEnabled(mDownLockPref.isChecked() ? false:true);
        return true;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_LONG_MENU_APP_ACTIVITY, uri)) {
            mUserDefinedLongPressMenu.setSummary(friendlyName);
        }
    }
}
