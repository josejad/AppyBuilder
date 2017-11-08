// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.IOException;

@DesignerComponent(version = YaVersion.GALLERYVIEWER_COMPONENT_VERSION,
        description = "A component that shows items in a center-locked, horizontally scrolling list",
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class GalleryViewer extends AndroidViewComponent implements AdapterView.OnItemClickListener {
    private Gallery view;
    private GalleryImageAdapter adapter;
    private String TAG = "GalleryViewer";
    private YailList images;
    private int imagePad=5;
    private long selectedImageIndex=0;

    private int width = LENGTH_FILL_PARENT;
    /**
     * Creates a new AndroidViewComponent.
     *
     * @param container container, component will be placed in
     */
    public GalleryViewer(ComponentContainer container) {
        super(container);
        view = new Gallery(container.$context());
        view.setSpacing(imagePad);
        adapter = new GalleryImageAdapter(container.$context());
        view.setOnItemClickListener(this);

        container.$add(this);
        Width(this.width);
    }

    /**
     * Adds a series of images to the gallery
     * @param images list of images
     */
    @SimpleProperty
    public void Images(YailList images) {
        //convert from YailList to string array and
        this.images = images;
        adapter.setImages(images.toStringArray());
        view.setAdapter(adapter);

        view.requestLayout();
    }

    //todo: read this: http://stackoverflow.com/questions/10792258/android-scrolling-background-in-gallery
    //todo: It would be good idea to allow user specify a folder to automatically load all files (with some extension
    //todo:     regex (e.g. *.png) so that we load all those images.

    /**
     * Used to specify number of spaces between images
     *
     * @param spaces
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "5")
    @SimpleProperty
    public void Padding(int spaces) {
        this.imagePad = spaces;
        view.setSpacing(spaces);
    }

    /**
     * Returns number of padding spaces between images
     *
     */
    @SimpleProperty
    public int Padding() {
        return this.imagePad;
    }

//    /**
//     *
//     * @param itemstring
//     */
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Option 1,Option 2,Option 3")
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public void ImagesFromString(String itemstring) {
//        if (itemstring.length() == 0) {
//            this.images = new YailList();
//        } else {
//            this.images = YailList.makeList((Object[]) itemstring.split(" *, *"));
//        }
//
//        //Now make it display in the spinner button
//        view.setAdapter(adapter);
//        view.requestLayout();
//    }

    // NOTE: Don't set width height of adapter because it will set the widht/height of each actual image not the gallery itself
    @SimpleProperty
    public void ThumbnailWidth(int width) {
        adapter.setWidth(width);
        view.setAdapter(adapter);
        view.requestLayout();
    }

    @SimpleProperty
    public int ThumbnailWidth() {
        return adapter.getWidth();
    }

    @SimpleProperty
    public void ThumbnailHeight(int height) {
        adapter.setHeight(height);
        view.setAdapter(adapter);
        view.requestLayout();
    }

    @SimpleProperty
    public int ThumbnailHeight() {
        return adapter.getHeight();
    }


    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public void Selection(String value) {
        value = value.toLowerCase();
        YailList images = Images();
//        for (int i=0; i< images.size(); i++) {  // use this if new YailList (array) is used
        for (int i=0; i< images.length(); i++) {
            if (value.equals(images.getString(i).toLowerCase())) {
                view.setSelection(i);
                selectedImageIndex=i;
                break;
            }
        }

    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String Selection() {
        return images.getString((int) selectedImageIndex);
    }

    /**
     * Elements property getter method
     *
     * @return a YailList representing the list of images
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList Images() {
//        String[] images = adapter.getImages();
//        return YailList.makeList(images);
        return this.images;

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
//        EventDispatcher.dispatchEvent(this, "Click", images.getString(imageIndex));
//        AfterPicking(images.getString(imageIndex));
        selectedImageIndex = index;
        AfterPicking(images.getString(index));
    }

    /**
     * todo: blah blah blah
     */
    @SimpleEvent(description = "Triggered after an image is selected. It will also report selected image name")
    public void AfterPicking(String imageName) {
        EventDispatcher.dispatchEvent(this, "AfterPicking", imageName);
    }
//    @Override
//    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
////        EventDispatcher.dispatchEvent(this, "Click", images.getString(imageIndex));
//        AfterPicking(images.getString(index));
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> adapterView) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }


    private class GalleryImageAdapter extends BaseAdapter {
        private Context mContext;

        private String[] images = null;
        private int width = 150;
        private int height = 120;
        private int imageBackground;

        public GalleryImageAdapter(Context context) {
            mContext = context;

            imageBackground = container.$context().getResources().getIdentifier("ImageGallery_android_galleryItemBackground", "styleable", container.$context().getPackageName());
//            TypedArray ta = container.$context().obtainStyledAttributes(resid);
//
//            mGalleryItemBackground = ta.getResourceId(secondres, 1);
//            a.recycle();
//
//            TypedArray ta = context.obtainStyledAttributes(secondres, 1);

        }

        private void setWidth(int width) {
            this.width = width;
        }

        private int getWidth() {
            return this.width;
        }
        private void setHeight(int height) {
            this.height = height;
        }

        private int getHeight() {
            return this.height;
        }

        public int getCount() {
            return images.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public void setImages(String[] images) {
            this.images = images;
        }

        public String[] getImages() {
            return this.images;
        }
        // Override this method according to your need
        public View getView(int index, View view, ViewGroup viewGroup) {
            ImageView iv = new ImageView(mContext);

            try {
                iv.setImageDrawable(MediaUtil.getBitmapDrawable(container.$form(), images[index]));
                iv.setLayoutParams(new Gallery.LayoutParams(width, height));
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                iv.setBackgroundResource(imageBackground);

            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }


            return iv;
        }
    }

}
