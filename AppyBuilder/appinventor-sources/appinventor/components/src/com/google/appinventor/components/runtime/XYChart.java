// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.graphics.Color;
import android.view.View;
import com.androidplot.xy.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.LinkedList;

@DesignerComponent(version = YaVersion.XYCHART_COMPONENT_VERSION,
        description = "XYChart component allows you blah blah blah blah blah",
        category = ComponentCategory.VISUALIZATION)
@SimpleObject
@UsesLibraries(libraries = "Androidplot-core-0.5.0-release.jar")
public final class XYChart extends AndroidViewComponent {
    private static final int HISTORY_SIZE = 30;            // number of points to plot in history

    private String title = "Title";
    private XYPlot xyPlot;

    //our series
    private SimpleXYSeries xAxisSeries;
    private SimpleXYSeries yAxisSeries;
    private SimpleXYSeries zAxisSeries;

    private String legendX = " X Axis";
    private String legendY = " Y Axis";
    private String legendZ = " Z Axis";


    private float xMinValue=0f;
    private float xMaxValue=30f;
    private String xLabel="X Axis";

    private float yMinValue=-180f;
    private float yMaxValue=359f;
    private String yLabel="Y Axis";

    private LinkedList<Number> xHistory;
    private LinkedList<Number> yHistory;
    private LinkedList<Number> zHistory;

    /**
     * Creates a new Image component.
     *
     * @param container  container, component will be placed in
     */
    public XYChart(ComponentContainer container) {
        super(container);
        xyPlot = new XYPlot(container.$context(), this.title);

        xHistory = new LinkedList<Number>();
        yHistory = new LinkedList<Number>();
        zHistory = new LinkedList<Number>();

        xyPlot.setBorderPaint(null);

        xyPlot.setMinimumWidth(LENGTH_FILL_PARENT);
        xyPlot.setMinimumHeight(222);

        xAxisSeries = new SimpleXYSeries(legendX);
        yAxisSeries = new SimpleXYSeries(legendY);
        zAxisSeries = new SimpleXYSeries(legendZ);

        xyPlot.disableAllMarkup();
        //http://androidplot.com/docs/a-dynamic-xy-plot/
//        xyPlot.addSeries(xAxisSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.RED, Color.RED, null));
//        xyPlot.addSeries(yAxisSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.GREEN, Color.GREEN, null));
//        xyPlot.addSeries(zAxisSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.BLUE, Color.BLUE, null));

        //todo: the last parm is the fill color. Maybe I should have a property for this. If none, then pass null, else the fill color
//        LineAndPointFormatter lf1= new LineAndPointFormatter(Color.RED, Color.RED, Color.RED);
//        lf1.getFillPaint().setAlpha(66);   //adds some transparency to the fill color
//        xyPlot.addSeries(xAxisSeries, lf1);
//        xyPlot.setGridPadding(5, 0, 5, 0);    //do I really need this??

        xyPlot.addSeries(xAxisSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.RED, Color.RED, null));
        xyPlot.addSeries(yAxisSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.GREEN, Color.GREEN, null));
        xyPlot.addSeries(zAxisSeries, LineAndPointRenderer.class, new LineAndPointFormatter(Color.BLUE, Color.BLUE, null));


//        dynamicPlot.addSeries(sine1Series, new LineAndPointFormatter(Color.rgb(0, 0, 0), null, Color.rgb(0, 80, 0)));

//        xyPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);

        //todo: make these configurable
//        xyPlot.setDomainStepValue(5);
//        xyPlot.setTicksPerRangeLabel(3);
//
//        xyPlot.getRangeLabelWidget().pack();
//        xyPlot.getDomainLabelWidget().pack();

        // Adds the component to its designated container
        container.$add(this);

    }


    //------------ x related
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0")
    @SimpleProperty
    public void XMinValue(float value) {
        this.xMinValue=value;
//        xyPlot.setDomainLeftMin(value);
        xyPlot.setDomainBoundaries(value, this.xMaxValue, BoundaryMode.FIXED);
        xyPlot.redraw();
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float XMinValue() {
        return xMinValue;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Title")
    @SimpleProperty
    public void Title(String title) {
        this.title=title;
        xyPlot.setTitle(title);
        xyPlot.redraw();
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String Title() {
        return this.title;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "30")
    @SimpleProperty
    public void XMaxValue(float value) {
        this.xMaxValue=value;
//        xyPlot.setDomainLeftMax(value);
        xyPlot.setDomainBoundaries(this.xMinValue,value,  BoundaryMode.FIXED);
        xyPlot.redraw();
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float XMaxValue() {
        return xMaxValue;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "X Axis")
    @SimpleProperty
    public void XAxisLabel(String value) {
        this.xLabel=value;
        xyPlot.setDomainLabel(value);
        xyPlot.redraw();   //not sure if I have to redraw
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String XAxisLabel() {
        return xLabel;
    }
    //--------------- y related
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "-180")
    @SimpleProperty
    public void YMinValue(float value) {
        this.yMinValue=value;
        xyPlot.setRangeBoundaries(value, this.yMaxValue, BoundaryMode.FIXED);
        xyPlot.redraw();  //not sure if I have to redraw
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float YMinValue() {
        return yMinValue;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "359")
    @SimpleProperty
    public void YMaxValue(float value) {
        this.yMaxValue=value;
        xyPlot.setRangeBoundaries(this.yMinValue, value,  BoundaryMode.FIXED);
        xyPlot.redraw();  //not sure if I have to redraw
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public float YMaxValue() {
        return yMaxValue;
    }
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Y Axis")
    @SimpleProperty
    public void YAxisLabel(String value) {
        this.yLabel=value;
        xyPlot.setRangeLabel(value);
        xyPlot.redraw();   //not sure if I have to redraw
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public String YAxisLabel() {
        return yLabel;
    }
    //-----------------------

    @SimpleProperty
    public void RefreshChart(YailList valuesList) {

        if (null == valuesList || valuesList.size()== 0 ) {
            return;
        }

        Object[] objects = valuesList.toStringArray();
        int dataSize = valuesList.size();

//        for (Object object : objects) {
//            Log.e("RefreshChart", "value is of type: " + object.getClass());
//            if (!(object instanceof Float)) {
//                throw new YailRuntimeError("Items passed to Chart must be Float", "Error");
//            }
//        }

        Float x = null;
        Float y = null;
        Float z = null;

        x = new Float(objects[0]+"");

//        x * Math.sin(index + phase + 4);

        if (dataSize > 1) y = new Float(objects[1]+"");
        if (dataSize > 2) z = new Float(objects[2]+"");


        // get rid the oldest sample in history:
        if (xHistory.size() > HISTORY_SIZE) {
            xHistory.removeFirst();
            if (dataSize > 1) yHistory.removeFirst();
            if (dataSize > 2) zHistory.removeFirst();
        }

        // add the latest history sample:
        xHistory.addLast(x);
        if (dataSize > 1) yHistory.addLast(y);
        if (dataSize > 2) zHistory.addLast(z);


        // update the plot with the updated history Lists:
        xAxisSeries.setModel(xHistory, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        if (dataSize > 1) yAxisSeries.setModel(yHistory, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        if (dataSize > 2) zAxisSeries.setModel(zHistory, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        xyPlot.redraw();

    }


    @Override
    public View getView() {
        return xyPlot;
    }
}



