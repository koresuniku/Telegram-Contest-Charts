package org.telegram.charts.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChartData implements Parcelable {

    private final List<LineData> _lines;
    private final List<Date> _dates;
    private String _title;
    private int _position;

    private int _maxYValue;
    private int _minYValue;

    private int _wrapMaxYValue;

    ChartData() {
        _lines = new ArrayList<>();
        _dates = new ArrayList<>();
    }

    private ChartData(Parcel in) {
        _lines = in.createTypedArrayList(LineData.CREATOR);

        int valuesSize = in.readInt();
        long[] dates = new long[valuesSize];
        in.readLongArray(dates);
        _dates = millisToDates(dates);

        _title = in.readString();
        _maxYValue = in.readInt();
        _minYValue = in.readInt();
        _position = in.readInt();
    }

    public static final Creator<ChartData> CREATOR = new Creator<ChartData>() {
        @Override
        public ChartData createFromParcel(Parcel in) {
            return new ChartData(in);
        }

        @Override
        public ChartData[] newArray(int size) {
            return new ChartData[size];
        }
    };

    public List<LineData> getLines() {
        return _lines;
    }

    public List<Date> getDates() {
        return _dates;
    }

    private long[] datesToMillis() {
        long[] result = new long[_dates.size()];
        for (int i = 0; i < _dates.size(); i++) {
            result[i] = _dates.get(i).getTime();
        }
        return result;
    }

    private List<Date> millisToDates(long[] millisArray) {
        List<Date> result = new ArrayList<>();
        for (long dateMillis : millisArray) {
            result.add(new Date(dateMillis));
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(_lines);

        dest.writeInt(_dates.size());
        dest.writeLongArray(datesToMillis());

        dest.writeString(_title);
        dest.writeInt(_maxYValue);
        dest.writeInt(_minYValue);
        dest.writeInt(_position);
    }

    public String getTitle() {
        return _title;
    }

    void setTitle(String title) {
        _title = title;
    }

    public int getMaxYValue() {
        return _maxYValue;
    }

    public int getMinYValue() {
        return _minYValue;
    }

    void findExtrema(int start, int end) {
        List<Integer> maximas = new ArrayList<>();
        List<Integer> minimas = new ArrayList<>();
        for (LineData lineData : getLines()) {
            lineData.findExtrema(start, end);
            if (lineData.isSelected()) {
                maximas.add(lineData.getMaxYValue());
                minimas.add(lineData.getMinYValue());
            }
        }
        if (!maximas.isEmpty() && !minimas.isEmpty()) {
            _maxYValue = Collections.max(maximas);
            _minYValue = Collections.min(minimas);

            if (start == 0 && end == getDates().size() - 1) {
                _wrapMaxYValue = _maxYValue;
            }
        } else {
            _maxYValue = 0;
            _minYValue = 0;
        }
    }

    void setPosition(int position) {
        _position = position;
    }

    int getWrapMaxYValue() {
        return _wrapMaxYValue;
    }

    void setWrapMaxYValue(int _wrapMaxYValue) {
        this._wrapMaxYValue = _wrapMaxYValue;
    }
}
