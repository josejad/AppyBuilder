//package com.google.appinventor.components.runtime;
//
//
//import android.content.Context;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.*;
//import android.widget.LinearLayout;
//import com.google.appinventor.components.annotations.*;
//import com.google.appinventor.components.common.ComponentCategory;
//import com.google.appinventor.components.common.PropertyTypeConstants;
//import com.google.appinventor.components.common.YaVersion;
//import com.google.appinventor.components.runtime.util.MediaUtil;
//import com.google.appinventor.components.runtime.util.YailList;
//
//import java.io.IOException;
//import java.util.List;
//
//@DesignerComponent(version = YaVersion.GALLERYVIEWER_COMPONENT_VERSION,
//        description = "blah blah blah: add doc",
//        category = ComponentCategory.USERINTERFACE)
//@SimpleObject
//public class StackView extends AndroidViewComponent implements AdapterView.OnItemClickListener {
//    private android.widget.StackView view;
//    private GalleryImageAdapter adapter;
//    private String TAG = "StackView";
//    private YailList images;
//    private int imagePad=5;
//    private long selectedImageIndex=0;
//
//    private int width = LENGTH_FILL_PARENT;
//    /**
//     * Creates a new AndroidViewComponent.
//     *
//     * @param container container, component will be placed in
//     */
//    public StackView(ComponentContainer container) {
//        super(container);
//        view = new android.widget.StackView(container.$context());
//
//        adapter = new GalleryImageAdapter(container.$context());
//        view.setOnItemClickListener(this);
//
//        container.$add(this);
//        Width(this.width);
//    }
//
//    /**
//     * Adds a series of images to the gallery
//     * @param images list of images
//     */
//    @SimpleProperty
//    public void Images(YailList images) {
//        //convert from YailList to string array and
//        this.images = images;
//        adapter.setImages(images.toStringArray());
//        view.setAdapter(adapter);
//
//        view.requestLayout();
//    }
//
//    /**
//     * Used to specify number of spaces between images
//     *
//     * @param spaces
//     */
//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "5")
//    @SimpleProperty
//    public void Padding(int spaces) {
//        this.imagePad = spaces;
//        view.setSpacing(spaces);
//    }
//
//    /**
//     * Returns number of padding spaces between images
//     *
//     */
//    @SimpleProperty
//    public int Padding() {
//        return this.imagePad;
//    }
//
////    /**
////     *
////     * @param itemstring
////     */
////    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Option 1,Option 2,Option 3")
////    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
////    public void ImagesFromString(String itemstring) {
////        if (itemstring.length() == 0) {
////            this.images = new YailList();
////        } else {
////            this.images = YailList.makeList((Object[]) itemstring.split(" *, *"));
////        }
////
////        //Now make it display in the spinner button
////        view.setAdapter(adapter);
////        view.requestLayout();
////    }
//
//    // NOTE: Don't set width height of adapter because it will set the widht/height of each actual image not the gallery itself
//    @SimpleProperty
//    public void ThumbnailWidth(int width) {
//        adapter.setWidth(width);
//        view.setAdapter(adapter);
//        view.requestLayout();
//    }
//
//    @SimpleProperty
//    public int ThumbnailWidth() {
//        return adapter.getWidth();
//    }
//
//    @SimpleProperty
//    public void ThumbnailHeight(int height) {
//        adapter.setHeight(height);
//        view.setAdapter(adapter);
//        view.requestLayout();
//    }
//
//    @SimpleProperty
//    public int ThumbnailHeight() {
//        return adapter.getHeight();
//    }
//
//
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public void Selection(String value) {
//        value = value.toLowerCase();
//        YailList images = Images();
////        for (int i=0; i< images.size(); i++) {  // use this if new YailList (array) is used
//        for (int i=0; i< images.length(); i++) {
//            if (value.equals(images.getString(i).toLowerCase())) {
//                view.setSelection(i);
//                selectedImageIndex=i;
//                break;
//            }
//        }
//
//    }
//
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public String Selection() {
//        return images.getString((int) selectedImageIndex);
//    }
//
//    /**
//     * Elements property getter method
//     *
//     * @return a YailList representing the list of images
//     */
//    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
//    public YailList Images() {
////        String[] images = adapter.getImages();
////        return YailList.makeList(images);
//        return this.images;
//
//    }
//
//    @Override
//    public View getView() {
//        return view;
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
////        EventDispatcher.dispatchEvent(this, "Click", images.getString(imageIndex));
////        AfterPicking(images.getString(imageIndex));
//        selectedImageIndex = index;
//        AfterPicking(images.getString(index));
//    }
//
//    /**
//     * todo: blah blah blah
//     */
//    @SimpleEvent(description = "Triggered after an image is selected. It will also report selected image name")
//    public void AfterPicking(String imageName) {
//        EventDispatcher.dispatchEvent(this, "AfterPicking", imageName);
//    }
////    @Override
////    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
//////        EventDispatcher.dispatchEvent(this, "Click", images.getString(imageIndex));
////        AfterPicking(images.getString(index));
////    }
////
////    @Override
////    public void onNothingSelected(AdapterView<?> adapterView) {
////        //To change body of implemented methods use File | Settings | File Templates.
////    }
//
//
//    class StackAdapter extends ArrayAdapter<StackItem> {
//
//        private List<StackItem> items;
//        private Context context;
//
//        public StackAdapter(Context context, int textViewResourceId, List<StackItem> objects) {
//            super(context, textViewResourceId, objects);
//            this.items = objects;
//            this.context = context;
//        }
//
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View itemView = convertView;
//            if (itemView == null) {
//                LinearLayout linearLayout = new LinearLayout(context);
//                android.widget.LinearLayout.LayoutParams localLayoutParams =
//                        new android.widget.LinearLayout.LayoutParams(
//                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
//                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT);
//                linearLayout.setLayoutParams(localLayoutParams);
//                itemView = linearLayout;
//            }
//            StackItem stackItem = items.get(position);
//            if (stackItem != null) {
//                // TextView defined in the stack_item.xml
//                TextView textView = new TextView(context);
//                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                textView.setLayoutParams(layoutParams);
//
//                // ImageView defined in the stack_item.xml
//                ImageView imageView = new ImageView(context);
//                layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                imageView.setLayoutParams(layoutParams);
//                layoutParams.gravity= Gravity.CENTER;
//
//                if (textView != null) {
//                    textView.setText(stackItem.getItemText());
//
//                    // "image1", "image2",..
//                    String imageName= stackItem.getImageName();
//
//                    int resId= this.getDrawableResIdByName(imageName);
//
//                    imageView.setImageResource(resId);
//                    imageView.setBackgroundColor(Color.rgb(211,204,188));
//                }
//
//            }
//            return itemView;
//        }
//
//    public class StackItem {
//
//        private String itemText;
//
//        // "image1","image2",..
//        private String imageName;
//
//        public StackItem(String text, String imageName) {
//            this.imageName = imageName;
//            this.itemText = text;
//        }
//
//        public String getImageName() {
//            return imageName;
//        }
//
//
//        public String getItemText() {
//            return itemText;
//        }
//
//    }
//}
