package anlin.softdev.kuleuven.memories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import gallery.ImageInfo;
import gallery.MyGridViewAdapter;
import gallery.ScreenUtils;

/**
 * the activity to show the search result images
 */
public class SearchResultActivity extends AppCompatActivity {

    private GridView imagesGridview;
    private MyGridViewAdapter gridAdapter;
    private Context thisContext;
    private ArrayList<String> imagePathList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        thisContext = this;

        imagesGridview = findViewById(R.id.results_gridview);
        imagesGridview.setNumColumns(3);        //each row three pictures
        int height = ScreenUtils.getScreenWidth(this) / 3;
        int width = ScreenUtils.getScreenWidth(this) / 3;
        imagesGridview.setColumnWidth(width);

        //get data from intent
        Intent thisIntent = getIntent();
        Bundle extras = thisIntent.getExtras();

        List<ImageInfo> data = extras.getParcelableArrayList("DATA");
        gridAdapter = new MyGridViewAdapter(this,R.layout.grid_item_layout, (ArrayList<ImageInfo>) data,false);
        imagesGridview.setAdapter(gridAdapter);

        imagePathList = new ArrayList<>();      //load all paths of the search results
        for(ImageInfo image:data){
            imagePathList.add(image.getPath());
        }

        imagesGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageInfo item = (ImageInfo) parent.getItemAtPosition(position);
                String imagePath = item.getPath();
                Intent detailIntent = new Intent(thisContext,ImageDetailActivity.class);
                detailIntent.putExtra("path",imagePath);
                detailIntent.putStringArrayListExtra("ALLPATH",imagePathList);
                detailIntent.putExtra("index",position);        //the index of current image in the path list
                startActivity(detailIntent);
            }
        });

    }
}
