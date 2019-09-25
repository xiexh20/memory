package anlin.softdev.kuleuven.memories;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import database.Label;
import database.LabelGroup;
import database.NewSQLService;
import detectors.tflite.WrappedDetector;
import gallery.FolderInfo;
import gallery.ImageInfo;
import gallery.ImageLoader;
import gallery.MyGridViewAdapter;
import ocr.OCRAnalyzer;

/**
 * the activity for user to select images to import to database
 */
public class SelectImageActivity extends AppCompatActivity {

    private Spinner folderListSpinner ;
    private ImageLoader imageLoader;
    private GridView imagesGridView;
    private MyGridViewAdapter gridAdapter;
    private Context mContext;
    private CheckBox switchSelectAll;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);
        mContext = this;
        imageLoader = new ImageLoader(this);
        initViews();
    }

    /**
     * initialize views and listeners
     */
    private void initViews() {
        imageLoader.restartLoading();
        List<FolderInfo> folders = imageLoader.getFolderList();
        ArrayList<String> folderNames = new ArrayList<>(folders.size());
        for(FolderInfo folder:folders){
            folderNames.add(folder.getFolderName()+" ["+folder.getImageCount()+"]");
        }
        ArrayAdapter<String> folderAdapter = new ArrayAdapter<>(this,R.layout.folder_nameview_layout,folderNames);
        folderListSpinner = findViewById(R.id.folderSpinner);
        folderListSpinner.setAdapter(folderAdapter);
        folderListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String folderName =  textView.getText().toString();
                gridAdapter = new MyGridViewAdapter(mContext,R.layout.grid_item_layout, (ArrayList<ImageInfo>) folders.get(position).getAllImages(),true);
//                gridAdapter.setItemSelectable(true);
                imagesGridView.setAdapter(gridAdapter);
                switchSelectAll.setChecked(false);      //set to unchecked
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        gridAdapter = new MyGridViewAdapter(this,R.layout.grid_item_layout, (ArrayList<ImageInfo>) folders.get(0).getAllImages(),true);
//        gridAdapter.setItemSelectable(true);
        imagesGridView = findViewById(R.id.selectImageGridView);
        imagesGridView.setAdapter(gridAdapter);

        switchSelectAll = findViewById(R.id.switchSelectAll);
        switchSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.isChecked()){
                    checkAll(true);        //box checked, check all images
                }
                else{
                    checkAll(false);     //uncheck all
                }
            }
        });

        btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImagesToDB();
//                updateLabelsInDB();       //this function skip OCR, do object detection only
//                updateLGExceptPhotos();   //this method skip photos folder in my sd card, do object detection only

            }
        });
    }

    /**
     * get selected images, do OCR and classification, and then add images to DB, return to main activity
     */
    private void addImagesToDB()
    {
        List<ImageInfo> selectedImages = gridAdapter.getSelectedImages();
        int nrImages = selectedImages.size();

        //run this on another thread
        new Thread(() -> {
            try {
                OCRAnalyzer ocrAnalyzer = new OCRAnalyzer(mContext);
//                WrappedClassifier classifier = new WrappedClassifier(this);
//                classifier.setClassifier(Classifier.Model.FLOAT, Classifier.Device.CPU, 1);     //set thread to 1,avoid too much work
                NewSQLService gtService = new NewSQLService(this);
                WrappedDetector detector = new WrappedDetector(this);
                for(ImageInfo image:selectedImages){
                    String detectedText = ocrAnalyzer.analyzeImage(Uri.fromFile(new File(image.getPath())));
                    image.setDetectedText(detectedText);        //do OCR analysis

                    //do object detection
                    List<detectors.tflite.Classifier.Recognition> results = detector.deDetectionOn(image.getPath());
                    List<String> labelNames = detector.getMostLiklyLabels(results);
                    ArrayList<Label> labels = new ArrayList<>();
                    for(String name:labelNames){
                        labels.add(new Label(name));
                    }
                    LabelGroup labelGroup = new LabelGroup(labels);
                    image.setLabels(labelGroup);
                    gtService.addImage(image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        /*
        @author：guolin
        @source：CSDN
        @original article：https://blog.csdn.net/guolin_blog/article/details/51336415
        */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(nrImages+" images selected")
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

    /**
     * a method to update labels only in remote database (used when change our model)
     */
    private void updateLabelsInDB()
    {
        List<ImageInfo> selectedImages = gridAdapter.getSelectedImages();
        int nrImages = selectedImages.size();

        //run this on another thread
        new Thread(() -> {
            try {
//                OCRAnalyzer ocrAnalyzer = new OCRAnalyzer(mContext);
//                WrappedClassifier classifier = new WrappedClassifier(this);
                WrappedDetector detector = new WrappedDetector(this);
                NewSQLService gtService = new NewSQLService(this);
                HashMap<ImageInfo,List<detectors.tflite.Classifier.Recognition>> complexImages = new HashMap<>();
                //process classifier result
                for(ImageInfo image:selectedImages){
//                    String detectedText = ocrAnalyzer.analyzeImage(Uri.fromFile(new File(image.getPath())));
                    image.setDetectedText("");
                    List<detectors.tflite.Classifier.Recognition> results = detector.deDetectionOn(image.getPath());
                    List<String> labelNames = detector.getMostLiklyLabels(results);

                    ArrayList<Label> labels = new ArrayList<>();
                    for(String name:labelNames){
                        labels.add(new Label(name));
                    }
                    LabelGroup labelGroup = new LabelGroup(labels);
                    image.setLabels(labelGroup);
                    if(labelNames.size()>4){
                        //stop
                        complexImages.put(image,results);
                    }
//                    gtService.addImage(image);
                    gtService.updateImageLG(image);
                }
                if(complexImages.size()>0){
                    Toast.makeText(this,"total number of complex images"+complexImages.size(),Toast.LENGTH_SHORT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        /*
        @author：guolin
        @source：CSDN
        @original article：https://blog.csdn.net/guolin_blog/article/details/51336415
        */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(nrImages+" images selected")
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

    /**
     * check/uncheck all images
     * @param status
     */
    private void checkAll(boolean status)
    {
        int totalNum = gridAdapter.getCount();
        for(int i = 0;i<totalNum;i++) {
            gridAdapter.setItemChecked(i,status);
        }
        if(status==true){
            gridAdapter.setSelectedImages(gridAdapter.getAllImages());

        }
        else {  //uncheck all
            gridAdapter.setSelectedImages(new ArrayList<>());
        }
        gridAdapter.notifyDataSetChanged();     //this will update the view
    }
    //update label groups of all images except photos
    private void updateLGExceptPhotos()
    {
        imageLoader.restartLoading();
        WrappedDetector detector = new WrappedDetector(this);
        NewSQLService gtService = new NewSQLService(this);

        try{
            new Thread(() -> {
                int total = 0;
                for(int i = 0;i<imageLoader.getFolderList().size();i++){
                    if(i!=1) {  //load images except photos
                        List<ImageInfo> imageInfos = imageLoader.getFolderList().get(i).getAllImages();
                        for (int k = 0; k < imageInfos.size(); k++) {
                            ImageInfo image = imageInfos.get(k);

                            image.setDetectedText("");
                            List<detectors.tflite.Classifier.Recognition> results = detector.deDetectionOn(image.getPath());
                            List<String> labelNames = detector.getMostLiklyLabels(results);
                            ArrayList<Label> labels = new ArrayList<>();
                            for (String name : labelNames) {
                                labels.add(new Label(name));
                            }
                            LabelGroup labelGroup = new LabelGroup(labels);
                            image.setLabels(labelGroup);
                            gtService.updateImageLG(image);
                            total++;

                            }


                        }

                    }
                }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }



}
