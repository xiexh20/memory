package detectors.tflite;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gallery.ImageInfo;
import ocr.FilePath;

/**
 * a wrapped class for interpreting Object Detection
 * created by Xianghui Xie on 2019/5/23
 */
public class WrappedDetector {

    //data variables
    private static final Logger LOGGER = new Logger();
    private Classifier detector;
    private Activity dActivity;     //the activity to do detector
    private List<String> labels;    //the most likely labels
    private int cropSize;

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private final float MINI_CONFIDENCE = 0.3f;
    private final int MAX_LABELNUM = 5;

    public WrappedDetector(Activity dActivity)
    {
        cropSize = TF_OD_API_INPUT_SIZE;
        this.dActivity = dActivity;
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            dActivity.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
//            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            dActivity.getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            dActivity.finish();         //destroy this activity
        }
    }


    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API
    }

    /**
     * do object detection on a given image infor (image path)
     * @param imageInfo
     * @return
     */
    public List<Classifier.Recognition> doDetectionOn(ImageInfo imageInfo)
    {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imageInfo.getPath(), bmOptions);
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, cropSize, cropSize,true);
        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

        return results;
    }

    /**
     * do object detection on given image uri
     * @param imageUri
     * @return
     */
    public List<Classifier.Recognition> deDetectionOn(Uri imageUri)
    {
        String absPath = FilePath.getRealPathFromUriAboveApi19(dActivity,imageUri);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(absPath, bmOptions);
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, cropSize, cropSize,true);

        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        return results;
    }

    /**
     * do object detection on given image path
     * @param absPath
     * @return
     */
    public List<Classifier.Recognition> deDetectionOn(String absPath)
    {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(absPath, bmOptions);
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, cropSize, cropSize,true);

        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        return results;
    }

    /**
     * get the list of most likely labels
     * @param detectResults
     * @return
     */
    public List<String> getMostLiklyLabels(List<Classifier.Recognition> detectResults)
    {
        ArrayList<String> labels = new ArrayList<>();
        for(Classifier.Recognition result:detectResults){
            if(result.getConfidence()>=MINI_CONFIDENCE){
                String label = result.getTitle();
                if(!labels.contains(label)&&labels.size()<=MAX_LABELNUM){    //if this is a new label, and not exceed maximum allowable label number
                    labels.add(label);
                }
            }
        }

        if(labels.size()==0){
            //if the minimum confidence is not very good, pick the first two labels
            for(Classifier.Recognition result:detectResults){
                String labelName = result.getTitle();
                if(!labels.contains(labelName)&&labels.size()<2){
                    labels.add(labelName);
                }
            }
        }
        return labels;
    }


}
