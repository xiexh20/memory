package anlin.softdev.kuleuven.memories;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import database.Device;
import database.NewSQLService;
import ocr.DeviceIDUtil;

public class SearchableActivity extends AppCompatActivity {

    private NewSQLService gtService;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.search);

        mContext = this;
        Device device = new Device(DeviceIDUtil.getAndroidId(this),DeviceIDUtil.getDeviceName());
        gtService = new NewSQLService(this, device);
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String search_option = prefs.getString("searchOption","both");
//        MySettingFragment.setPrefContext(this);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //save query
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            //search the database based on the query
            if(!query.equals("")) { //if not empty
                switch(search_option) {
                    case "both":
                        gtService.searchByLabelText(query);
                        break;
                    case "text":
                        gtService.searchByText(query);
                        break;
                    case "label":
                        gtService.searchByLabel(query);
                        break;
                }

            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("please enter a text!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //return to main activity
                                Intent mIntent = new Intent(mContext,MainActivity.class);
                                startActivity(mIntent);
                            }
                        })
                        .show();
            }
            finish();

        }
    }
    public void clearHistory(){

    }


}
