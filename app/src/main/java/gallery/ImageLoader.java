package gallery;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * a class to load all images and store imageView information and folder information
 *  and also use OCR analyzer to detect all text
 */
public class ImageLoader {
    private List<FolderInfo> folderList;
    private List<ImageInfo> imageList;
    private AppCompatActivity activity;
    private List<ImageInfo> newImageList;       //a list to store images added to the file system
    private HashSet<ImageInfo> previousImages;    //a list to store previous images (use Hash set to improve speed for searching)

    public ImageLoader(AppCompatActivity activity) {
        this.activity = activity;
        folderList = new ArrayList<>(20);
        imageList = new ArrayList<>(1000);
        previousImages = new HashSet<>(1000);
        newImageList = new ArrayList<>(20);
    }

    public void restartLoading()
    {
        newImageList.clear();       //clear new image list
        loadImages();
    }
    private void loadImages()
    {
        //get the Uri from MediaStore content provider pointing to the media
        // (here media refers to the photos stored on the device)
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        //Now retrieving a cursor object containing the data column for the imageView media is required
        // to be performed. The data column will contain the path to the particular imageView files on the disk.
        String[] projection = {MediaStore.MediaColumns.DATA};
        String [] myProjection ={MediaStore.Images.Media.DATA,          //the data path
                MediaStore.Images.Media.DISPLAY_NAME,   //imageView name
                MediaStore.Images.Media.DATE_ADDED,  //date and time of the imageView
                MediaStore.Images.Media._ID,        //imageView id in the database
                MediaStore.Images.Media.SIZE};      //imageView size, unit: Byte
//        Cursor cursor = getContentResolver().query(uri, projection,
//                null, null, null);
        Cursor cursor = activity.getContentResolver().query(uri, myProjection,
                null, null, myProjection[2]+" DESC");

        //retrieve the path of all the images by iterating through the cursor object obtained
        // in the previous step and keep adding those paths to an ArrayList<String>.
        // Creating Media objects passing in the imageView path and concurrently adding those
        // Media objects to an ArrayList<Media> to be done thereafter.
        // Finally, the dataset(ArrayList<Media> in this case) containing Media objects for all the
        // images is required to be passed to the Media Adapter in order to display all the photos in the UI.

        List<ImageInfo> imageInfoList = new ArrayList<>(1000);
        List<FolderInfo> folderInfoList = new ArrayList<>(20);
        HashSet currentImages = new HashSet(previousImages.size());
        List<ImageInfo> tempNewImageList = new ArrayList<>();
//        HashSet previousImages = new HashSet(this.imageList);   //it takes some time to create the image
        while (cursor.moveToNext()) {
            //process data
//            String absolutePathOfImage = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            int size = cursor.getInt(cursor.getColumnIndexOrThrow(myProjection[4]));

            boolean isSmall = size<5000;    //if the imageView is very small(<5000KB), do not load
            if(!isSmall){
                //if not a small imageView: add to list, start OCR analyzer and find the folder of the imageView
                String absolutePathOfImage = cursor.getString(cursor.getColumnIndexOrThrow(myProjection[0]));   //get imageView path
                File imageFile = new File(absolutePathOfImage);                  // get the file of the imageView
                Uri imageUri = Uri.fromFile(imageFile);


                //get other information of the image
                String imageName = cursor.getString(cursor.getColumnIndexOrThrow(myProjection[1]));
                long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(myProjection[2]));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(myProjection[3]));
                ImageInfo tempImage = new ImageInfo(absolutePathOfImage,imageName,dateTime);
                //add current image to the list (coach)
                imageInfoList.add(tempImage);
                currentImages.add(tempImage);

                //find the folder of the imageView
                File folderFile = imageFile.getParentFile();                    // 获取图片上一级文件夹
                String folderName = folderFile.getName();
                String folderPath = folderFile.getAbsolutePath();

                //too slow, influence the app speed significantly
//                if(!this.imageList.contains(tempImage)){
//                    //if this is a new image
//                    newImageList.add(tempImage);
//                }

                //HashSet is much faster than ArrayList in Searching, but still a little bit stuck
                if(!previousImages.contains(tempImage)&&previousImages.size()!=0){
                    //if this is not the first time to load images and this image does not exist in previous image list
                    tempNewImageList.add(tempImage);
                }

                FolderInfo tempFolderInfo = new FolderInfo(folderName,folderPath);
                int folderIndex = folderInfoList.indexOf(tempFolderInfo);
                if(folderIndex<0){
                    //a new imageView folder, add folder to the folder list
                    tempFolderInfo.setFirstImage(tempImage);
                    folderInfoList.add(tempFolderInfo);
                }
                else{
                    tempFolderInfo = folderInfoList.get(folderIndex);   //let the folder point to the existing folder
                }
                tempFolderInfo.addImage(tempImage);         //add this imageView to the folder list
            }
        }

        //show folder and detected text in main activity

//        imageView.setImageURI(Uri.fromFile(new File(folderInfoList.get(0).getFirstImage().getPath())));
//        imageInfoList.clear();
        folderList.clear();     //clear folder and imageView list before updating them
        imageList.clear();
//        previousImages.clear();
        this.folderList = folderInfoList;
        this.imageList = imageInfoList;
        this.previousImages = currentImages;
        newImageList = tempNewImageList;

    }

    /**
     * get all folder list
     * @return
     */
    public List<FolderInfo> getFolderList() {
        return folderList;
    }

    public List<ImageInfo> getImageList() {
        return imageList;
    }

    public List<ImageInfo> getNewImageList() {
        return newImageList;
    }

    /**
     * get a set of image in previous loading ( used in synchronizing database)
     * @return
     */
    public HashSet<ImageInfo> getPreviousImages() {
        return previousImages;
    }
}
