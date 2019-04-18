package org.telegram.charts.model;

import android.content.res.Resources;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Parser {

    public static List<ChartData> getChartsDataFromJson(int jsonResId, Resources resources) {
        String jsonString = getJsonString(jsonResId, resources);
        return getChartDataList(jsonString);
    }

    private static String getJsonString(int jsonResId, Resources resources) {
        final StringBuilder builder = new StringBuilder();
        try{
            final InputStream file = resources.openRawResource(jsonResId);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line;
            while ((line = reader.readLine())!= null) builder.append(line);
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        return builder.toString();
    }


    private static List<ChartData> getChartDataList(String jsonString) {
        final List<ChartData> result = new ArrayList<>();
        try {
            final JSONArray root = new JSONArray(jsonString);
            for (int i = 0; i < root.length(); i++) {
                JSONObject chartObject = root.getJSONObject(i);
                ChartData chartData = parseChartJson(chartObject);
                switch (i) {
                    case 0: chartData.setTitle("Chart №1"); break;
                    case 1: chartData.setTitle("Chart №2"); break;
                    case 2: chartData.setTitle("Chart №3"); break;
                    case 3: chartData.setTitle("Chart №4"); break;
                    case 4: chartData.setTitle("Chart №5"); break;
                }
                chartData.setPosition(i);
                chartData.findExtrema(0, chartData.getDates().size() - 1);
                result.add(chartData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static ChartData parseChartJson(JSONObject chartObject) {
        final ChartData result = new ChartData();
        try {
            JSONArray columnArray = chartObject.getJSONArray("columns");
            JSONObject colorsObject = chartObject.getJSONObject("colors");
            JSONObject namesObject = chartObject.getJSONObject("names");

            LineData lineData;
            for (int i = 0; i < columnArray.length(); i++) {
                JSONArray columnObject = columnArray.getJSONArray(i);
                String label = columnObject.getString(0);
                if (label.equals("x")) {
                    for (int v = 1; v < columnObject.length(); v++) {
                        result.getDates().add(new Date(columnObject.getLong(v)));
                    }
                } else {
                    lineData = new LineData();
                    lineData.setLabel(label);
                    lineData.setColor(Color.parseColor(colorsObject.getString(label)));
                    lineData.setName(namesObject.getString(label));

                    switch (i) {
                        case 1: lineData.setName("Joined"); break;
                        case 2: lineData.setName("Left"); break;
                        case 3: lineData.setName("Document"); break;
                        case 4: lineData.setName("Location"); break;
                    }
                    for (int v = 1; v < columnObject.length(); v++) {
                        lineData.getValues().add(columnObject.getInt(v));
                    }
                    lineData.findExtrema(0, result.getDates().size() - 1);
                    result.getLines().add(lineData);
                }
            }

            result.setWrapMaxYValue(result.getMaxYValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
