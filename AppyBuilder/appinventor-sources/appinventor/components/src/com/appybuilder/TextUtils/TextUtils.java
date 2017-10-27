package com.appybuilder.TextUtils;

import android.util.Log;
import android.widget.TextView;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DesignerComponent(version = TextUtils.VERSION,
        description ="Join AppyBuilder Community at <a href=\"http://Community.AppyBuilder.com\" target=\"ab\">http://Community.AppyBuilder.com</a> <p>" +
                "AppyBuilder Text utility is a component that can be used to perform common text-utilities",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "http://www.appybuilder.com/extensions/icons/extensionIcon.png")
@SimpleObject(external = true)
public class TextUtils extends AndroidNonvisibleComponent implements Component {

    public static final int VERSION = 1;
    private ComponentContainer container;


    public TextUtils(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        Log.d("CDK", "TextUtils" );
    }

   @SimpleFunction(description = "Converts words to TitleCase. E.g. converts: title case to Title Case")
    public String TitleCase(String text) {
        text = text.trim();
        if (text.isEmpty()) return text;

       List<String> wordArray = new ArrayList<String>();
        String[] words = text.split("\\s+"); //splits by space, ignores double space
        String result="";
        for (String aWord : words) {
            result += " " + Character.toTitleCase(aWord.charAt(0)) + aWord.substring(1);
        }
        if (!result.isEmpty()) result = result.substring(1);
        return result;

    }

//    @SimpleFunction(description = "Rotates TextBox with given rotation")
//    public void Rotate(TextBox textBox, int rotation) {
//        textBox.getView().setRotation(rotation);
//    }

     @SimpleFunction(description = "Rotates component with given rotation; from 0 to 360 degrees")
    public void Rotate(AndroidViewComponent visibleComp, int rotation) {
         visibleComp.getView().setRotation(rotation);
    }

     @SimpleFunction(description = "Rotates component with given rotation; from 0 to 360 degrees")
    public void RotateTextBox(TextBox visibleComp, int rotation) {
         ((TextView) visibleComp.getView()).setRotation(180);
    }

    @SimpleFunction(description = "Reverses the text")
    public String Reverse(String text) {
       return new StringBuilder(text).reverse().toString();
    }

   @SimpleFunction(description = "Compares text1 and text2 and returns true if equal. If ignoreCase is true, it ignores the case")
    public boolean Equals(String text1, String text2, boolean ignoreCase) {
        if (ignoreCase) return text1.equalsIgnoreCase(text2 );
        else return text1.equals(text2);
    }

   @SimpleFunction(description = "Formats a numeric text with thousand separator using current Locale. " +
           "Use numDecimals to specify number of decimals. If text is NOT numeric, returns itself")
    public String FormatThousandSeparator(String text, int numDecimals) {
        text = text.trim();
        if (!isNumeric(text)) return text;

       // Creates string that repeats # n times
       char[] chars = new char[numDecimals];
       Arrays.fill(chars, '#');
       String strNumDecimals = new String(chars);

       double amount = Double.parseDouble(text);
       DecimalFormat formatter = numDecimals == 0? new DecimalFormat("#,###"): new DecimalFormat("#,###." + strNumDecimals);

       return formatter.format(amount);
    }

   @SimpleFunction(description = "A regular expression is a special text string for " +
           "describing a search pattern. You can think of regular expressions as wildcards on steroids")
    public String ReplaceRegex(String text, String regexExpression, String replaceWith) {
        String result="";
       Pattern p = Pattern.compile(regexExpression);

        // get a matcher object
       Matcher matcher = p.matcher(text);
       result = matcher.replaceAll(replaceWith);

       return result;
    }

    private boolean isNumeric(String text) {
        try {
            Float.parseFloat(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}



