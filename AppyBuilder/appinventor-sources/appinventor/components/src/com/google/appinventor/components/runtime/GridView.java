package com.google.appinventor.components.runtime;

//todo: Check this: https://www.learn2crack.com/2014/01/android-custom-gridview.html
// https://www.learn2crack.com/2014/01/android-custom-gridview.html

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.*;
import org.apache.http.util.TextUtils;

import java.io.IOException;

@DesignerComponent(version = YaVersion.GRIDVIEW_COMPONENT_VERSION,
        description = "<p>A GridView is a component that displays items in a two-dimensional, scrollable grid. The GridView can contain list of TEXT items or Asset image items OR combination</p>",
        category = ComponentCategory.USERINTERFACE,
        nonVisible = false,
        iconName = "images/gridview.png")
@SimpleObject
public class GridView extends AndroidViewComponent implements AdapterView.OnItemClickListener {
    private final Drawable defaultBackgroundDrawable;
    private final Context context;
    private android.widget.GridView view;
    private MyAdapter adapter;
    private String TAG = "GridView";

    private YailList elements;
    private int elementsPad=5;
    private int defaultNumCols=4;
    private long selectedImageIndex=0;
    private int columnWidth=150;
    private String imagePath = "";
    private Drawable backgroundImageDrawable;
    private int backgroundColor;
    private boolean isStretched=true;
    private int textColor=0xFF000000;  // black

    // https://www.codeproject.com/questions/705639/how-to-create-gridview-in-android-programaticall
    // http://www.materialdoc.com/
    // https://developer.android.com/guide/components/index.html

    /**
     * Creates a new AndroidViewComponent.
     *
     * @param container container, component will be placed in
     */
    public GridView(ComponentContainer container) {
        super(container);
        this.context = container.$context();
        view = new android.widget.GridView(container.$context());
//        view.setColumnWidth(4);
        defaultBackgroundDrawable = getView().getBackground();

        view.setLayoutParams(new android.widget.GridView.LayoutParams(
                android.widget.LinearLayout.LayoutParams.FILL_PARENT,
                android.widget.LinearLayout.LayoutParams.FILL_PARENT));
//        view.setBackgroundColor(COLOR_LTGRAY);
        StretchEnabled(isStretched);
        view.setGravity((Gravity.CENTER_HORIZONTAL));

        Columns(defaultNumCols);
        BackgroundColor(Component.COLOR_DEFAULT);

        adapter = new MyAdapter(container.$context());

        // Do below only AFTER adapter is initialized
//        ThumbnailWidth(columnWidth);
        FontSize(Component.FONT_DEFAULT_SIZE);
        TextColor(COLOR_BLACK);
        ElementsFromString("");

        view.setOnItemClickListener(this);

        //TODO: What should be the stretchMode for columnWidth
//        view.setStretchMode();

//        view.setAdapter(adapter);
        container.$add(this);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description="The elements specified as a string with the " +
            "items separated by commas " +
            "such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the " +
            "list.",  category = PropertyCategory.BEHAVIOR)
    public void ElementsFromString(String itemstring) {
        elements = ElementsUtil.elementsFromString(itemstring);
        Elements(elements);
    }
    @SimpleFunction(description="CLears the items from the component")
    public void ClearGridView() {
        adapter.clear();
        adapter = new MyAdapter(context);
        adapter.setElements(elements.toStringArray());
        view.setAdapter(adapter);
        Elements(elements);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty
    public void StretchEnabled(boolean enabled) {
        this.isStretched = enabled;
        if (enabled) {
//            view.setStretchMode(android.widget.GridView.STRETCH_SPACING_UNIFORM);
            view.setStretchMode(android.widget.GridView.STRETCH_COLUMN_WIDTH);
        } else {
            view.setStretchMode(android.widget.GridView.NO_STRETCH);
        }
//        view.setColumnWidth(222);
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int TextColor() {
        return textColor;
    }

    /**
     * Specifies the checkbox's text color as an alpha-red-green-blue
     * integer.
     *
     * @param argb  text RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void TextColor(int argb) {
        adapter.setTextColor(argb);
    }

    /**
     * Specifies the label's text's font size, measured in sp(scale-independent pixels).
     *
     * @param size font size in sp (scale-independent pixels)
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty(description = "Sets the font size of the elements")
    public void FontSize(float size) {
        adapter.setFontSize(size);
    }
    @SimpleProperty(description = "Returns the font size of the elements")
    public float FontSize() {
        return adapter.getFontSize();
    }

    /**
     * Adds a series of elements to the component
     * @param elements list of elements
     */
    @SimpleProperty
    public void Elements(YailList elements) {
        if (elements == null) return;

        //convert from YailList to string array and
        this.elements = elements;
        adapter = new MyAdapter(container.$context());
//        adapter.clear();
        adapter.setElements(elements.toStringArray());
        adapter.notifyDataSetChanged();
        view.setAdapter(adapter);
        view.invalidate();
//        view.requestLayout();
    }


    //todo: read this: http://stackoverflow.com/questions/10792258/android-scrolling-background-in-gallery
    //todo: It would be good idea to allow user specify a folder to automatically load all files (with some extension
    //todo:     regex (e.g. *.png) so that we load all those elements.

    /**
     * Used to specify number of spaces between elements
     *
     * @param padding
     */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "2")
    @SimpleProperty(description = "The amount of padding on left, top, right, bottom")
    public void Padding(int padding) {
        this.elementsPad = padding;
      adapter.setPadding(padding);
      /*view.setAdapter(adapter);
      view.requestLayout();*/
    }

    /**
     * Returns number of padding spaces between elements
     *
     */
    @SimpleProperty
    public int Padding() {
        return this.elementsPad;
    }


//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
//    @SimpleProperty(description = "Specifies the path of the component background image.  ")
    public void BackgroundImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        try {
            backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
            updateAppearance();
//            view.setBackground(backgroundImageDrawable);
        } catch (IOException ioe) {
            // TODO(user): Maybe raise Form.ErrorOccurred.
            Log.e(TAG, "Unable to load " + imagePath);
            // Fall through with a value of null for backgroundImageDrawable.
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty(description = "Specifies the background color. ")
    public void BackgroundColor(int argb) {
//        view.setBackgroundColor(argb);
        backgroundColor = argb;
        view.setBackgroundColor(backgroundColor);

//        updateAppearance();
    }

     @SimpleProperty(description = "Returns the background color. ")
    public int BackgroundColor() {
        return backgroundColor;
    }

    // Update appearance based on values of backgroundImageDrawable, backgroundColor and shape.
    // Images take precedence over background colors.
    private void updateAppearance() {
        // If there is no background image,
        // the appearance depends solely on the background color and shape.
        if (backgroundImageDrawable == null) {
            if (backgroundColor == Component.COLOR_DEFAULT) {
                // If there is no background image and color is default,
                // restore original 3D bevel appearance.
                ViewUtil.setBackgroundDrawable(view, defaultBackgroundDrawable);
            } else {
                // Clear the background image.
                ViewUtil.setBackgroundDrawable(view, null);
                view.setBackgroundColor(backgroundColor);
            }
        } else {
            // If there is a background image
            ViewUtil.setBackgroundImage(view, backgroundImageDrawable);
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "155")
    @SimpleProperty(description = "Sets the thumbnail width")
    public void ThumbnailWidth(int width) {
        /*this.columnWidth = width;
        view.setColumnWidth(width);*/
        adapter.setWidth(width);
        view.setAdapter(adapter);
        view.requestLayout();
    }


    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns number of columns for this component")
    public int Columns() {
        return view.getNumColumns();
    }

    /**
     * Number of columns for the view
     *
     * @param columns
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "4")
    @SimpleProperty(description = "Sets number of columns used for this component")
    public void Columns(int columns) {
        view.setNumColumns(columns);
        view.invalidate();  //redraw
    }

    @SimpleProperty
    public int ThumbnailWidth() {
        return adapter.getWidth();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "155")
    @SimpleProperty(description = "Sets the thumbnail height")
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
        YailList elements = Elements();
//        for (int i=0; i< elements.size(); i++) {  // use this if new YailList (array) is used
        for (int i=0; i< elements.length(); i++) {
            if (value.equals(elements.getString(i).toLowerCase())) {
                view.setSelection(i);
                selectedImageIndex=i;
                break;
            }
        }

    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String Selection() {
        return elements.getString((int) selectedImageIndex);
    }

    /**
     * Elements property getter method
     *
     * @return a YailList representing the list of elements
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public YailList Elements() {
//        String[] elements = adapter.getElements();
//        return YailList.makeList(elements);
        return this.elements;

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
//        EventDispatcher.dispatchEvent(this, "Click", elements.getString(imageIndex));
//        AfterPicking(elements.getString(imageIndex));
        selectedImageIndex = index;
        AfterPicking(elements.getString(index));
    }

    /**
     * todo: blah blah blah
     */
    @SimpleEvent
    public void AfterPicking(String item) {
        EventDispatcher.dispatchEvent(this, "AfterPicking", item);
    }

    private class MyAdapter extends ArrayAdapter<String> {
        private Context mContext;

        private String[] elements = {};
        private int width = 150;
        private int height = 150;
        private int padding = 2;
        private int imageBackground;
        private float fontSize=Component.FONT_DEFAULT_SIZE;
        private int textColor=0xFF000000;  // black

        public MyAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            mContext = context;

            imageBackground = container.$context().getResources().getIdentifier("ImageGallery_android_galleryItemBackground", "styleable", container.$context().getPackageName());
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
            return elements.length;
        }

/*        public Object getItem(int position) {
            return position;
        }*/

        public long getItemId(int position) {
            return position;
        }

        public void setElements(String[] elements) {
            this.elements = elements;
        }

        public String[] getElements() {
            return this.elements;
        }

        // Override this method according to your need
        public View getView(int index, View convertView, ViewGroup parent) {
            ImageView iv = new ImageView(mContext);
            TextView textView = new TextView(mContext);

            // Check if an existing view is being reused, otherwise inflate the view
            // adapter = new ArrayAdapter<String>(container.$form(), android.R.layout.simple_list_item_1);

            /*if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
                convertView.findViewById(android.R.layout.simple_list_item_1);
                convertView.layout(android.R.layout.simple_list_item_1);
            }*/
            String anElement = elements[index];
            anElement = anElement.toLowerCase();
            boolean isImage = false;
            if (anElement.endsWith(".png") || anElement.endsWith(".jpg") || anElement.endsWith(".jpeg") || anElement.endsWith(".gif")) {
                isImage = true;
            }
            try {
                if (isImage) {
                    iv.setImageDrawable(MediaUtil.getBitmapDrawable(container.$form(), elements[index]));
                    iv.setLayoutParams(new android.widget.GridView.LayoutParams(width, height));
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    iv.setBackgroundResource(imageBackground);
                    iv.setPadding(padding, padding, padding, padding);
                    return iv;
                } else {
                    // we are dealing with text
                    textView.setText(elements[index]);
                    textView.setTextSize(fontSize);
                    textView.setTextColor(textColor);
                    textView.setLayoutParams(new android.widget.GridView.LayoutParams(width, height));
                    textView.setBackgroundResource(imageBackground);
                    textView.setPadding(padding, padding, padding, padding);

                    return textView;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return textView;
        }

        public void setFontSize(float fontSize) {
            this.fontSize = fontSize;
        }

        public float getFontSize() {
            return fontSize;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public float getTextColor() {
            return textColor;
        }

        public int getPadding() {
            return padding;
        }

        public void setPadding(int padding) {
            this.padding = padding;
        }
    }
}
