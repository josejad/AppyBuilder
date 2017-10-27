package com.appybuilder.ListUtils;

import android.util.Log;
import android.widget.Toast;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;
import java.util.*;


@DesignerComponent(version = ListUtils.VERSION,
        description ="Join AppyBuilder Community at <a href=\"http://Community.AppyBuilder.com\" target=\"ab\">http://Community.AppyBuilder.com</a> <p>" +
                "AppyBuilder List utility component that can be used to manipulate a list and perform " +
                "such functions as shuffling, sorting, reversing, etc.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "http://www.appybuilder.com/extensions/icons/extensionIcon.png")
@SimpleObject(external = true)
public class ListUtils extends AndroidNonvisibleComponent implements Component {

    public static final int VERSION = 3;
    private ComponentContainer container;
    private String LOG_TAG="ListUtils";


    public ListUtils(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        Log.d("CDK", "ListUtils" );
    }

   @SimpleFunction(description = "Sorts a list in ascending or descending. 1=ascending, -1=descending")
    public YailList SortList(YailList list, int sortOrder) {
       String[] strArray = list.toStringArray();
       if (sortOrder > 0) Arrays.sort(strArray);
       else if (sortOrder < 0) Arrays.sort(strArray, Collections.reverseOrder());

       return YailList.makeList(strArray);

    }

   @SimpleFunction(description = "Shuffles a list. Keeps original list unchanged")
    public YailList Shuffle(YailList list) {
//       Toast.makeText(container.$context(), "executing shuffle", Toast.LENGTH_LONG);
        YailList foo = YailList.makeList(list);
       Collections.shuffle(foo);

       return foo;
    }


   @SimpleFunction(description = "Reverses a list from bottom to top")
    public YailList Reverse(YailList list) {
        if (list.isEmpty()) return list;

       String[] strings = list.toStringArray();
       List<String> strings1 = Arrays.asList(strings);
       Collections.reverse(strings1);

       return YailList.makeList(strings1);

    }

    @SimpleFunction(description = "Removes duplicates from list. <br/><p>If ignoreCase is true, then it ignores case-sensitive. " +
            "For example 'a' and 'A' will be treated same")
    public YailList RemoveDuplicates(YailList list, boolean ignoreCase) {

        list.toStringArray();
        List<String> myList = new ArrayList<String>();
        myList.addAll(Arrays.asList(list.toStringArray()));

        List<String> list2= new ArrayList<String>(new LinkedHashSet<String>(myList));
        if (ignoreCase) {
            Set<String> toRetain = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            toRetain.addAll(list2);
            Set<String> set = new LinkedHashSet<String>(list2);
            set.retainAll(new LinkedHashSet<String>(toRetain));
            list2 = new ArrayList<String>(set);
        }

        return YailList.makeList(list2);
    }


    @SimpleFunction(description = "List 1 would be text to display and list 2 will be image to display")
    public YailList ElementsFromLists(YailList listItems, YailList listImages) {
        String[] strArrayListItems = listItems.toStringArray();
        String[] strArrayListImages = listImages.toStringArray();

        Log.d(LOG_TAG, "items:" + Arrays.toString(strArrayListItems));
        Log.d(LOG_TAG, "images:" + Arrays.toString(strArrayListImages));

        String csvItems="";
        for (int i=0; i<strArrayListItems.length; i++) {
            csvItems = csvItems + "," + strArrayListItems[i] + "|" + strArrayListImages[i];
        }
        if (!csvItems.equals("")) {
            csvItems = csvItems.substring(1); //get rid of 1st comma
        }
        Log.d(LOG_TAG, "new list is:{"+csvItems+"}");
        return ElementsUtil.elementsFromString(csvItems);
    }

}

