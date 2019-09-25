package anlin.softdev.kuleuven.memories;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class OptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_layout);
        Toolbar option_toolbar = findViewById(R.id.option_toolbar);
        option_toolbar.setTitle("Settings");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, new MySettingFragment())
                .commit();
        MySettingFragment.setPrefContext(this);


    }

}
