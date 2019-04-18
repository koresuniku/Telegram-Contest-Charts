package org.telegram.charts.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class LineData implements Parcelable {

    private int _color;
    private String _name;
    private String _label;
    private final List<Integer> _values;

    private int _maxYValue;
    private int _minYValue;

    private boolean _isSelected = true;

    LineData() {
        _values = new ArrayList<>();
        findExtrema(0, _values.size());
    }

    private LineData(Parcel in) {
        _color = in.readInt();
        _name = in.readString();
        _label = in.readString();

        int valuesSize = in.readInt();
        int[] valuesArray = new int[valuesSize];
        in.readIntArray(valuesArray);

        _values = arrayToValues(valuesArray);

        _maxYValue = in.readInt();
        _minYValue = in.readInt();

        _isSelected = in.readByte() != 0;
    }

    public static final Creator<LineData> CREATOR = new Creator<LineData>() {
        @Override
        public LineData createFromParcel(Parcel in) {
            return new LineData(in);
        }

        @Override
        public LineData[] newArray(int size) {
            return new LineData[size];
        }
    };

    public int getColor() {
        return _color;
    }

    public void setColor(int color) {
        _color = color;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    private String getLabel() {
        return _label;
    }

    void setLabel(String label) {
        _label = label;
    }

    public List<Integer> getValues() {
        return _values;
    }

    public boolean isSelected() {
        return _isSelected;
    }

    void setSelected(boolean selected) {
        _isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_color);
        dest.writeString(_name);
        dest.writeString(_label);

        dest.writeInt(getValues().size());
        dest.writeIntArray(valuesToArray());

        dest.writeInt(_maxYValue);
        dest.writeInt(_minYValue);

        dest.writeByte((byte) (_isSelected ? 1 : 0));
    }

    private int[] valuesToArray() {
        int[] result = new int[getValues().size()];
        for (int i = 0; i < getValues().size(); i++) {
            result[i] = getValues().get(i);
        }
        return result;
    }

    private List<Integer> arrayToValues(int[] valuesArray) {
        List<Integer> result = new ArrayList<>();
        for (int yValue : valuesArray) {
            result.add(yValue);
        }
        return result;
    }

    void findExtrema(int start, int end) {
        int currentMax = -1;
        int currentMin = -1;
        boolean first = true;
        for (Integer integer : getValues().subList(start, end)) {
            if (first) {
                currentMin = integer;
                currentMax = integer;
                first = false;
            }
            else if (integer > currentMax) currentMax = integer;
            else if (integer < currentMin) currentMin = integer;
        }
        _maxYValue = currentMax;
        _minYValue = currentMin;
    }

    int getMaxYValue() {
        return _maxYValue;
    }

    int getMinYValue() {
        return _minYValue;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LineData && ((LineData) obj).getLabel().equals(getLabel());
    }
}
