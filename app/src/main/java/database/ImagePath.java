package database;

import java.util.Objects;

public class ImagePath extends BaseDataType {
    private String path;
    private int pathID;

    public ImagePath(String path, int id) {
        this.pathID = id;
        this.path = path;
    }

    public ImagePath(String path)
    {
        this.path = path;
        pathID = -1;        //-1 means this id has not been synchronized with remote database
    }

    public String getPath() {
        return path;
    }

    public int getPathID() {
        return pathID;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPathID(int pathID) {
        this.pathID = pathID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImagePath imagePath = (ImagePath) o;
        return pathID == imagePath.pathID &&
                path.equals(imagePath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, pathID);
    }
}
