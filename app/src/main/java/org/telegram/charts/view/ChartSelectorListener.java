package org.telegram.charts.view;

import org.telegram.charts.model.LineData;

public interface ChartSelectorListener {
    void onLineSelected(LineData lineData, boolean isSelected);
}
