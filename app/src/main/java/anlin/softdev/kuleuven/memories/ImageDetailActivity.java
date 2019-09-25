package anlin.softdev.kuleuven.memories;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gallery.ScreenUtils;
import gallery.ZoomageView;

public class ImageDetailActivity extends AppCompatActivity {

    private ZoomageView detailZoomageView;
    private ImageButton btnShare;
    private ImageButton btnMoreInfo;
    private ImageView btnNext;
    private ImageView btnPrev;

    // Keep reference to the ShareActionProvider from the menu
    private androidx.appcompat.widget.ShareActionProvider mShareActionProvider;
//    private String imagePath;
    private Uri shareUri;       //the uri ready to share

    //data variable
    private int imageIndex;     //the index of current image
    private List<String> allPathList;   //the list of all result image, for slide show

    //permissions code
    private static final int PERMISSIONS_REQUEST_ON_BUTTON_CLICK = 11;
    private static final int PERMISSIONS_REQUEST_TO_LOAD_IMAGE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);


        initViews();
        if(!hasExternalStoragePermission()){    //actually this is not needed as long as the MainActivity has been granted storage permission
            requestPermission(PERMISSIONS_REQUEST_TO_LOAD_IMAGE);   //if permission not granted, request permission
        }

    }

    private void initViews() {
        detailZoomageView = findViewById(R.id.zoomageView);
        ViewGroup.LayoutParams params = detailZoomageView.getLayoutParams();
        params.width = ScreenUtils.getScreenWidth(this);        //set the with of the image view to fit the screen

        btnShare = findViewById(R.id.imageBtnShare);      //image button to share this image
        btnShare.setOnClickListener(v -> shareImageAction());
        btnMoreInfo = findViewById(R.id.imageBtnMoreInfo); //imager button to show more information about this image
        btnMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreImageInfo();
            }
        });
        btnNext = findViewById(R.id.imageBtnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextImage();
            }
        });
        btnPrev = findViewById(R.id.imageBtnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviousImage();
            }
        });

//        imagePath = getIntent().getStringExtra("path");
//        Glide.with(this).load(imagePath).into(detailZoomageView);   //load image
        imageIndex = getIntent().getIntExtra("index",0);     //get the index of the image in the list, default 0
        allPathList = getIntent().getStringArrayListExtra("ALLPATH");
        Glide.with(this).load(allPathList.get(imageIndex)).into(detailZoomageView);
    }

    /**
     * show next image
     */
    private void showNextImage() {
        imageIndex++;
        if(imageIndex>=allPathList.size()){
            imageIndex =0;  //return to the first image
        }
        Glide.with(this).load(allPathList.get(imageIndex)).into(detailZoomageView);
    }

    /**
     * show previous image
     */
    private void showPreviousImage()
    {
        imageIndex--;
        if(imageIndex<0){
            imageIndex=allPathList.size()-1;    //go to the last picture
        }
        Glide.with(this).load(allPathList.get(imageIndex)).into(detailZoomageView);
    }

    /**
     * @author: nikkulshrestha
     * adapted from https://github.com/nikkulshrestha/imageShare.git
     * this code snip is only for education, any commercial is not allowed
     */
    private void shareImageAction()
    {
        File imageFile = new File(allPathList.get(imageIndex));
        if (hasExternalStoragePermission()){
            shareUri = FileProvider.getUriForFile(
                    ImageDetailActivity.this,
                    "anlin.softdev.kuleuven.memories.provider", // provider authority
                    imageFile);             //prepare sharable Uri
            shareImage(shareUri);
        }
        else {
            requestPermission(PERMISSIONS_REQUEST_ON_BUTTON_CLICK);
        }
    }
    private void showMoreImageInfo() {
        //TODO: show more information about this image
        if(allPathList.get(imageIndex).endsWith(".jpg")){
            //if this is a jpg image, use ExiInterface to extract information
            File imageFile = new File(allPathList.get(imageIndex));
            long size = imageFile.length()/1024;     //get the size of the image, unit:KB

            try {
                ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());

                String TAG_APERTURE = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
                String TAG_DATETIME = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                String TAG_EXPOSURE_TIME = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                String TAG_FLASH = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
                String TAG_FOCAL_LENGTH = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                String TAG_IMAGE_LENGTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                String TAG_IMAGE_WIDTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                String TAG_ISO = exifInterface.getAttribute(ExifInterface.TAG_ISO);
                String TAG_MAKE = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                String TAG_MODEL = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                String TAG_ORIENTATION = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
                String TAG_WHITE_BALANCE = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

                String sizeInfo = size+" KB";
                if(size>1024){
                    float sizeInMB = (float)Math.round(100*size/1024)/100;
                    sizeInfo = sizeInMB+" MB"; //change unit to MB
                }
                StringBuilder infoBuilder = new StringBuilder();
                if(TAG_DATETIME!=null) {
                    infoBuilder.append(TAG_DATETIME + "\n");
                }
                infoBuilder.append(TAG_IMAGE_LENGTH+"×"+TAG_IMAGE_WIDTH+"  "+sizeInfo+"\n\n");
                infoBuilder.append("Path:"+allPathList.get(imageIndex));

                //get the name of this image
                int index = allPathList.get(imageIndex).lastIndexOf('/');
                String imageName = allPathList.get(imageIndex).substring(index+1);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(infoBuilder.toString())
                        .setTitle(imageName)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * @author: nikkulshrestha
     * @source: https://github.com/nikkulshrestha/imageShare.git
     * this code snip is only for education, any commercial is not allowed
     * @param imageURI
     */
    private void shareImage(Uri imageURI) {
        if (imageURI != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageURI);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Select App"));
        }
    }
    private boolean hasExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                    .setTitle("Storage Permission")
                    .setMessage("Storage permission is required to share image")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showRequestPopup(requestCode);
                        }
                    })
                    .setNegativeButton("Cancel", null);
            alertBuilder.create().show();
        } else {
            // No explanation needed; request the permission
            showRequestPopup(requestCode);
        }
    }

    private void showRequestPopup(int requestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                requestCode);
    }
}
