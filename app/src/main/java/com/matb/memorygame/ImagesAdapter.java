package com.matb.memorygame;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import java.util.List;


public class ImagesAdapter extends BaseAdapter
{
    private static String LOG_TAG = ImagesAdapter.class.getSimpleName();

    private Context context;
    private List<Integer> imageIds;
    private List<Integer> selectedPositions;
    private List<Integer> disabledImages;

    ImagesAdapter(Context context, List<Integer> imageIds, List<Integer> selectedPositions, List<Integer> disabledImages)
    {
        this.context = context;
        this.imageIds = imageIds;
        this.selectedPositions = selectedPositions;
        this.disabledImages = disabledImages;
    }

    public void addDisabledImages(Integer disabledImages)
    {
        this.disabledImages.add(disabledImages);
    }

    public void setImageIds(List<Integer> imageIds)
    {
        this.imageIds = imageIds;
    }

    public void setDisabledImages(List<Integer> disabledImages)
    {
        this.disabledImages = disabledImages;
    }

    public void setSelectedPositions(List<Integer> selectedPositions)
    {
        this.selectedPositions = selectedPositions;
    }

    @Override
    public int getCount()
    {
        return this.imageIds.size();
    }

    @Override
    public Object getItem(int i)
    {
        return null;
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup)
    {
        View view;
        if (convertView == null)
        {
            // If the view is not recycled, create a new one
            view = LayoutInflater.from(this.context).inflate(
                    R.layout.grid_item, viewGroup, false);
        }
        else
        {
            view = convertView;
        }

        ImageView imageView = view.findViewById(R.id.front);
        int imageId = imageIds.get(i);

        if (disabledImages.contains(imageId))
        {
            imageView.setImageResource(R.drawable.blank);
        }
        else
        {
            imageView.setImageResource(imageId);
        }

        // Show face or back of card depending if it has been selected
        ViewFlipper viewFlipper = view.findViewById(R.id.grid_item);
        viewFlipper.setInAnimation(null);
        viewFlipper.setOutAnimation(null);
        int childId = selectedPositions.contains(i) || disabledImages.contains(imageId) ? R.id.front : R.id.back;
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(viewFlipper.findViewById(childId)));

        return view;
    }
}
