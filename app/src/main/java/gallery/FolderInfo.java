package gallery;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import database.BaseDataType;

/**
 * information of image folder
 * Created by Xianghui on 10/05/2019
 */
public class FolderInfo extends BaseDataType implements Parcelable {

    private String folderName;                         // 文件夹名称
    private String folderPath;                         // 文件夹路径
    private ImageInfo firstImage;                       // 文件夹中第一张图片的信息
    private List<ImageInfo> allImages;                  // 文件夹中的图片集合
    private int imageCount;                             //total number of images in this folder

    public FolderInfo(String name, String path) {
        this.folderName = name;
        this.folderPath = path;
        allImages = new ArrayList<>();
        firstImage = null;
        imageCount = 0;     //initialize imageView count to zero
    }

    public FolderInfo() {
        folderName = null;
        allImages = new ArrayList<>();
        folderPath = null;
        firstImage = null;
        imageCount = 0 ;
    }

    protected FolderInfo(Parcel in) {
        folderName = in.readString();
        folderPath = in.readString();
        firstImage = in.readParcelable(ImageInfo.class.getClassLoader());
        allImages = in.createTypedArrayList(ImageInfo.CREATOR);
        imageCount = in.readInt();
    }

    public static final Creator<FolderInfo> CREATOR = new Creator<FolderInfo>() {
        @Override
        public FolderInfo createFromParcel(Parcel in) {
            return new FolderInfo(in);
        }

        @Override
        public FolderInfo[] newArray(int size) {
            return new FolderInfo[size];
        }
    };

    /**
     * add an imageView to this folder list
     * @param image
     */
    public void addImage(ImageInfo image)
    {
        if(image!=null){
            allImages.add(image);
            imageCount++;
        }
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public ImageInfo getFirstImage() {
        return firstImage;
    }

    public List<ImageInfo> getAllImages() {
        return allImages;
    }

    public void setFirstImage(ImageInfo firstImage) {
        this.firstImage = firstImage;
    }

    public int getImageCount() {
        return imageCount;
    }

    @Override
    public boolean equals(Object object) {
        try {
            FolderInfo other = (FolderInfo) object;
            return this.folderPath.equalsIgnoreCase(other.folderPath);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(object);
    }

    @Override
    public String toString() {
        return "FolderInfo{" +
                "name='" + folderName + '\'' +
                ", path='" + folderPath + '\'' +
                ", firstImage=" + firstImage +
                ", photoInfoList=" + allImages +
                '}';
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public void setAllImages(List<ImageInfo> allImages) {
        this.allImages = allImages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(allImages.toArray());
        dest.writeString(folderName);
        dest.writeString(folderPath);
        dest.writeParcelable(firstImage,flags);
        dest.writeInt(imageCount);

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
 *        may god beast bless
 *        NO BUG!
 */