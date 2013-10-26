/* Attribute for generating the HTML file with JavaScript to plot simulation
 * results using the dygraphs library. The HTML file is generated by
 * "Export to Web".

 Copyright (c) 2012-2013 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.vergil.basic.export.web;

import java.util.HashMap;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DyGraphsJSPlotter
/**
 * Attribute for generating the HTML file with JavaScript to plot simulation
 * results using the Dygraphs library. The HTML file is generated by
 * "Export to Web". An @link IconLink attribute can be customized with
 * the generated file to open the HTML page.
 * <p>
 * Configure the export parameters by double-clicking on the attribute,
 * then click the "Configure" button).
 * </p><p>
 * <i>dataJSON</i> contains the data series to be plotted in the chart area of
 * the page. Two JavaScript array formats are acceptable:
 * <ul>
 * <li>[{name:"series 1", value:[[x1, y1], [x2, y2], ..., [xn, yn]]},
 *      {name:"series 2", value:[[x'1, y'1], [x'2, y'2], ..., [x'n, y'n]]},
 *      ...]</li>
 * <li>[{name:"series 1", value:[{x:x1, y:y1}, {x:x2, y:y2}, ..., {x:xn, y:yn}]},
 *      {name:"series 2", value:[{x:x'1, y:y'1}, {x:x'2, y:y'2}, ..., {x:x'n, y:y'n}]},
 *      ...]</li>
 * </ul>
 * <i>eventsJSON</i> contains the event information series to be annotated in
 * the chart area. Only one JavaScript array format is acceptable:
 * <ul>
 * <li>[{name:"series 1", value:[{x:x1, y:y1, text:"event 1 info"}, {x:x2, y:y2,
 * text:"event 2 info"}, ...]}, {...}, ...]</li>
 * </ul>
 * </p>
 *
 * @author Baobing (Brian) Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DygraphsJSPlotterAttribute extends JSPlotterAttribute {

    /** Construct an attribute that will generate HTML to plot using the Dygraphs
     *  library.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public DygraphsJSPlotterAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        height.setExpression("2");
        setExpression("Customize by clicking \"Configure\". DyGraphsJSPlotter\n"
                + "is free under the MIT license: http://dygraphs.com/");
        yAxisMode.addChoice("logarithmic");
        horizontalAlign.setVisibility(NONE);
        verticalAlign.setVisibility(NONE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *
     *  @param exporter  The web exporter to write content to
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails, or creating a web attribute fails.
     */
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {
        super._provideAttributes(exporter);
        HashMap<String, String> config = getBasicConfig();

        // Add the graph title and other libraries.
        insertHeaderContent(false, true, "<title>" + config.get("graphTitle")
                + "</title>\n\n");

        for (String line : _otherLibs) {
            insertHeaderContent(false, true, line);
        }

        // Write the parameters to the JavaScript function in the header.
        insertHeaderContent(true, true, "var mainChart;\n");
//        insertHeaderContent(true, true, "$(function () {\n");
        insertHeaderContent(true, true,
                "\tvar data = " + config.get("dataJSON") + ";\n");
        insertHeaderContent(true, true,
                "\tvar events = " + config.get("eventsJSON") + ";\n");

        insertHeaderContent(
                true,
                true,
                "\tvar config = {graphTitle: '" + config.get("graphTitle")
                        + "'" + ", enableLegend: " + config.get("enableLegend")
                        + ", horizontalAlign: '"
                        + config.get("horizontalAlign") + "'"
                        + ", verticalAlign: '" + config.get("verticalAlign")
                        + "'" + ", dataConnectWidth: "
                        + config.get("dataConnectWidth")
                        + ", enableDataMarker: "
                        + config.get("enableDataMarker")
                        + ", dataMarkerRadius: "
                        + config.get("dataMarkerRadius")
                        + ", eventsConnectWidth: "
                        + config.get("eventsConnectWidth")
                        + ", enableEventsMarker: "
                        + config.get("enableEventsMarker")
                        + ", eventsMarkerRadius: "
                        + config.get("eventsMarkerRadius") + ", xAxisMode: '"
                        + config.get("xAxisMode") + "'"
                        + ", drawVerticalGridLine: "
                        + config.get("drawVerticalGridLine")
                        + ", xAxisTitle: '" + config.get("xAxisTitle") + "'"
                        + ", yAxisMode: '" + config.get("yAxisMode") + "'"
                        + ", drawHorizontalGridLine: "
                        + config.get("drawHorizontalGridLine")
                        + ", yAxisTitle: '" + config.get("yAxisTitle")
                        + "'};\n\n");

        // Output the JavaScript code for data plotting to the header.
        for (String line : _plotCodeStart) {
            insertHeaderContent(true, false, line + "\n");
        }

        // Insert custom content, if any
        if (customContent != null && customContent.getExpression() != null
                    && !customContent.getExpression().equals("")) {
            insertHeaderContent(true, false, customContent.getExpression());
        }

        // Insert rest of plotcode
        for (String line : _plotCodeEnd) {
            insertHeaderContent(true, false, line + "\n");
        }

        String widthConfig = Boolean.valueOf(config.get("autoResize")) ? "min-width"
                : "width";
        String heightConfig = Boolean.valueOf(config.get("autoResize")) ? "min-height"
                : "height";
        insertBodyContent("<div id=\"mainChart-container\" style=\""
                + widthConfig + ": " + config.get("graphWidth") + "px; "
                + heightConfig + ": " + config.get("graphHeight")
                + "px; margin: 0 auto\"></div>\n");
        insertBodyContent("<hr><div id=\"sub-container\" style=\""
                + widthConfig + ": " + config.get("graphWidth")
                + "px; height: 280px; margin:0 auto\">\n");

        // Output the body content.
        for (String line : _bodyContent) {
            insertBodyContent(line + "\n");
        }

        // Generate the HTML page.
        WebElement webElement = WebElement.createWebElement(getContainer(),
                "outputHTMLFileWebAttribute", "outputHTMLFileWebAttribute");
        webElement.setExpression(getHTMLPageContent());
        webElement.setParent(config.get("outputHTMLFile"));
        exporter.defineElement(webElement, true);

        // Save the data and events series to a separate file if required.
        if (Boolean.valueOf(config.get("saveDataToFile"))) {
            webElement = WebElement.createWebElement(getContainer(),
                    "outputDataFileWebAttribute", "outputDataFileWebAttribute");
            webElement.setExpression(config.get("dataJSON") + "\n"
                    + config.get("eventsJSON"));
            webElement.setParent(config.get("outputDataFile"));
            exporter.defineElement(webElement, true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private parameters                      ////

    /** Other JavaScript libraries that are required. */
    private static String[] _otherLibs = {
            "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>\n",
            "<script src=\"http://dygraphs.com/dygraph-combined.js\"></script>\n" };

    /** HTML code specifying the layout of the HTML page. */
    private static String[] _bodyContent = {
            "\t<div id=\"legend-container\" style=\"width: 25%; height:100%; float: left\"></div>",
            "\t<div id=\"panel-container\" style=\"width: 25%; height:100%; float: left\"></div>",
            "\t<div id=\"eventsInfo-container\" style=\"width: 49%; height:100%; float: right\"></div>",
            "</div><hr>",
            "<h2>Instructions:</h2>",
            "<ol>",
            "\t<li><b style=\"color:blue;\">Hide/show</b> a data trace or event trace by <b style=\"color:blue;\">clicking </b> the corresponding checkbox.</li>",
            "\t<li>Get <b style=\"color:blue;\">all events</b> that happened at a particular point on the X axis by <b style=\"color:blue;\">single click</b> on a data point on the upper chart</li>",
            "\t<li><b style=\"color:blue;\">Zoom-in/out</b> to/from an interval by <b style=\"color:blue;\">dragging</b> on the lower chart.</li>",
            "</ol>" };

    /** JavaScript code to plot the figure using the Dygraphs library.
     *  Separated into start and end so custom content can be inserted.  */
    private static String[] _plotCodeStart = {
            "\t\t\t\tvar pieChart, dataTable, seriesLabels;\n",
            "\t\t\t\t$(document).ready(function() {",
            "\t\t\t\t\t// parse data and create main chart",
            "\t\t\t\t\tparseJSON();", "\t\t\t\t\tcreateMainChart();",
            "\t\t\t\t});",
            "\t\t\t\t\tfunction createMainChart() {",
            "\t\t\t\t\t\tmainChart = new Dygraph(",
            "\t\t\t\t\t\t\tdocument.getElementById(\"mainChart-container\"),",
            "\t\t\t\t\t\t\tdataTable,",
            "\t\t\t\t\t\t\t{"};

    /** JavaScript code to plot the figure using the Dygraphs library.
     *  Separated into start and end so custom content can be inserted.  */
    private static String[] _plotCodeEnd = {
            "\t\t\t\t\t\t\t\tlabels: seriesLabels,",
            "\t\t\t\t\t\t\t\txlabel: config.xAxisTitle,",
            "\t\t\t\t\t\t\t\tylabel: config.yAxisTitle,",
            "\t\t\t\t\t\t\t\ttitle: config.graphTitle,",
            "\t\t\t\t\t\t\t\tavoidMinZero: true,",
            "\t\t\t\t\t\t\t\tdrawXGrid: config.drawVerticalGridLine,",
            "\t\t\t\t\t\t\t\tdrawYGrid: config.drawHorizontalGridLine,",
            "\t\t\t\t\t\t\t\tlogscale: (config.yAxisMode == 'logarithmic'),",
            "\t\t\t\t\t\t\t\tlegend: config.enableLegend ? 'always' : 'onmouseover',",
            "\t\t\t\t\t\t\t\trangeSelectorHeight: 60,",
            "\t\t\t\t\t\t\t\tshowRangeSelector: true,",
            "\t\t\t\t\t\t\t\tconnectSeparatedPoints: true,",
            "\t\t\t\t\t\t\t\tlabelsDiv: \"legend-container\",",
            "\t\t\t\t\t\t\t\tlabelsSeparateLines: true,",
            "\t\t\t\t\t\t\t\tpointClickCallback: function(e, point) {",
            "\t\t\t\t\t\t\t\t\t$(\"#eventsInfo-container\").html('<b>Events at ' + ",
            "\t\t\t\t\t\t\t\t\t\t\tparseDatetime(point.xval) + '</b>: <br>' +",
            "\t\t\t\t\t\t\t\t\t\t\tgetEvents(point.xval));",
            "\t\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t);// End of new Dygraph()\n",
            "\t\t\t\t\t\t// customize for each series",
            "\t\t\t\t\t\tvar seriesOpts = [], pointShapes = [], panelContent = '';",
            "\t\t\t\t\t\tfor (var shape in Dygraph.Circles){",
            "\t\t\t\t\t\t\tpointShapes.push(Dygraph.Circles[shape]);",
            "\t\t\t\t\t\t}",
            "\t\t\t\t\t\tvar strokePatterns = [null, [6, 3], [2, 3], [6, 2, 2, 2],",
            "\t\t\t\t\t\t                     [6, 2, 2, 2, 2, 2], [2, 4],",
            "\t\t\t\t\t\t                     [12, 5], [18, 5], [12, 5, 2, 5],",
            "\t\t\t\t\t\t                     [18, 5, 2, 5], [18, 5, 2, 5, 2, 5]];\n",
            "\t\t\t\t\t\tjQuery.each(seriesLabels, function(i, seriesName){",
            "\t\t\t\t\t\t\t// add checkbox for visibility control",
            "\t\t\t\t\t\t\tif (i >= 1){",
            "\t\t\t\t\t\t\t\tpanelContent += '<input type=\"checkbox\" checked onClick=\"mainChart.setVisibility(' +",
            "\t\t\t\t\t\t\t\t\t\t(i-1) + ', this.checked)\">' + seriesName + '<br>';",
            "\t\t\t\t\t\t\t}\n",
            "\t\t\t\t\t\t\t// customize data series",
            "\t\t\t\t\t\t\tif (isEventTrace(seriesName)){",
            "\t\t\t\t\t\t\t\tseriesOpts[seriesName] = {",
            "\t\t\t\t\t\t\t\t\t\tdrawPoints: config.enableEventsMarker,",
            "\t\t\t\t\t\t\t\t\t\tstrokeWidth: config.eventsConnectWidth",
            "\t\t\t\t\t\t\t\t};",
            "\t\t\t\t\t\t\t}else {",
            "\t\t\t\t\t\t\t\tseriesOpts[seriesName] = {",
            "\t\t\t\t\t\t\t\t\t\tdrawPoints: config.enableDataMarker,",
            "\t\t\t\t\t\t\t\t\t\tpointSize: config.dataMarkerRadius,",
            "\t\t\t\t\t\t\t\t\t\tdrawPointCallback: pointShapes[i % pointShapes.length],",
            "\t\t\t\t\t\t\t\t\t\tstrokePattern: strokePatterns[i % strokePatterns.length],",
            "\t\t\t\t\t\t\t\t\t\tstrokeWidth: config.dataConnectWidth,",
            "\t\t\t\t\t\t\t\t\t\thighlightCircleSize: config.dataMarkerRadius + 2,",
            "\t\t\t\t\t\t\t\t\t\tdrawHighlightPointCallback: pointShapes[i % pointShapes.length]",
            "\t\t\t\t\t\t\t\t};",
            "\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t});",
            "\t\t\t\t\t\t$(\"#panel-container\").html(panelContent);",
            "\t\t\t\t\t\tmainChart.updateOptions(seriesOpts);",
            "\t\t\t\t\t}// End of createMainChart()",
            "\t\t\t\t\t",
            "\t\t\t\t\t// Parse the data and events series to the native format",
            "\t\t\t\t\tfunction parseJSON(){",
            "\t\t\t\t\t\tseriesLabels = ['x'], dataTable = [];",
            "\t\t\t\t\t\tvar dataObj = {};",
            "\t\t\t\t\t\t",
            "\t\t\t\t\t\tjQuery.each(data, function(i, dataTrace){",
            "\t\t\t\t\t\t\tseriesLabels.push(dataTrace.name); ",
            "\t\t\t\t\t\t\tjQuery.each(dataTrace.value, function(j, item){",
            "\t\t\t\t\t\t\t\tvar rowObj = {}, pointObj = {};",
            "\t\t\t\t\t\t\t\tpointObj[dataTrace.name] = item.y;",
            "\t\t\t\t\t\t\t\trowObj[item.x] = pointObj;",
            "\t\t\t\t\t\t\t\tdataObj = jQuery.extend(true, dataObj, rowObj);",
            "\t\t\t\t\t\t\t});",
            "\t\t\t\t\t\t});",
            "\t\t\t\t\t\t",
            "\t\t\t\t\t\tjQuery.each(events, function(i, eventTrace){",
            "\t\t\t\t\t\t\tseriesLabels.push(eventTrace.name);",
            "\t\t\t\t\t\t\tjQuery.each(eventTrace.value, function(j, item){",
            "\t\t\t\t\t\t\t\tvar rowObj = {}, pointObj = {};",
            "\t\t\t\t\t\t\t\tpointObj[eventTrace.name] = item.y;",
            "\t\t\t\t\t\t\t\trowObj[item.x] = pointObj;",
            "\t\t\t\t\t\t\t\tdataObj = jQuery.extend(true, dataObj, rowObj);",
            "\t\t\t\t\t\t\t});",
            "\t\t\t\t\t\t});\n",
            "\t\t\t\t\t\tjQuery.each(dataObj, function(x, rowObj){",
            "\t\t\t\t\t\t\tvar row = [];",
            "\t\t\t\t\t\t\tif (config.xAxisMode == 'datetime'){",
            "\t\t\t\t\t\t\t\trow.push(new Date(Number(x)));",
            "\t\t\t\t\t\t\t}else {",
            "\t\t\t\t\t\t\t\trow.push(Number(x));",
            "\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t\tjQuery.each(seriesLabels, function(i, name){",
            "\t\t\t\t\t\t\t\tif (i >= 1){",
            "\t\t\t\t\t\t\t\t\tif (name in rowObj){",
            "\t\t\t\t\t\t\t\t\t\trow.push(rowObj[name]);",
            "\t\t\t\t\t\t\t\t\t}else {",
            "\t\t\t\t\t\t\t\t\t\trow.push(null);",
            "\t\t\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t\t});",
            "\t\t\t\t\t\t\tdataTable.push(row);",
            "\t\t\t\t\t\t});",
            "\t\t\t\t\t}\n",
            "\t\t\t\t\t// Get all events at a certain X axis point",
            "\t\t\t\t\tfunction getEvents(xPoint){",
            "\t\t\t\t\t\tvar text = '';",
            "\t\t\t\t\t\tjQuery.each(events, function(i, eventTrace){",
            "\t\t\t\t\t\t\tvar tempArray = jQuery.grep(eventTrace.value, function(item, j){",
            "\t\t\t\t\t\t\t\treturn (item.x == xPoint);",
            "\t\t\t\t\t\t\t});",
            "\t\t\t\t\t\t\t",
            "\t\t\t\t\t\t\tif (tempArray.length > 0){",
            "\t\t\t\t\t\t\t\ttext += '<b>' + eventTrace.name + '</b>: ';",
            "\t\t\t\t\t\t\t\tjQuery.each(tempArray, function(j, item){",
            "\t\t\t\t\t\t\t\t\tif (j > 0)",
            "\t\t\t\t\t\t\t\t\t\ttext += ', ';",
            "\t\t\t\t\t\t\t\t\ttext += item.text;",
            "\t\t\t\t\t\t\t\t});",
            "\t\t\t            \t\ttext += '<br>';",
            "\t\t\t\t\t\t\t}",
            "\t\t\t\t\t\t});",
            "\t\t\t\t\t\treturn text||'None';",
            "\t\t\t\t\t}",
            "",
            "\t\t\t\t\t// Parse Datetime",
            "\t\t\t\t\tfunction parseDatetime(value){",
            "\t\t\t\t\t\tif (config.xAxisMode == 'datetime')",
            "\t\t\t\t\t\t\treturn new Date(value).toUTCString();",
            "\t\t\t\t\t\telse ",
            "\t\t\t\t\t\t\treturn value;",
            "\t\t\t\t\t}\n",
            "\t\t\t\t\tfunction isEventTrace(name){",
            "\t\t\t\t\t\tvar count = 0;",
            "\t\t\t\t\t\tjQuery.each(events, function(i, eventTrace){",
            "\t\t\t\t\t\t\tif (eventTrace.name == name)",
            "\t\t\t\t\t\t\t\tcount++;",
            "\t\t\t\t\t\t});",
            "\t\t\t\t\t\treturn (count > 0);",
            "\t\t\t\t\t}" };
}
