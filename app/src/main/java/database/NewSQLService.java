package database;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import anlin.softdev.kuleuven.memories.SearchResultActivity;
import gallery.ImageInfo;
import ocr.DeviceIDUtil;

/**
 * new MySQL service based on DataProcessor, each service create a new Listener
 * created by Xianghui Xie on 2019/5/16
 */

public class NewSQLService {

    //Service url here
    private final String CHECK_DEVICE = "https://studev.groept.be/api/a18_sd602/checkDevice/deviceid";
    private final String ADD_DEVICE = "https://studev.groept.be/api/a18_sd602/addDevice/deviceid/devicename";
    private final String ADD_LABEL = "https://studev.groept.be/api/a18_sd602/addLabel/newlabelname";
    //this service is used for finding paths by text
    private final String FINDPATH_BY_TEXT ="";
    //find an image from remote database by image path and device id
    private final String FINDIMAEG_BY_PATH = "https://studev.groept.be/api/a18_sd602/findImageByPath/deviceid/imagepath";
    //get all information of image
    private final String GET_IMAGEINFO = "https://studev.groept.be/api/a18_sd602/getImageInfos";
    //search image in remote database by input text (find matches in detectedText)
    private final String SEARCHIMAGE_BYTEXT = "https://studev.groept.be/api/a18_sd602/searchImageByText/inputtext/deviceid";
    //search image in remote database by user input (find matches both in label and detectedText)
    private final String SEARCHIMAGE_BYLABEL_TEXT = "https://studev.groept.be/api/a18_sd602/searchImageByLabelText/userinput/deviceid/userinput";
    //search image in remote database by first two labels
    private final String SEARCHIMAGE_BYLABEL = "https://studev.groept.be/api/a18_sd602/searchImageByLabel/labelname/labelname/deviceid";

    //this service is used for adding label group by label name(s)
    private final String ADD_LABELGROUP = "https://studev.groept.be/api/a18_sd602/addLabelGroupWithNames/labelgroupid/labelnamea/labelnameb/labelnamec/labelnamed";
    //this service is used for finding one or more labels based on given label name(s)
    private final String FIND_LABELS = "https://studev.groept.be/api/a18_sd602/findLabels/labelnamea/labelnameb/labelnamec/labelnamed/labelnamee";
    //this service is used for finding the labelGroup id of given a list of label name(s)
    private final String FIND_LABELGROUP = "https://studev.groept.be/api/a18_sd602/findLabelGroup/labelnamea/labelnameb/labelnamec/labelnamed/labelnamee";
    //this service is used for adding image
    private final String ADD_IMAGE = "https://studev.groept.be/api/a18_sd602/addImageWithLabel/imagepath/detectedtext/deviceid/labelgroupid";
    //delete an image in remote database
    private final String DELETE_IMAGE = "https://studev.groept.be/api/a18_sd602/deleteImage/imageid";
    //add image without label group
    private final String ADDIMAGE_NOLABEL = "https://studev.groept.be/api/a18_sd602/addImageNoLabel/imagepath/detectedtext/deviceid";
    //update only the labels of an image
    private final String UPDATE_IMAGELG = "https://studev.groept.be/api/a18_sd602/updateImageLabelGroup/labelgroupid/imageid";


    //placement of unsupported url chars
    private final String REPLACE_SLASHL = "@";
    private final String REPLACE_SPACE = "+";   //the char to replace space in url

    //the keys to replace specific name in service url String
    private final String KEY_imageid = "imageid";
    private final String KEY_imagepath = "imagepath";
    private final String KEY_detextedtext = "detectedtext";
    private final String KEY_labelgroup = "labelgroup";
    private final String KEY_deviceid = "deviceid";
    private final String KEY_inputtext = "inputtext";
    private final String KEY_labelgroupid = "labelgroupid";
    private final String KEY_userinput = "userinput";
    private final String KEY_labelname = "labelname";
    private final String KEY_space = " ";

    //data variable here
    private Context serviceContext;     //the context of service
    private Device thisDevice;      //information of this Android device
    private RequestQueue requestQueue;

    //a count to generate labelGroup id for inserting a row to database, this is needed to update image
    private static int labelGroupCount = 0;

    public NewSQLService(Context serviceContext, Device thisDevice) {
        this.serviceContext = serviceContext;
        this.thisDevice = thisDevice;
        updateLabelGroupCount();        //always update counter when first create service

        //initialize requestQueue
        requestQueue = SingletonRequestQueue.getInstance(serviceContext.getApplicationContext()).getRequestQueue();
        addDevice(thisDevice);      //always add device to remote database at the beginning of the app

        //read label names from txt file, add all labels to the database
//        try {
//            BufferedReader labelsReader = new BufferedReader(new InputStreamReader(serviceContext.getAssets().open("labelmap.txt")));
//            String line = labelsReader.readLine();
//            while (line!=null){
//                addLabel(new Label(line));      //add all labels
//                line = labelsReader.readLine();
//            }
//            labelsReader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

    public NewSQLService(Context context)
    {
        this.serviceContext = context;
        Device device = new Device(DeviceIDUtil.getAndroidId(context),DeviceIDUtil.getDeviceName());
        this.thisDevice = device;
        updateLabelGroupCount();        //always update counter when first create service

        //initialize requestQueue
        requestQueue = SingletonRequestQueue.getInstance(serviceContext.getApplicationContext()).getRequestQueue();
    }

    /**
     * constructor for testing
     */
    public NewSQLService() {
        thisDevice = null;
        updateLabelGroupCount();        //always update counter when first create service
    }

    /**
     * add a device to the remote database
     * @param device
     */
    public void addDevice(Device device)
    {
        String checkDeviceUrl = CHECK_DEVICE.replace("deviceid",device.getDeviceID());

        //although it is defined as final, it will change each time new object is passed. So parameters
        //could be passed in this way, the same to parameter defined as final, they are just local variables
        final String addDeviceUrl = ADD_DEVICE.replace("deviceid",device.getDeviceID())
                .replace("devicename",device.getDeviceName());
        JsonArrayRequest checkDeviceRequest =
                new JsonArrayRequest(Request.Method.GET, checkDeviceUrl, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                //check if remote database have this device
                                if(response==null||response.length()==0){
                                    //if empty: add new device to remote database
                                    JsonArrayRequest addDeviceRequest =
                                            new JsonArrayRequest(Request.Method.GET,addDeviceUrl,null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    //do nothing after addition
                                                }
                                            }, new MyErrorResponse(serviceContext));
//                                    RequestQueue requestQueue = Volley.newRequestQueue(serviceContext);
//                                    requestQueue.add(addDeviceRequest);
                                    SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addDeviceRequest);
                                }
                            }
                        }, new MyErrorResponse(serviceContext));
        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(checkDeviceRequest);
    }


    /**
     * add image information to the remote database
     * @param image
     */
    public void addImage(ImageInfo image)
    {
        //after finishing this I can start loading images in my local gallery :)
        //information needed to be added to the remote database: path(non-null), detectedText(non-null)
        //labelGroup(nullable), deviceID (this device)

        String pathToDB = image.getPath().replaceAll("/", REPLACE_SLASHL);
        String findImageUrl = FINDIMAEG_BY_PATH.replace("deviceid",thisDevice.getDeviceID()).replace("imagepath",pathToDB);
        JsonArrayRequest findImageRequest = new JsonArrayRequest(Request.Method.GET, findImageUrl, null,
                (response -> {
                    //check if this image exist in remote database or not
                    if(response==null||response.length()==0){   //not found in remote database, add it
                        addNewImage(image);
                    }
                    else{   //image found: update image
                        updateImageInfo(image);     //FIXME: update is not efficient enough
                    }
                }),new MyErrorResponse(serviceContext));
        requestQueue.add(findImageRequest);     //add request
    }

    private void addNewImage(ImageInfo image) {
        String path = image.getPath();
        String pathToDB = path.replaceAll("/", REPLACE_SLASHL);   //replace all slash to dollar symbol
        String detectedText = image.getDetectedText();

        //replace unsupported chars
        String textToDB = detectedText.replaceAll(" ", "+")      //replace all space with +
                .replaceAll("/", "@")                            //replace all slash
                .replaceAll("%", "&")                            //replace all percentage mark
                .replaceAll("#", "*");                           //replace all #

        LabelGroup imageLG = image.getLabelGroup();
        Device device = thisDevice;

        //first check if all labels are there ->add labels
        //then check if this label group is there ->add label groups
        //add images

        if(imageLG==null||imageLG.getLabelCount()==0){
            //no label(s) for this image
            String urlBuilder = ADDIMAGE_NOLABEL;   //if no label, use another url
            urlBuilder = urlBuilder.replace("deviceid",device.getDeviceID())
                                    .replace("imagepath",pathToDB);
            if(!textToDB.equals("") ){
                //if some text detected
                urlBuilder = urlBuilder.replace("detectedtext",textToDB);
            }

            String addImageUrl = urlBuilder;
            JsonArrayRequest addImageRequest = new JsonArrayRequest(Request.Method.GET, addImageUrl, null,
                    ((response -> {
                        //add image, no response
                    })),new MyErrorResponse(serviceContext));
            SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addImageRequest);
        }
        else{
            //some labels exist
            ArrayList<Label> labels = imageLG.getLabels();
            char[] varIdentifier = {'a','b','c','d','e'};   //this array is for passing parameters to url
            String urlBuilder1 = FIND_LABELS;       //url builder for finding labels
            String urlBuilder2 = ADD_LABELGROUP;    //url builder for adding labelGroup
            String urlBuilder3 = FIND_LABELGROUP;   //url builder for finding a labelGroup in remote database given a

            labelGroupCount++;  //update labelGroupCount
            imageLG.setLabelGroupId(labelGroupCount);
            //to add a label group, the labelgroup id needed to be specified (for later update image)
            urlBuilder2 = urlBuilder2.replace("labelgroupid",labelGroupCount+"");

            for(int i= 0;i<labels.size();i++){
                urlBuilder1 = urlBuilder1.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
                urlBuilder2 = urlBuilder2.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
                urlBuilder3 = urlBuilder3.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
            }
            String findLabelsUrl = urlBuilder1;         //url to find image labels
            String addLabelGroupUrl = urlBuilder2;      //url to add labelGroup (this will be used only when the label group does not exist in remote database)
            String findLabelGroupUrl = urlBuilder3;     //url to find labelGroup given labels' name
            JsonArrayRequest findLabelsRequest = new JsonArrayRequest(Request.Method.GET, findLabelsUrl, null,
                    (response -> {
                        //check the return JsonArray size
                        ArrayList<Label> labelsTobeAdded = new ArrayList<>();       //the list of labels need to be added to the remote database
                        if(response==null||response.length()==0){
                            //none of the labels exist in remote database, add them all to the database
                            labelsTobeAdded = labels;
                        }
                        else if(response.length()<labels.size()){
                            //part of the labels has been added to the table AllLabels, find out new labels
                            //process received data

                            for(int i = 0;i<response.length();i++){
                                try {
                                    JSONObject jsonObject = (JSONObject)response.get(i);
                                    String labelName = jsonObject.getString("labelName");
                                    for(int j = 0;j<labels.size();j++){
                                        if(!labelName.equalsIgnoreCase(labels.get(j).getLabelName())){
                                            // if not found
                                            labelsTobeAdded.add(new Label(labels.get(j).getLabelName()));
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        //add new labels to remote database
                        for(int k = 0; k<labelsTobeAdded.size(); k++){
                            addLabel(labelsTobeAdded.get(k));        //add service do not need to process data, no problem of multi thread
                        }

                        //TODO: implementing request remote database and find label group
                        //FIXME: delay needed?
                        //all labels has been added to the remote database, then check if this combination has already been added
                        JsonArrayRequest findLabelGroupRequest = new JsonArrayRequest(Request.Method.GET,findLabelGroupUrl,null,
                                ((findLGResponse) -> {
                                    LabelGroup LGtoBeAdded = null;      //the label group to be added to the remote database
                                    if(findLGResponse==null||findLGResponse.length()==0){
                                        //if not existing in remote database, add new labelGroup to remote database
                                        LGtoBeAdded = imageLG;
                                    }
                                    else{ //double check received data
                                        for(int i = 0;i<findLGResponse.length();i++) {
                                            try {
                                                MyJSONObject myJSONObject = new MyJSONObject((JSONObject) findLGResponse.get(i));
                                                String label1 = myJSONObject.getString("label1"); //return null if str = "null"
                                                String label2 = myJSONObject.getString("label2");
                                                String label3 = myJSONObject.getString("label3");
                                                String label4 = myJSONObject.getString("label4");
                                                int remoteLGid = myJSONObject.getInt("imageLabelID");
                                                ArrayList<Label> receivedLabels = new ArrayList<>(4);
                                                if(label1!=null){       //avoid size problem
                                                    receivedLabels.add(new Label(label1));
                                                    if(label2!=null){
                                                        receivedLabels.add(new Label(label2));
                                                        if(label3!=null){
                                                            receivedLabels.add(new Label(label3));
                                                            if(label4!=null){
                                                                receivedLabels.add(new Label(label4));
                                                            }
                                                        }
                                                    }
                                                }

                                                //FIXME: equals method  TEST PASSED!!!-2019-5-17-14:23 :( not yet
                                                LabelGroup receivedLabelGroup = new LabelGroup(receivedLabels,remoteLGid);
                                                if(receivedLabelGroup.equals(imageLG)){ //equals method does not work when the size is not the same
                                                    LGtoBeAdded = null;
                                                    imageLG.setLabelGroupId(remoteLGid);    //update local label group id
                                                    break;  //exit this FOR loop
                                                }
                                                else {
                                                    LGtoBeAdded = imageLG;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (Exception e1){
                                                e1.printStackTrace();
                                            }
                                        }
                                    }

                                    if(LGtoBeAdded != null) {
                                        //if indeed this label need to be added to the remote database, add image label and then add image
                                        JsonArrayRequest addLabelGroupRequest = new JsonArrayRequest(Request.Method.GET, addLabelGroupUrl, null,
                                                (addLGResponse -> {
                                                    //add label group successfully, then add image
                                                    //FIXME: double check this labelgroup count id. ERROR: network failed
                                                    String addImageUrl = ADD_IMAGE.replace("labelgroupid",imageLG.getLabelGroupId()+"")
                                                            .replace("deviceid",device.getDeviceID())
                                                            .replace("imagepath",pathToDB)
                                                            .replace("detectedtext",textToDB);
                                                    int test = 0;   //for debug
                                                    JsonArrayRequest addImageRequest = new JsonArrayRequest(Request.Method.GET, addImageUrl, null,
                                                            ((addImageResponse -> {
                                                                //add image, no response
                                                                int size = response.length();       //for debug
                                                            })),new MyErrorResponse(serviceContext));
                                                    SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addImageRequest);

                                                }), new MyErrorResponse(serviceContext));
                                        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addLabelGroupRequest);
                                    }
                                    else{
                                        //the labelGroup already exist in remote database, add image directly
                                        String addImageUrl = ADD_IMAGE.replace("labelgroupid",imageLG.getLabelGroupId()+"")
                                                .replace("deviceid",device.getDeviceID())
                                                .replace("imagepath",pathToDB)
                                                .replace("detectedtext",textToDB);
                                        JsonArrayRequest addImageRequest = new JsonArrayRequest(Request.Method.GET, addImageUrl, null,
                                                ((addImageResponse -> {
                                                    //add image, no response
                                                    int size = 0;   //for debug
                                                })),new MyErrorResponse(serviceContext));
                                        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addImageRequest);
                                    }
                                }),new MyErrorResponse(serviceContext));
                        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findLabelGroupRequest);

                    }),new MyErrorResponse(serviceContext));
            SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findLabelsRequest);
        }
    }

    /**
     * add a label to the remote database table: AllLabels
     * @param label
     */
    public void addLabel(Label label)
    {
        //first check if remote database has the same label or not (find based on label name)
        String findLabelUrl = FIND_LABELS.replace("labelnamea",label.getLabelName().replaceAll(" ",REPLACE_SPACE));
        String addLabelUrl = ADD_LABEL.replace("newlabelname",label.getLabelName().replaceAll(" ",REPLACE_SPACE));
        JsonArrayRequest findLabelRequest = new JsonArrayRequest(Request.Method.GET, findLabelUrl, null,
                ((response)->{
                    if(response==null||response.length()==0){
                        //if not found, add this label to remote database
                        int size = response.length();   //test code
                        //start adding label request
                        JsonArrayRequest addLabelRequest = new JsonArrayRequest(Request.Method.GET,addLabelUrl,null,
                                ((response1)->{
                            int size1 = response1.length();
                        }),new MyErrorResponse(serviceContext));
                        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addLabelRequest);
                    }
                }),
                new MyErrorResponse(serviceContext));
        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findLabelRequest);
//        requestQueue.add(findLabelRequest);
    }

    /**
     * add a labelGroup to the remote database table: LabelGroup
     * @param labelGroup
     */
    public String addLabelGroup(LabelGroup labelGroup)
    {
        //first, find out all the labels in this LabelGroup (search labels by label name)
        //if the return array length is smaller than local labelGroup, find out them and add them to remote database
        //else if all the labels are in remote database, check if this combination is exist or not (sequence also matter!)
        //if not exist, add new label group to remote database.

        ArrayList<Label> labels = labelGroup.getLabels();
        char[] varIdentifier = {'a','b','c','d','e'};   //this array is for passing parameters to url
        String urlBuilder1 = FIND_LABELS;       //url builder for finding labels
        String urlBuilder2 = ADD_LABELGROUP;    //url builder for adding labelGroup
        String urlBuilder3 = FIND_LABELGROUP;   //url builder for finding a labelGroup in remote database given a

        labelGroupCount++;  //update labelGroupCount
        labelGroup.setLabelGroupId(labelGroupCount);
        //to add a label group, the labelgroup id needed to be specified (for later update image)
        urlBuilder2 = urlBuilder2.replace("labelgroupid",labelGroupCount+"");

        for(int i= 0;i<labels.size();i++){
            urlBuilder1 = urlBuilder1.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
            urlBuilder2 = urlBuilder2.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
            urlBuilder3 = urlBuilder3.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
        }
        String findLabelsUrl = urlBuilder1;      //url for find image labels
        String addLabelGroupUrl = urlBuilder2;      //url for add labelGroup
        String findLabelGroupUrl = urlBuilder3;     //url for finding labelGroup given labels' name
        JsonArrayRequest findLabelsRequest = new JsonArrayRequest(Request.Method.GET, findLabelsUrl, null,
                (response -> {
                    //check the return JsonArray size
                    ArrayList<Label> labelsTobeAdded = new ArrayList<>();       //the list of labels need to be added to the remote database
                    if(response==null||response.length()==0){
                        //none of the labels exist in remote database, add them all to the database
//                        for(int i = 0;i<labels.size();i++){
//                            addLabel(labels.get(i));        //add service do not need to process data, no problem of multi thread
//                        }
                        labelsTobeAdded = labels;

                    }
                    else if(response.length()<labels.size()){
                        //part of the labels has been added to the table AllLabels, find out new labels
                        //process received data

                        for(int i = 0;i<response.length();i++){
                            try {
                                JSONObject jsonObject = (JSONObject)response.get(i);
                                String labelName = jsonObject.getString("labelName");
//                                boolean isExist = false;
                                for(int j = 0;j<labels.size();j++){
                                    if(!labelName.equalsIgnoreCase(labels.get(j).getLabelName())){
                                        // if not found
                                        labelsTobeAdded.add(new Label(labels.get(j).getLabelName()));
                                    }
                                }
//                                if(!isExist){   //if this label does not exist in remote database
//                                    labelsTobeAdded.add(new Label(labelName));
//                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //add new labels to remote database
                    for(int k = 0; k<labelsTobeAdded.size(); k++){
                        addLabel(labelsTobeAdded.get(k));        //add service do not need to process data, no problem of multi thread
                    }

                    //all labels has been added to the remote database, then check if this combination has already been added
                    //TODO: implementing request remote database and find label group
                    //FIXME: delay needed?
                    JsonArrayRequest findLabelGroupRequest = new JsonArrayRequest(Request.Method.GET,findLabelGroupUrl,null,
                            ((findLGResponse) -> {
                                LabelGroup LGtoBeAdded = null;      //the label group to be added to the remote database
                                if(findLGResponse==null||findLGResponse.length()==0){
                                    //if not existing in remote database, add new labelGroup to remote database
                                    LGtoBeAdded = labelGroup;
//                                    JsonArrayRequest addLabelGroupRequest = new JsonArrayRequest(Request.Method.GET, addLabelGroupUrl, null,
//                                            (addLGResponse -> {
//                                                int i = addLGResponse.length();
//                                                //add data, do nothing
//                                            }),new MyErrorResponse(serviceContext));
//                                    SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addLabelGroupRequest);
                                }
                                else{ //double check received data
                                    for(int i = 0;i<findLGResponse.length();i++) {
                                        try {
                                            MyJSONObject myJSONObject = new MyJSONObject((JSONObject) findLGResponse.get(i));
                                            String label1 = myJSONObject.getString("label1"); //return null if str = "null"
                                            String label2 = myJSONObject.getString("label2");
                                            String label3 = myJSONObject.getString("label3");
                                            String label4 = myJSONObject.getString("label4");
                                            int remoteLGid = myJSONObject.getInt("imageLabelID");
                                            ArrayList<Label> receivedLabels = new ArrayList<>(4);
                                            receivedLabels.add(new Label(label1));
                                            receivedLabels.add(new Label(label2));
                                            receivedLabels.add(new Label(label3));
                                            receivedLabels.add(new Label(label4));

                                            //FIXME: equals method  TEST PASSED!!!-2019-5-17-14:23
                                            LabelGroup receivedLabelGroup = new LabelGroup(receivedLabels,remoteLGid);
                                            if(receivedLabelGroup.equals(labelGroup)){
                                                LGtoBeAdded = null;
                                                break;  //exit for loop
                                            }
                                            else {
                                                LGtoBeAdded = labelGroup;
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (Exception e1){
                                            e1.printStackTrace();
                                        }
                                    }
                                }

                                //if indeed this label need to be added to the remote database
                                if(LGtoBeAdded!=null) {
                                    JsonArrayRequest addLabelGroupRequest = new JsonArrayRequest(Request.Method.GET, addLabelGroupUrl, null,
                                            (addLGResponse -> {
                                                int i = addLGResponse.length();
                                                //add data, do nothing
                                            }), new MyErrorResponse(serviceContext));
                                    SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addLabelGroupRequest);
                                }

                            }),new MyErrorResponse(serviceContext));
                    SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findLabelGroupRequest);

                }),new MyErrorResponse(serviceContext));
        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findLabelsRequest);

        return findLabelsUrl;
    }

    /**
     * update information stored in remote database
     * @param newImage
     */
    public void updateImageInfo(ImageInfo newImage)
    {
        //the fastest method: delete the row data and add a new row
        //to delete a row, we need to know the primary key of this row(imageID), this is accomplished by finding an image by path
        String pathToDB = newImage.getPath().replaceAll("/", REPLACE_SLASHL);
        String findImageUrl = FINDIMAEG_BY_PATH.replace("imagepath",pathToDB).replace("deviceid",thisDevice.getDeviceID());

        JsonArrayRequest findImageRequest = new JsonArrayRequest(Request.Method.GET, findImageUrl, null,
                ((response) -> {
                    //if this image is exist, delete and add new image
                    ArrayList<Integer> deleteList = new ArrayList<>();      //the list of images to be deleted(to avoid duplication)
                    for(int i = 0;i<response.length();i++){
                        try {
                            JSONObject jsonObject = (JSONObject)response.get(i);
                            int deleteId = jsonObject.getInt("imageID");
                            deleteList.add(deleteId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e1){
                            e1.printStackTrace();
                        }
                    }
                    for(Integer id: deleteList){
                        deleteImage(id);        //delete this row of image in remote database
                    }

                    //add new image to database
                    addNewImage(newImage);
                }),new MyErrorResponse(serviceContext));
        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findImageRequest);
    }

    /**
     * search images in remote database by OCR detected text
     * @param text
     */
    public void searchByText(String text)
    {
        text = text.replaceAll(KEY_space,REPLACE_SPACE);
        String searchUrl = SEARCHIMAGE_BYTEXT.replace("deviceid",thisDevice.getDeviceID()).replace("inputtext",text);
//        String searchUrl = FINDPATH_BY_TEXT.replace("detectedtext",text);
        JsonArrayRequest searchPathRequest = new JsonArrayRequest(Request.Method.GET, searchUrl, null,
                        ((response) -> {
                            //process data
                            ArrayList<ImageInfo> resultList = processReturnedImages(response);
                            //show search result (start a new activity)
                            if(resultList.size()==0){
                                //not found, show no result Toast
                                Toast.makeText(serviceContext,"Image not found!",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                showResult(resultList);
                            }
                        }), new MyErrorResponse(serviceContext));
//        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(searchPathRequest);
        requestQueue.add(searchPathRequest);
    }

    /**
     * show images that match user input based on the path from imageList
     * start a new activity
     * @param imageList
     */
    private void showResult(ArrayList<ImageInfo> imageList) {
        Intent intent = new Intent(serviceContext, SearchResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("DATA",imageList);
        intent.putExtras(bundle);           //use put extras rather than extra to store bundle
        serviceContext.startActivity(intent);       //start new activity in a non-activity class: succeed
    }

    /**
     * request service, get the current counter of LabelGroup in remote database
     * then update local labelgroupcount
     *
     */
    public void updateLabelGroupCount()
    {
        String getCountUrl = "https://studev.groept.be/api/a18_sd602/getLabelGroupCunter";
        JsonArrayRequest getCountRequest =
                new JsonArrayRequest(Request.Method.GET,
                        getCountUrl,
                        null,
                        ((response)->{
                            try{
                                JSONObject jsonObject = (JSONObject)response.get(0);
                                labelGroupCount = jsonObject.getInt("counter");
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }),
                        new MyErrorResponse(serviceContext));
//        RequestQueue requestQueue = Volley.newRequestQueue(serviceContext);
        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(getCountRequest);
    }

    /**
     * delete an image row in remote database
     * @param imageId: the primary key of the image in database
     */
    private void deleteImage(int imageId)
    {
        String deleteUrl = DELETE_IMAGE.replace("imageid",imageId+"");
        JsonArrayRequest deleteRequest = new JsonArrayRequest(Request.Method.GET,deleteUrl,null,
                ((response) -> {
                    //image deleted, no operation
                }), new MyErrorResponse(serviceContext));
        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(deleteRequest);
    }

    /**
     * delete an image in remote database based on given real path (format: / /  / )
     * initially used for delete images added to database by mistake
     * @param realPath
     */
    public void deleteImageInDB(String realPath)
    {
        String pathToDB = realPath.replaceAll("/","|");
        String findImageUrl = FINDIMAEG_BY_PATH.replace(KEY_deviceid,thisDevice.getDeviceID()).replace(KEY_imagepath,pathToDB);
        JsonArrayRequest findImageRequest = new JsonArrayRequest(Request.Method.GET,findImageUrl,null,
                (response -> {
                    ArrayList<Integer> deleteList = new ArrayList<>(100);
                    for(int i = 0;i<response.length();i++){
                        try {
                            JSONObject jsonObject = (JSONObject) response.get(i);
                            int imageId = jsonObject.getInt("imageID");
                            deleteList.add(imageId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //delete the whole list
                    for(Integer id:deleteList){
                        deleteImage(id);
                    }
                }),new MyErrorResponse(serviceContext));
        requestQueue.add(findImageRequest);
    }

    /**
     * a method to test which char could be added to the remote database
     * @param testStr
     */
    public void testAddString(String testStr)
    {
//        String serviceUrl = ADD_IMAGE.replace("imagepath","testCharAddition")
//                .replace("detectedtext",testStr).replace("deviceid",3+"");
        String serviceUrl = ADDIMAGE_NOLABEL.replace("path","additiongroup2");
        serviceUrl = serviceUrl.replace("deviceid","3");
        serviceUrl = serviceUrl.replace("detectedtext",testStr);
        JsonArrayRequest testRequest = new JsonArrayRequest(Request.Method.GET,serviceUrl,null,
                (response -> {
                    //no response
                }),new MyErrorResponse(serviceContext));

        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(testRequest);
    }

    /**
     * a method to test adding Chinese character to remote database
     * TEST passed: Chinese character will be decoded as unicode, although it may show strange result in MySQL workbench
     * you can still use query to search Chinese
     */
    public void testGetChineseChar(ImageInfo image)
    {
        String pathToDB = image.getPath().replaceAll("/", REPLACE_SLASHL);

        String findImageUrl = FINDIMAEG_BY_PATH.replace("deviceid",thisDevice.getDeviceID()).replace("imagepath",pathToDB);
        JsonArrayRequest getImageRequest = new JsonArrayRequest(Request.Method.GET,findImageUrl,null,
                (response -> {
                    ArrayList<String> receivedStrs = new ArrayList<>();
                    for(int i = 0;i<response.length();i++)
                    {
                        try {
                            JSONObject jsonObject = (JSONObject) response.get(i);
                            String detected = jsonObject.getString("detectedText");
                            receivedStrs.add(detected);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }),new MyErrorResponse(serviceContext));
        requestQueue.add(getImageRequest);     //test if it is possible to use requestQueue to request: use requestQueue to add request is totally fine
//        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(getImageRequest);
    }

    /**
     * update only the labels of an image
     * @param image
     */
    public void updateImageLG(ImageInfo image)
    {
        //first find the image in remote database, get the image id

        String pathToDB = image.getPath().replaceAll("/", REPLACE_SLASHL);
        String findImageUrl = FINDIMAEG_BY_PATH.replace("deviceid",thisDevice.getDeviceID()).replace("imagepath",pathToDB);
        JsonArrayRequest findImageRequest = new JsonArrayRequest(Request.Method.GET, findImageUrl, null,
                (response -> {
                    //check if this image exist in remote database or not
                    if(response==null||response.length()==0){   //not found in remote database, add it
                        if(image.getDetectedText()==null) {
                            image.setDetectedText("");  //if null, just set an empty string, to prevent null pointer exception
                        }
                        addNewImage(image);
                    }
                    else{   //image found: update image
                        try {
                            JSONObject jsonObject = (JSONObject) response.get(0);
                            int remoteImageID = jsonObject.getInt("imageID");
                            image.setRemoteID(remoteImageID);

                            //update label groupid of this image
                            LabelGroup imageLG = image.getLabelGroup();
                            ArrayList<Label> labels = imageLG.getLabels();
                            char[] varIdentifier = {'a','b','c','d','e','f','g','h'};   //this array is for passing parameters to url
//                            String urlBuilder2 = ADD_LABELGROUP;    //url builder for adding labelGroup
                            String urlBuilder3 = FIND_LABELGROUP;   //url builder for finding a labelGroup in remote database given a

                            try {
                                for(int i= 0;i<labels.size();i++){
//                                urlBuilder2 = urlBuilder2.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
                                    urlBuilder3 = urlBuilder3.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(KEY_space,REPLACE_SPACE));
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

//                            String addLabelGroupUrl = urlBuilder2;      //url to add labelGroup (this will be used only when the label group does not exist in remote database)
                            String findLabelGroupUrl = urlBuilder3;     //url to find labelGroup given labels' name


                            JsonArrayRequest findLabelGroupRequest = new JsonArrayRequest(Request.Method.GET,findLabelGroupUrl,null,
                                    ((findLGResponse) -> {
                                        LabelGroup LGtoBeAdded = null;      //the label group to be added to the remote database
                                        if(findLGResponse==null||findLGResponse.length()==0){
                                            //if not existing in remote database, add new labelGroup to remote database
                                            LGtoBeAdded = imageLG;
                                        }
                                        else{ //double check received data
                                            for(int i = 0;i<findLGResponse.length();i++) {
                                                try {
                                                    MyJSONObject myJSONObject = new MyJSONObject((JSONObject) findLGResponse.get(i));
                                                    String label1 = myJSONObject.getString("label1"); //return null if str = "null"
                                                    String label2 = myJSONObject.getString("label2");
                                                    String label3 = myJSONObject.getString("label3");
                                                    String label4 = myJSONObject.getString("label4");
                                                    int remoteLGid = myJSONObject.getInt("imageLabelID");
                                                    ArrayList<Label> receivedLabels = new ArrayList<>(4);
                                                    if(label1!=null){       //avoid size problem
                                                        receivedLabels.add(new Label(label1));
                                                        if(label2!=null){
                                                            receivedLabels.add(new Label(label2));
                                                            if(label3!=null){
                                                                receivedLabels.add(new Label(label3));
                                                                if(label4!=null){
                                                                    receivedLabels.add(new Label(label4));
                                                                }
                                                            }
                                                        }
                                                    }

                                                    //FIXME: equals method  TEST PASSED!!!-2019-5-17-14:23 :( not yet
                                                    LabelGroup receivedLabelGroup = new LabelGroup(receivedLabels,remoteLGid);
                                                    if(receivedLabelGroup.equals(imageLG)){ //equals method does not work when the size is not the same
                                                        LGtoBeAdded = null;
                                                        imageLG.setLabelGroupId(remoteLGid);    //update local label group id
                                                        break;  //exit this FOR loop
                                                    }
                                                    else {
                                                        LGtoBeAdded = imageLG;
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                } catch (Exception e1){
                                                    e1.printStackTrace();
                                                }
                                            }
                                        }

                                        if(LGtoBeAdded != null) {
                                            //if indeed this label need to be added to the remote database, add image label and then add image
                                            labelGroupCount++;  //update labelGroupCount
                                            imageLG.setLabelGroupId(labelGroupCount);
                                            //to add a label group, the labelgroup id needed to be specified (for later update image)
                                            String urlBuilder2 = ADD_LABELGROUP;    //url builder for adding labelGroup
                                            urlBuilder2 = urlBuilder2.replace("labelgroupid",labelGroupCount+"");
                                            for(int i= 0;i<labels.size();i++){
                                                urlBuilder2 = urlBuilder2.replace("labelname"+varIdentifier[i],labels.get(i).getLabelName().replace(" ",REPLACE_SPACE));
                                            }
                                            String addLabelGroupUrl = urlBuilder2;      //the url to add a label group in remote database

                                            JsonArrayRequest addLabelGroupRequest = new JsonArrayRequest(Request.Method.GET, addLabelGroupUrl, null,
                                                    (addLGResponse -> {
                                                        //add label group successfully, then update image

                                                        String updateImageLGUrl = UPDATE_IMAGELG.replace(KEY_labelgroupid,imageLG.getLabelGroupId()+"")
                                                                .replace(KEY_imageid,image.getRemoteID()+"");
                                                        JsonArrayRequest updateImageLGRequest = new JsonArrayRequest(Request.Method.GET, updateImageLGUrl, null,
                                                                ((updateImageLGResponse -> {
                                                                    //add image, no response
                                                                    int size = updateImageLGResponse.length();       //for debug
                                                                })),new MyErrorResponse(serviceContext));
                                                        SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(updateImageLGRequest);

                                                    }), new MyErrorResponse(serviceContext));
                                            SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(addLabelGroupRequest);
                                        }
                                        else{
                                            //the labelGroup already exist in remote database, update image directly
                                            String updateImageLGUrl = UPDATE_IMAGELG.replace(KEY_labelgroupid,imageLG.getLabelGroupId()+"")
                                                    .replace(KEY_imageid,image.getRemoteID()+"");
                                            JsonArrayRequest updateImageLGRequest = new JsonArrayRequest(Request.Method.GET, updateImageLGUrl, null,
                                                    ((updateImageLGResponse -> {
                                                        //add image, no response
                                                        int size = updateImageLGResponse.length();       //for debug
                                                    })),new MyErrorResponse(serviceContext));
                                            requestQueue.add(updateImageLGRequest);
                                        }
                                    }),new MyErrorResponse(serviceContext));
                            SingletonRequestQueue.getInstance(serviceContext).addToRequestQueue(findLabelGroupRequest);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }),new MyErrorResponse(serviceContext));
        requestQueue.add(findImageRequest);     //add request
    }

    /**
     * search images by matching the first label and detectedText
     * @param userInput
     */
    public void searchByLabelText(String userInput)
    {
        userInput = userInput.replaceAll(KEY_space,REPLACE_SPACE);
        String searchImageUrl = SEARCHIMAGE_BYLABEL_TEXT.replace(KEY_deviceid,thisDevice.getDeviceID()).replace(KEY_userinput,userInput);
        JsonArrayRequest searchImageRequest = new JsonArrayRequest(Request.Method.GET, searchImageUrl, null,
                (response -> {
                    //process received data
                    ArrayList<ImageInfo> resultList = processReturnedImages(response);

                    //show search result (start a new activity)
                    if(resultList.size()==0){
                        //not found, show no result Toast
                        Toast.makeText(serviceContext,"Image not found!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        showResult(resultList);
                    }

                }), new MyErrorResponse(serviceContext));
        requestQueue.add(searchImageRequest);
    }

    /**
     * search image in remote database based on the first two labels of the image
     * @param userInput
     */
    public void searchByLabel(String userInput)
    {
        userInput = userInput.replaceAll(KEY_space,REPLACE_SPACE);
        String searchImageUrl = SEARCHIMAGE_BYLABEL.replace(KEY_deviceid,thisDevice.getDeviceID()).replaceAll(KEY_labelname,userInput);
        JsonArrayRequest searchImageRequest = new JsonArrayRequest(Request.Method.GET, searchImageUrl, null,
                (response -> {
                    //process received data
                    ArrayList<ImageInfo> resultList = processReturnedImages(response);
                    //show search result (start a new activity)
                    if(resultList.size()==0){
                        //not found, show no result Toast
                        Toast.makeText(serviceContext,"Image not found!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        showResult(resultList);
                    }

                }), new MyErrorResponse(serviceContext));
        requestQueue.add(searchImageRequest);
    }

    @NotNull
    private ArrayList<ImageInfo> processReturnedImages(JSONArray response) {
        ArrayList<ImageInfo> resultList = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject jsonObject = (JSONObject) response.get(i);
                String pathFromDB = jsonObject.getString("imagePath");

                String realPath = pathFromDB.replaceAll(REPLACE_SLASHL, "/");  //the real path of this image
                File tempImageFile = new File(realPath);
                if (tempImageFile.exists()) {    //check if this image exist in local storage (avoid unsychronization in some case)
                    ImageInfo tempImage = new ImageInfo(realPath);
                    resultList.add(tempImage);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return resultList;
    }
}
