package gallery;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import database.BaseDataType;
import database.ImagePath;
import database.LabelGroup;

/**
 * a class to store the image information, corresponding to database table: Images
 * Created by Xianghui Xie 10/5/2019
 */
public class ImageInfo extends BaseDataType implements Parcelable {

    private String name;                 // image name
    private String path;                 // image path
    private long time;                   // the time when image is added
    private String detectedText;         //OCR analyze result
    private LabelGroup labels;
    private int labelGroupID;
    private ImagePath imagePath;        //the path and primary key of this imageView in remote database, imageView id is wrapped inside the path
    private int remoteID;               //the image id in remote database


    public ImageInfo(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
        detectedText = null;        //by default: null
        labels = null;              //by default: null
        imagePath = new ImagePath(path);
        remoteID = -1;              //by default: -1, not synchronized with remote database
    }

    public ImageInfo(String detectedText, LabelGroup labels, ImagePath imagePath) {
        this.detectedText = detectedText;
        this.labels = labels;
        this.imagePath = imagePath;
        remoteID = -1;              //by default: -1, not synchronized with remote database
    }

    /**
     * this constructor is for processing data from remote database
     * @param path
     */
    public ImageInfo(String path) {
        this.path = path;
        this.name = null;
        this.time = -1;
        detectedText = null;        //by default: null
        labels = null;              //by default: null
        imagePath = new ImagePath(path);
        remoteID = -1;              //by default: -1, not synchronized with remote database
    }

    protected ImageInfo(Parcel in) {
        name = in.readString();
        path = in.readString();
        time = in.readLong();
        detectedText = in.readString();
        labelGroupID = in.readInt();
        remoteID = -1;              //by default: -1, not synchronized with remote database
    }

    public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        @Override
        public ImageInfo createFromParcel(Parcel in) {
            return new ImageInfo(in);
        }

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getPath() {
//        return imagePath.getPath();
        if(imagePath==null){
            return path;
        }
        return imagePath.getPath();

    }

    public int getImageID()
    {
        return imagePath.getPathID();
    }

    public LabelGroup getLabelGroup() {
        return labels;
    }
    public void setImageID(int id)
    {
        if(imagePath!=null){
            imagePath.setPathID(id);
        }
    }

    public void setLabels(LabelGroup labels) {
        this.labels = labels;
    }

    public long getTime() {
        return time;
    }

    public String getDetectedText() {
        return detectedText;
    }

    public void setDetectedText(String detectedText) {
        this.detectedText = detectedText;
    }

    public int getRemoteID() {
        return remoteID;
    }

    public void setRemoteID(int remoteID) {
        this.remoteID = remoteID;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", time=" + time +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageInfo imageInfo = (ImageInfo) o;
        return Objects.equals(path, imageInfo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeLong(time);
        dest.writeString(detectedText);
        dest.writeInt(labelGroupID);
    }
}
/*
 *   ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 *     ┃　　　┃
 *     ┃　　　┃
 *     ┃　　　┗━━━┓
 *     ┃　　　　　　　┣┓
 *     ┃　　　　　　　┏┛
 *     ┗┓┓┏━┳┓┏┛
 *       ┃┫┫　┃┫┫
 *       ┗┻┛　┗┻┛
 *        May god beast bless,
 *        NO BUG!
 */