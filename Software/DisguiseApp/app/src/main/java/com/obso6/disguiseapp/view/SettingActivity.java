package com.obso6.disguiseapp.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.obso6.disguiseapp.R;

public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{

    //脚本说明
    private Preference scriptIntroduction;
    //APP说明
    private Preference appIntroduction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置读取
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference);
        scriptIntroduction=(Preference) findPreference("script_introduction");
        scriptIntroduction.setOnPreferenceClickListener(this);
        appIntroduction=(Preference) findPreference("app_introduction");
        appIntroduction.setOnPreferenceClickListener(this);
        bindPreferenceSummaryToValue(findPreference("scan_time"));
        bindPreferenceSummaryToValue(findPreference("mac_address"));
    }

    public boolean onPreferenceClick(Preference preference) {
        if(preference == scriptIntroduction){
            //脚本说明
            Uri uri = Uri.parse("https://github.com/ObSo6/DisguiseUSBCable/blob/main/Markdown/DuckyScript.md");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }else if(preference == appIntroduction){
            //APP说明
            Uri uri = Uri.parse("https://github.com/ObSo6/DisguiseUSBCable/blob/main/README.md");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        return true;
    }

    /**
     * bindPreferenceSummaryToValue 拷贝至AS自动生成的preferences的代码，用于绑定显示实时值
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        } else  {
            preference.setSummary(stringValue);
        }
        return true;
    };
    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

}
