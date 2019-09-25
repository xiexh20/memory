package gallery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import anlin.softdev.kuleuven.memories.R;

public class MyGridViewAdapter extends ArrayAdapter<ImageInfo> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<ImageInfo> data = new ArrayList<ImageInfo>();
    private List<ImageInfo> selectedImages;
    private int screenWidth;
    private int screenHeight;
    private boolean selectable;     //in result image activity, not selectable, in import activity, selectable
    private HashMap<Integer, Boolean> checkBoxsStatus;


    public MyGridViewAdapter(Context context, int layoutResourceId, ArrayList<ImageInfo> data, boolean selectable) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;

        screenWidth = ScreenUtils.getScreenWidth(context);
        screenHeight = ScreenUtils.getScreenHeight(context);
        this.selectable = selectable;
        selectedImages = new ArrayList<>(data.size());
        checkBoxsStatus = new HashMap<>();
        for(int i = 0;i<data.size();i++){
            //initialize data
            checkBoxsStatus.put(i,false);      //at first, all not checked
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder thisViewHolder;
        ImageInfo thisImage = data.get(position);

        //here set each cell of the gridView
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            thisViewHolder = new ViewHolder();
            thisViewHolder.imageView = row.findViewById(R.id.resultItemImage);
            thisViewHolder.checkBox = row.findViewById(R.id.imageCheckBox);
            if(selectable){
                thisViewHolder.checkBox.setVisibility(View.VISIBLE);        //visible for choose image to import activity
                thisViewHolder.checkBox.setChecked(checkBoxsStatus.get(position));
                thisViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //when an item is selected
                         if(selectedImages.contains(thisImage)){
                            selectedImages.remove(thisImage);
                            checkBoxsStatus.put(position,false);
                            }
                         else{
                            selectedImages.add(thisImage);
                            checkBoxsStatus.put(position,true);
                         }
                         thisViewHolder.checkBox.setChecked(checkBoxsStatus.get(position));
                    }
                });
                thisViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(selectedImages.contains(thisImage)){
                            selectedImages.remove(thisImage);
                            checkBoxsStatus.put(position,false);
                        }
                        else{
                            selectedImages.add(thisImage);
                            checkBoxsStatus.put(position,true);
                        }
                        thisViewHolder.checkBox.setChecked(checkBoxsStatus.get(position));
                    }
                });
            }
            else {
                thisViewHolder.checkBox.setVisibility(View.INVISIBLE);      //not visible for show result images
            }

           //set the width and height of the imageView to fit phone screen
            ViewGroup.LayoutParams params = thisViewHolder.imageView.getLayoutParams();
            params.width = screenWidth/3;       //set image view size
            params.height = screenHeight/4 ;
            thisViewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thisViewHolder.imageView.setLayoutParams(params);

            row.setTag(thisViewHolder);
        } else {
            thisViewHolder = (ViewHolder) row.getTag();
            thisViewHolder.checkBox.setChecked(checkBoxsStatus.get(position));     //otherwise just update the view
        }

        //use glide to load images: faster
        Glide
            .with(context)
            .load(thisImage.getPath())
            .into(thisViewHolder.imageView);

        return row;
    }

    static class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
    }


    public void setItemSelectable(boolean selectable)
    {
        this.selectable = selectable;
    }

    public List<ImageInfo> getSelectedImages() {
        return selectedImages;
    }

    public void setSelectedImages(List<ImageInfo> selectedImages) {
        this.selectedImages = selectedImages;
    }

    public ArrayList<ImageInfo> getAllImages() {
        return data;
    }

    public void setItemChecked(int position, boolean status)
    {
        checkBoxsStatus.put(position,status);
    }
}
