package anlin.softdev.kuleuven.memories;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import database.Device;
import database.Label;
import database.LabelGroup;
import database.NewSQLService;
import detectors.tflite.Classifier;
import detectors.tflite.WrappedDetector;
import gallery.ImageInfo;
import gallery.ImageLoader;
import ocr.DeviceIDUtil;
import ocr.FilePath;
import ocr.OCRAnalyzer;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /* some constants*/
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int MY_PERMISSIONS_REQUESTS = 0;

    /* view variables */
    private SearchView searchView;
    private Button btnLabel;
    private Button btnText;
    private Button btnBoth;
    private ImageButton btnUpdate;
    private ImageButton btnSelectImage;


    /* data variables */
    private Uri imageUri;
    private ImageLoader imageLoader;
    private NewSQLService gtService;     //group t MySQL service
    private Context mContext;               //variable to pass to inner class
    /* Image classification variable*/
    private Classifier classifier;
    //    private Logger logger;
    private Bitmap bitmap = null;
    private int clickCount = 0;     //a number to count the number of clicks on a view->to add folders

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* views initialization */
        initViews();
        requestPermissions(MainActivity.this);          //request permissions from android system

        /* data initiation*/
        mContext = this;
        Device device = new Device(DeviceIDUtil.getAndroidId(this), DeviceIDUtil.getDeviceName());
        gtService = new NewSQLService(this, device);

        //at the begining of the app, add device to the remote database, do not change device later on
        gtService.addDevice(device);
        imageLoader = new ImageLoader(this);
        //necessary configuration to activate search dialog when the user starts typing
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        //set toolbar as action bar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = findViewById(R.id.searchView);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
        searchView.setQueryRefinementEnabled(true);

        MySettingFragment.setPrefContext(this);
        MySettingFragment.setActivity(this);

    }

    private void initViews() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        searchView = findViewById(R.id.searchView);

        btnUpdate = findViewById(R.id.imageBtnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatabase();
            }
        });
        btnSelectImage = findViewById(R.id.imageBtnSelectImages);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageLoader.restartLoading();
                //start a new activity to select images
                Intent selectImageIntent = new Intent(mContext, SelectImageActivity.class);
                mContext.startActivity(selectImageIntent);       //start new activity in a non-activity class: succeed
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            //searchView.setQuery("camera",false);
            String filename = System.currentTimeMillis() + ".jpg";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);  //start camera
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_CAMERA); //open camera, analyze received picture

            //test OCR speed
//            imageLoader.restartLoading();
//            List<ImageInfo> imageInfos = imageLoader.getFolderList().get(44).getAllImages();    //get OCR test images
//            OCRAnalyzer ocrAnalyzer = new OCRAnalyzer(this);
//            ocrAnalyzer.testDiffScaleSpeed(imageInfos);

        } else if (id == R.id.nav_gallery) {
            //handle the gallery view action

            Intent intent = new Intent();
            intent.setType("image/*");  //indicate the type of data to return, in this case: all images
//                intent.setType("video/mp4");  //indicate the type of data to return: in this case, mp4 video
//                intent.setType("image/png");    //all png images
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_GALLERY);    //start show gallery and let user to choose a picture to analyze
//            startActivity(intent);      //start show gallery

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            //start option activity
            Intent intent = new Intent(this,OptionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            //test add all local images to database
//            imageLoader.restartLoading();
//            OCRAnalyzer ocrAnalyzer = new OCRAnalyzer(this);
//
//            int totalNum = 0;
//            for (int i = 0; i < imageLoader.getFolderList().size(); i++) {
//
//                List<ImageInfo> imageInfos = imageLoader.getFolderList().get(i).getAllImages(); //get images of the this folder
//
//                for (int count = 0; count < imageInfos.size(); count++) {
//                    ImageInfo thisImage = imageInfos.get(count);
////                    doClassificationOn(thisImage);
//                    //do OCR analysis
////                    String detected = ocrAnalyzer.analyzeImage(Uri.fromFile(new File(thisImage.getPath())));
////                    thisImage.setDetectedText(detected);
////                    gtService.addImage(thisImage);
//                    gtService.updateImageLG(thisImage);
//
//                    totalNum++;
//                }
//            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*request permissions from andriod system*/
    private void requestPermissions(Context context) {
        List<String> requiredPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //request external storage permission
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //request camera permission
            requiredPermissions.add(Manifest.permission.CAMERA);
        }

        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context,
                    requiredPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUESTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                } else {
                    // FIXME: Handle this case the user denied to grant the permissions
                    Log.i("ERROR:", "permission denied");
                }
                break;
            }
            default:
                // TODO: Take care of this case later
                break;
        }
    }

    /*
    when will this be called? after the activityForResult is finished, the data(image) will be passed to this method
    automatically by intent, and also the request code to specify which activity is finished.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {  //operation succeeded, an image is selected from local storage
                    String absPath = FilePath.getRealPathFromUriAboveApi19(this, data.getData());
                    //start detail image activity
                    inspectImageDetail(absPath);
                }
                break;
            case REQUEST_CAMERA:    //use camera
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        String absPath = FilePath.getRealPathFromUriAboveApi19(this,imageUri);
                        inspectImageDetail(absPath);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void inspectImageDetail(String absPath) {
        Intent detailIntent = new Intent(this, ImageDetailActivity.class);
        ArrayList<String> imagePathList = new ArrayList<>();
        imagePathList.add(absPath);
        detailIntent.putStringArrayListExtra("ALLPATH", imagePathList);
        detailIntent.putExtra("index", 0);        //the index of current image in the path list
        startActivity(detailIntent);
    }

    protected String showResultsOfClassification(List<Classifier.Recognition> results) {
        String result = "";
        if (results != null && results.size() >= 3) {
            Classifier.Recognition recognition = results.get(0);

            if (recognition != null) {
                if (recognition.getTitle() != null) result = result + recognition.getTitle();
                if (recognition.getConfidence() != null)
                    result = result + " " + recognition.getConfidence() + " ";
            }

            Classifier.Recognition recognition1 = results.get(1);
            if (recognition1 != null) {
                if (recognition1.getTitle() != null) result += recognition1.getTitle();
                if (recognition1.getConfidence() != null)
                    result = result + " " + recognition1.getConfidence() + " ";
            }

            Classifier.Recognition recognition2 = results.get(2);
            if (recognition2 != null) {
                if (recognition2.getTitle() != null) result += recognition2.getTitle();
                if (recognition2.getConfidence() != null)
                    result = result + " " + recognition2.getConfidence() + " ";
            }
        }
        return result;
    }


    /**
     * scan local image files, find newly added images and add them in remote database
     * find deleted images, delete them in remote database
     */
    private void updateDatabase() {
        new Thread(() -> {
            try {

                //prepare image path list
                HashSet<ImageInfo> previousImages = imageLoader.getPreviousImages();    //store previous image path
                imageLoader.restartLoading();
                List<ImageInfo> newImages = imageLoader.getNewImageList();              //get newly added image list
                HashSet<ImageInfo> currentImages = imageLoader.getPreviousImages();     //get current image path
                previousImages.removeAll(currentImages);        //find the deleted image path

                for(ImageInfo image:previousImages){
                    //delete images in remote database
                    gtService.deleteImageInDB(image.getPath());
                }

                //analyze newly added images
                OCRAnalyzer ocrAnalyzer = new OCRAnalyzer(mContext);              //to perform OCR analysis
                WrappedDetector detector = new WrappedDetector(this);   //to perform object detection
                for(ImageInfo image:newImages){
                    String detectedText = ocrAnalyzer.analyzeImage(Uri.fromFile(new File(image.getPath())));
                    image.setDetectedText(detectedText);

                    //do object detection
                    List<detectors.tflite.Classifier.Recognition> results = detector.deDetectionOn(image.getPath());
                    List<String> labelNames = detector.getMostLiklyLabels(results);
                    ArrayList<Label> labels = new ArrayList<>();
                    for(String name:labelNames){
                        labels.add(new Label(name));
                    }
                    LabelGroup labelGroup = new LabelGroup(labels);
                    image.setLabels(labelGroup);

                    gtService.addImage(image);      //add new image to databse
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
