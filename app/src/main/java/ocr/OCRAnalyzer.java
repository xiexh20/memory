package ocr;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gallery.ImageInfo;

import static android.content.ContentValues.TAG;

/**
 * the core class for OCR analysis
 * adapted by Xianghui Xie on 2019/5/10
 * from https://github.com/komamitsu/Android-OCRSample.git
 */

public class OCRAnalyzer {

    private Uri imageUri;
    private String imagePath;
    private String imageName;
    private String detectedText;
    private Context analyzeContext;     //the context for analyzing image

    public OCRAnalyzer(Context context, Uri imageUri) {
        this.imageUri = imageUri;
        analyzeContext = context;

        if(imageUri!=null){
            //get absolute image path
            imagePath = FilePath.getRealPathFromUriAboveApi19(context,imageUri);
            //get filename: cut the file name out from file path
            int cut = imagePath.lastIndexOf('/');
            if (cut != -1){
                imageName =imagePath.substring(cut + 1);
            }
        }
    }

    public OCRAnalyzer(Context analyzeContext) {
        this.analyzeContext = analyzeContext;
    }

    /**
     *
     * @param imageUri: the Uri of image to be analyzed
     * @return OCR analyze result
     */
    public String analyzeImage(Uri imageUri)
    {
        inspect(imageUri);
        String result = detectedText;
        return result;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getImageName() {
        return imageName;
    }

    public String getDetectedText() {
        return detectedText;
    }

    public void analyze()
    {
        inspect(imageUri);
    }
    /*
            the core method for text recognition
             */
    private void inspectFromBitmap(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(analyzeContext).build();
        try {
            if (!textRecognizer.isOperational()) {
                new AlertDialog.
                        Builder(analyzeContext).
                        setMessage("Text recognizer could not be set up on your device").show();
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            textRecognizer.isOperational(); ///?what does this do?
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);

            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }
            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedTextBuilder = new StringBuilder();
            for (TextBlock textBlock : textBlocks) {

                if (textBlock != null && textBlock.getValue() != null) {
                    detectedTextBuilder.append(textBlock.getValue());
                    detectedTextBuilder.append("|");    //use this symbol to separate text fragment
                }
            }

            //add the name and path of the selected image
//            detectedText.append("\nFROM\n");
//            detectedText.append("image path:"+imagePath+"\n");
//            detectedText.append("image name:"+imageName+"\n");

//            detectedTextView.setText(detectedText);
            this.detectedText = detectedTextBuilder.toString();   //set detected text
        }
        finally {
            textRecognizer.release();
        }
    }

    /**
     * do grey to an image
     * @param bmIn
     * @return
     */
    public static Bitmap doGreyscale(Bitmap bmIn) {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(bmIn.getWidth(), bmIn.getHeight(), bmIn.getConfig());
        // pixel information
        int A, R, G, B;
        int pixel;

        // get image size
        int width = bmIn.getWidth();
        int height = bmIn.getHeight();

        // scan through every single pixel
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = bmIn.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }


    private void inspect(Uri uri) {
        InputStream is = null;
        Bitmap bitmap = null;

        try {
            is = analyzeContext.getContentResolver().openInputStream(uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            inspectFromBitmap(bitmap);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed to find the file: " + uri, e);
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close InputStream", e);
                }
            }
        }
    }


    /**
     * a method to test whether grey or zip an image could improve OCR efficiency or not
     * @param images
     */
    public Bundle OCRSpeedTest(List<ImageInfo> images, int scale)
    {
        ArrayList<Long> greyTime = new ArrayList<>(images.size());
        ArrayList<Long> ocrOrigin = new ArrayList<>(images.size());
        ArrayList<Long> ocrWithScale = new ArrayList<>(images.size());
        ArrayList<Integer> difference = new ArrayList<>(images.size());
        ArrayList<Long> ocrTimeDifference = new ArrayList<>(images.size());
        ArrayList<String> ocrResultNoScale = new ArrayList<>(images.size());
        ArrayList<String> ocrResultScale = new ArrayList<>(images.size());
        ArrayList<Long> scaleTime = new ArrayList<>(images.size());
        float[] ocrTimeRatio = new float[images.size()];

        int count = 0;
        for(ImageInfo imageInfo:images){
            //do preparation
            String path = imageInfo.getPath();
            Bitmap bmIn = BitmapFactory.decodeFile(path);

            //step 1: do grey to the
//            long timeStart = System.nanoTime();
//            Bitmap bmOut = doGreyscale(bmIn);
//            long timeStop = System.nanoTime();
//            greyTime.add(timeStop-timeStart);

            //step 1: do scale up
            long timeStart = System.nanoTime();
            Bitmap scaledBm = Bitmap.createScaledBitmap(bmIn,bmIn.getWidth()/scale,bmIn.getHeight()/scale,false);
            long timeStop = System.nanoTime();
            scaleTime.add(timeStop-timeStart);

            //step 2: do OCR without grey
            detectedText = "";      //clear result
            timeStart = System.nanoTime();
            inspectFromBitmap(bmIn);
            timeStop = System.nanoTime();
            ocrOrigin.add(timeStop-timeStart);
            ocrResultNoScale.add(detectedText);

            //step 3: do OCR with grey
//            timeStart = System.nanoTime();
//            inspectFromBitmap(bmOut);
//            timeStop = System.nanoTime();
//            ocrWithGrey.add(timeStop-timeStart);
            //step 3: do OCR with scale up
            detectedText = "";      //clear result
            timeStart = System.nanoTime();
            inspectFromBitmap(scaledBm);
            timeStop = System.nanoTime();
            ocrWithScale.add(timeStop-timeStart);
            ocrResultScale.add(detectedText);

            //step 4: calculate time difference
            int timeDiff =(int)((ocrOrigin.get(count)-ocrWithScale.get(count)-scaleTime.get(count))/10);
            difference.add(timeDiff);
            ocrTimeDifference.add(ocrOrigin.get(count)-ocrWithScale.get(count));
            ocrTimeRatio[count] = (float)(1.0*ocrWithScale.get(count)/ocrOrigin.get(count));
            count++;
        }

        Bundle data = new Bundle();
        data.putStringArrayList("ocrResultNoScale",ocrResultNoScale);
        data.putStringArrayList("ocrResultWithScale",ocrResultScale);
        data.putIntegerArrayList("timeDiff",difference);
        data.putFloatArray("timeRatio",ocrTimeRatio);
        return data;
    }

    /**
     *
     * @param imageInfos
     */
    public void testDiffScaleSpeed(List<ImageInfo> imageInfos)
    {
        ArrayList<Bundle> bundles = new ArrayList<>();
       for(int i = 2;i<6;i++){
           Bundle bundle = OCRSpeedTest(imageInfos,i);
           bundles.add(bundle);
       }
       int test = 0;
    }
}
