package anlin.softdev.kuleuven.memories;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class MySettingFragment extends PreferenceFragmentCompat {
    private static Context prefContext;
    private static Activity activity;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preference, rootKey);
        Preference history = findPreference("clearHistory");
        history.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(prefContext,
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                AlertDialog.Builder builder = new AlertDialog.Builder(prefContext);
                builder.setMessage("Clear search history?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                suggestions.clearHistory();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .show();
                return true;
            }
        });
        SwitchPreferenceCompat theme = findPreference("theme");
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //onPreferenceChange will be called before the change is done
                ImageView imageView = activity.findViewById(R.id.imageView4);
                if (theme.isChecked()) {
                    imageView.setImageResource(R.drawable.word_memory);
                } else {
                    imageView.setImageResource(R.drawable.word_memory_black);
                }
                return true;
            }
        });
    }

    public static void setPrefContext(Context prefContext) {
        MySettingFragment.prefContext = prefContext;
    }

    public static void setActivity(Activity activity) {
        MySettingFragment.activity = activity;
    }

}