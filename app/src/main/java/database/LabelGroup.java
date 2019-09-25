package database;

import java.util.ArrayList;
import java.util.Objects;

/**
 * local datatype corresponding to database tabel: LabelGroup
 */
public class LabelGroup extends BaseDataType{
    private ArrayList<Label> labels;
    private int labelGroupId ;

    public LabelGroup(ArrayList<Label> labels) {
        this.labels = labels;
        labelGroupId = -1;      //-1 means that this id is not updated with remote database
    }

    public LabelGroup(ArrayList<Label> labels, int labelGroupId) {
        this.labels = labels;
        this.labelGroupId = labelGroupId;
    }

    public LabelGroup() {
        labels = new ArrayList<>();
        labelGroupId = -1;       //-1 means that this id is not updated with remote database
    }

    public LabelGroup(int labelGroupId) {
        this.labelGroupId = labelGroupId;
        labels = new ArrayList<>();
    }

    public int getLabelCount()
    {
        return labels.size();
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public int getLabelGroupId() {
        return labelGroupId;
    }

    public void setLabelGroupId(int labelGroupId) {
        this.labelGroupId = labelGroupId;
    }

    public void addLabel(Label label){
        labels.add(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelGroup that = (LabelGroup) o;
//        return labelGroupId == that.labelGroupId &&
//                labels.equals(that.labels);
        return labels.equals(that.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, labelGroupId);
    }
}
