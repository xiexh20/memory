package database;

import java.util.Objects;

/**
 * local data type corresponding to database table: AllLabels
 */
public class Label extends BaseDataType{
    private String labelID;
    private String labelName;

    public Label(String labelID, String labelName) {
        this.labelID = labelID;
        this.labelName = labelName;
    }

    public Label(String labelName) {
        this.labelName = labelName;
        labelID = "-1";       // by default: -1, which means not synchoronized with remote database
    }

    public String getLabelID() {
        return labelID;
    }

    public String getLabelName() {
        return labelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        if(label.labelName==null&&(labelName!=null)) return false;
        if(labelName==null&&(label.labelName!=null)) return false;
        return labelName.equals(label.labelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labelName);
    }

    public void setLabelID(String newId)
    {
        labelID = newId;
    }
}
