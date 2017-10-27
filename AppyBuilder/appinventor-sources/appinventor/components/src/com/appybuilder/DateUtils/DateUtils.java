package com.appybuilder.DateUtils;

import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.Dates;
import com.google.appinventor.components.runtime.util.YailList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@DesignerComponent(version = DateUtils.VERSION,
        description ="Join AppyBuilder Community at <a href=\"http://Community.AppyBuilder.com\" target=\"ab\">http://Community.AppyBuilder.com</a> <p>" +
                "AppyBuilder Date utility helper methods allowing you to perform such functions like " +
                "getting current date / time in user specified formats," +
                " calculating day, hour, minute, second difference between 2 dates",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "http://www.appybuilder.com/extensions/icons/extensionIcon.png")
@SimpleObject(external = true)
public class DateUtils extends AndroidNonvisibleComponent implements Component {

    public static final int VERSION = 1;
    private ComponentContainer container;


    public DateUtils(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        Log.d("CDK", "DateUtils Created" );
    }


    /**
     * Takes imageA and imageB and returns imageC
     *
     * Such that RGBA of (point in imageC) = weight*(point in imageA) + (1 - weight)*(point in imageB)
     */
    @SimpleFunction(description = "Returns current date and/or time in specified format. " +
            "Examples are: yyyy.MM.dd G HH:mm:ss z. Where MM is month or MMM is 3 char mont, HH is 24hr time and hh is 12hr time")
    public String FormatCurrentDateTime(String dateFormat) {
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(today);
    }

    @SimpleFunction(description = "Returns days, hours, minutes, seconds between 2 dates. Note that dates have to have same format")
    public YailList TimeDiff(String dateStart, String dateEnd, String dateFormat) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        Date d1, d2;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateEnd);
            long diff = d2.getTime() - d1.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            dateTimeFormat.setLenient(true);

            Calendar start = new GregorianCalendar();
            start.setTime(dateTimeFormat.parse(dateStart));

            Calendar end = new GregorianCalendar();
            end.setTime(dateTimeFormat.parse(dateEnd));

//            long diffWeeks = duration/1000/60/60/24/7

            List<String> results = Arrays.asList(diffDays+"", diffHours+"", diffMinutes+"", diffSeconds+"");
            return YailList.makeList( results);
        } catch (Exception e) {
            return YailList.makeList(Arrays.asList("Error,in,conversion,"+e.getMessage(), "0", "0", "0"));

        }
    }


}

