package com.matb.memorygame;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ImagesAdapter extends BaseAdapter
{
    private Context context;
    private List<Integer> imageIds;
    private List<Integer> disabledImages;

    ImagesAdapter(Context context, List<Integer> imageIds)
    {
        this.context = context;
        this.imageIds = imageIds;
        this.disabledImages = new ArrayList<>();
    }

    public void setDisabledImages(List<Integer> disabledImages)
    {
        this.disabledImages = disabledImages;
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
    public View getView(final int i, View view, ViewGroup viewGroup)
    {
        ImageView imageView;
        if (view == null)
        {
            // If the view is not recycled, create a new one
            imageView = new ImageView(this.context);
        }
        else
        {
            imageView = (ImageView) view;
        }

        Integer imageId = this.imageIds.get(i);
        imageView.setImageResource(imageId);

        if (disabledImages.contains(imageId))
        {
            imageView.setVisibility(View.GONE);
        }

        return imageView;
    }
}
