package org.telegram.charts;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.charts.model.ChartData;
import org.telegram.charts.model.ChartStateData;
import org.telegram.charts.model.Parser;
import org.telegram.charts.view.ChartView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity  {

    private NightModeHelper _nightModeHelper;
    private LinearLayout _chartsContainer;
    private int _chartsDivider;

    private ArrayList<ChartStateData> _chartDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _nightModeHelper = new NightModeHelper(this, R.style.AppTheme);
        toggleBars(NightModeHelper._uiNightMode != Configuration.UI_MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        _chartsContainer = findViewById(R.id.chartsContainer);
        _chartsDivider = getResources().getDimensionPixelSize(R.dimen.chart_view_divider);

        if (savedInstanceState != null && savedInstanceState.containsKey(ChartsHelper.CHART_STATES_KEY)) {
            _chartDataList = savedInstanceState.getParcelableArrayList(ChartsHelper.CHART_STATES_KEY);
        } else {
            List<ChartData> chartDataList = Parser.getChartsDataFromJson(R.raw.chart_data, getResources());
            List<ChartStateData> chartStateDataList = new ArrayList<>();
            for (ChartData chartData : chartDataList) chartStateDataList.add(new ChartStateData(chartData));
            _chartDataList.addAll(chartStateDataList);
        }

        fillCharts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_theme) {
            _nightModeHelper.toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillCharts() {
        ChartView chartView;
        for (ChartStateData chartStateData : _chartDataList) {
            chartView = new ChartView(this, chartStateData);
            _chartsContainer.addView(chartView);
            ((LinearLayout.LayoutParams) chartView.getLayoutParams()).bottomMargin = _chartsDivider;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ChartsHelper.CHART_STATES_KEY, _chartDataList);
    }

    private void toggleBars(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (on) {
                View view = getWindow().getDecorView();
                view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                    getWindow().setNavigationBarColor(Color.WHITE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        getWindow().setNavigationBarDividerColor(getResources().getColor(R.color.colorBackground, getTheme()));
                    }
                }
            } else {
                View view = getWindow().getDecorView();
                view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    view.setSystemUiVisibility(view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                }
            }
        }
    }
}