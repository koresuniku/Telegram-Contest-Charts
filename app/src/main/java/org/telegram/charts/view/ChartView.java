package org.telegram.charts.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.telegram.charts.R;
import org.telegram.charts.model.ChartStateData;

public class ChartView extends LinearLayout {

    private ChartStateData _chartStateData;

    public ChartView(Context context) {
        super(context);
        setup();
    }

    public ChartView(Context context, ChartStateData chartStateData) {
        super(context);
        _chartStateData = chartStateData;
        setup();
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        inflate(getContext(), R.layout.view_chart, this);

        setOrientation(VERTICAL);

        final ChartViewportView chartViewportView = findViewById(R.id.chartViewportView);
        final ChartSelectorView chartSelectorView = findViewById(R.id.chartSelectorView);

        if (_chartStateData != null) {
            _chartStateData.setup(getResources());
            chartViewportView.setChartStateData(_chartStateData);
            chartSelectorView.setChartStateData(_chartStateData);
            chartSelectorView.setChartSelectorListener(chartViewportView);
        }

        setBackgroundColor(getContext().getResources().getColor(R.color.colorSurface));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(getResources().getDimension(R.dimen.chart_view_elevation));
        }
    }
}
