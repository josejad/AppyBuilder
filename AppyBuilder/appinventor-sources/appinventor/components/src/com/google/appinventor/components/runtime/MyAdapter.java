//package com.google.appinventor.components.runtime;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.AssetManager;
//import android.content.res.Resources;
//import android.graphics.drawable.Drawable;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//import com.google.appinventor.components.runtime.beans.RowItem;
//
//import java.io.IOException;
//import java.util.List;
//
//public class MyAdapter extends ArrayAdapter<RowItem> {
//
//    private static final String TAG = "MyAdapter";
//    private final Context mContext;
//    public int setThumbWidth=80;
//    public int setThumbHeigth=80;
//    public boolean showText=true;
//
//    public MyAdapter(final Context context, int resourceId, final List<RowItem> items) {
//        super(context, resourceId, items);
//        this.mContext = context;
//    }
//
//
//    // nice tut, but won't be good for my work: http://www.viralandroid.com/2016/02/android-listview-with-image-and-text.html
////http://theopentutorials.com/tutorials/android/listview/android-custom-listview-with-image-and-text-using-arrayadapter/
//    //http://www.android-examples.com/android-custom-listview-with-imageview-and-textview-using-arrayadapter-string/
//        /*private view holder class*/
//    private class ViewHolder {
//        ImageView imageView;
//        TextView txtDesc;
//    }
//
//    @Override
//    public long getItemId(final int position) {
//        return getItem(position).hashCode();
//    }
//
//    @Override
//    public View getView(final int position, View convertView, final ViewGroup parent) {
//        RowItem rowItem = getItem(position);
//
//        ViewHolder viewHolder = null;
//        if (convertView == null) {
//
//            // programmatically find lvImageText.xml layout from resources
//            Resources res = mContext.getResources();
//            int myLayout = res.getIdentifier("list_item", "layout", mContext.getPackageName());
//
//            // after layout is found, then inflate it and assign it to convertView
//            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//            convertView = mInflater.inflate(myLayout, null);
//
//            // from the inflated convertView (which is actually lvImageText.xml), find the desc and icon IDs,
//            //  and then set it into our viewHolder
//            viewHolder = new ViewHolder();
//            viewHolder.txtDesc = (TextView) convertView.findViewById(res.getIdentifier("desc", "id", mContext.getPackageName()));
//            viewHolder.imageView = (ImageView) convertView.findViewById(res.getIdentifier("icon", "id", mContext.getPackageName()));
//
//            // do we want to show text or make it invisible?
//            if (!showText) {
//                viewHolder.txtDesc.setVisibility(View.INVISIBLE);
//            }
//
////            viewHolder.txtDesc.setGravity(Gravity.CENTER);
//
//            setWidthHeight(viewHolder.imageView, setThumbWidth, setThumbHeigth);
//
//            // Now associate the convertView with our viewHolder, so that we can later use it
//            convertView.setTag(viewHolder);
//
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
//        viewHolder.txtDesc.setText(rowItem.getDesc());
//        String imageName = rowItem.getImageName();
//        try {
//            viewHolder.imageView.setImageDrawable(getAsset(mContext, imageName));
//        } catch (Exception e) {
//            Log.d(TAG, "Error loading image: " + imageName);
//        }
//
////            viewHolder.imageView.setImageResource(rowItem.getImageName());
//
////            RowItem rowItem = getItem(position);
////
////            tv.setText(rowItem.getDesc());
////            tv.setTextColor(itemColor);
//        return convertView;
//    }
//
//    private void setWidthHeight(View view, int w, int h) {
//        ViewGroup.LayoutParams params=view.getLayoutParams();
//        params.width=w;
//        params.height=h;
//        view.setLayoutParams(params);
//    }
//
//    public static Drawable getDrawable(Context context, String name) {
//        int resourceId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
//        return context.getResources().getDrawable(resourceId);
//    }
//
//    public static Drawable getAsset(Context contex, String assetName) {
//        AssetManager assetManager = contex.getAssets();
//
//
//        try {
//            Drawable drawable = Drawable.createFromStream(assetManager.open(assetName), null);
////            InputStream is = assetManager.open(assetName);
////            BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
//            return drawable;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//
//}
