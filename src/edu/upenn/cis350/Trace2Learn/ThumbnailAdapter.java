package edu.upenn.cis350.Trace2Learn;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

//Adapter class for a list of bitmap images
public class ThumbnailAdapter extends BaseAdapter {
    private Context mContext;

    private ArrayList<Bitmap> images;

    public ThumbnailAdapter(Context c, ArrayList<Bitmap> images) {
    	mContext = c;
        this.images = images;
    }

    //returns the number of images in the list
    public int getCount() {
        return images.size();
    }

    //returns the bitmap at position
    public Object getItem(int position) {
        return images.get(position);
    }

    //get the position of the item
    public long getItemId(int position) {
        return position;
    }
    
    //update the list of bitmaps with images
    public void update(ArrayList<Bitmap> images){
    	this.images = images;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);

        imageView.setImageBitmap(images.get(position));
        imageView.setLayoutParams(new Gallery.LayoutParams(64, 64));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setBackgroundColor(Color.LTGRAY);
        return imageView;
    }
}
