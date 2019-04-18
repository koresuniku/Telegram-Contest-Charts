package org.telegram.charts;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;

class NightModeHelper {
    static int _uiNightMode = Configuration.UI_MODE_NIGHT_UNDEFINED;

    private WeakReference<Activity> _activityRef;
    private SharedPreferences _preferences;

    NightModeHelper(Activity activity, int theme) {
        _preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        init(activity, theme, _preferences.getInt(activity.getString(R.string.night_theme_key), Configuration.UI_MODE_NIGHT_NO));
    }

    private void init(Activity activity, int theme, int defaultUiMode) {
        _activityRef = new WeakReference<>(activity);
        if (_uiNightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) _uiNightMode = defaultUiMode;
        updateConfig(_uiNightMode);
        activity.setTheme(theme);
    }

    private void updateConfig(int uiNightMode) {
        if (_activityRef.get() != null && _preferences != null) {
            Activity activity = _activityRef.get();
            Configuration newConfig = new Configuration(activity.getResources().getConfiguration());
            newConfig.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
            newConfig.uiMode |= uiNightMode;
            activity.getResources().updateConfiguration(newConfig, null);
            _uiNightMode = uiNightMode;
            _preferences.edit().putInt(activity.getString(R.string.night_theme_key), _uiNightMode).apply();
        }
    }

    void toggle() {
        if (_uiNightMode == Configuration.UI_MODE_NIGHT_YES) notNight();
        else night();
    }

    private void notNight() {
        updateConfig(Configuration.UI_MODE_NIGHT_NO);
        _activityRef.get().recreate();
    }

    private void night() {
        updateConfig(Configuration.UI_MODE_NIGHT_YES);
        _activityRef.get().recreate();
    }
}