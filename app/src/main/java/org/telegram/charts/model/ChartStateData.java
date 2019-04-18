package org.telegram.charts.model;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import org.telegram.charts.ChartsHelper;
import org.telegram.charts.R;
import org.telegram.charts.view.ChartDrawData;

import java.util.ArrayList;
import java.util.List;

import static org.telegram.charts.ChartsHelper.MIN_SHARE;
import static org.telegram.charts.ChartsHelper.STEP_COUNT;

public class ChartStateData implements Parcelable, Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    public static final int ANIMATION_NONE = -1;
    public static final int ANIMATION_SIMPLE = 0;
    public static final int ANIMATION_COMPLEX = 1;
    public static final int ANIMATION_TO_EMPTY = 2;
    public static final int ANIMATION_FROM_EMPTY = 3;

    private final ValueAnimator _valueAnimator = ValueAnimator.ofFloat(0f, 1f);
    private final Interpolator _valueInterpolator = new AccelerateDecelerateInterpolator();
    private final List<ValueAnimator.AnimatorUpdateListener> _animationListeners = new ArrayList<>();
    private final List<ChartDrawData> _chartDrawData = new ArrayList<>();

    private ChartData _chartData;

    private int _currentDisplayMaxY;
    private int _currentDisplayStepY;
    private int _pendingDisplayMaxY;
    private int _pendingDisplayStepY;
    private LineData _pendingLine;
    private int _pendingPosition;
    private int _animationType = ANIMATION_NONE;
    private boolean _isEmpty;

    //Setup
    private int _axisStep;
    private int _sliderMinWidth;
    private int _limitSliderLeft;
    private int _limitSliderRight;
    private float _xStepPx;

    //Deltas
    private float _yStepPx;
    private int _alphaShow;
    private int _alphaHide;
    private int _currentSliderWidth;
    private int _currentStart;
    private int _currentEnd;

    public ChartStateData(ChartData chartData) {
        _chartData = chartData;
        _currentDisplayStepY = ChartsHelper.getYValueStep(_chartData);
        _currentDisplayMaxY = _currentDisplayStepY * (STEP_COUNT - 1);
        _currentStart = 0;
        _currentEnd = chartData.getDates().size() - 1;
        init();
    }

    private ChartStateData(Parcel in) {
        _currentDisplayMaxY = in.readInt();
        _currentDisplayStepY = in.readInt();
        _chartData = in.readParcelable(ChartData.class.getClassLoader());
        _currentStart = in.readInt();
        _currentEnd = in.readInt();
        init();
    }

    private void init() {
        initAnimator();
        setIdle();
    }

    private void initAnimator() {
        _valueAnimator.setDuration(250);
        _valueAnimator.setInterpolator(_valueInterpolator);
        _valueAnimator.addListener(this);
        _valueAnimator.addUpdateListener(this);
    }

    @Override
    public void onAnimationStart(Animator animation) {}

    @Override
    public void onAnimationEnd(Animator animation) {
        finishAnimateSelection();
        setIdle();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {}

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        updateValues(animation);
        for (ValueAnimator.AnimatorUpdateListener listener : _animationListeners) {
            listener.onAnimationUpdate(animation);
        }
    }

    private void updateValues(ValueAnimator animator) {
        float animatedValue = (float) animator.getAnimatedValue();

        _yStepPx = _animationType == ANIMATION_FROM_EMPTY ?
                (float) _axisStep / getCurrentDisplayStepY() :
                (float) _axisStep / (getCurrentDisplayStepY() + getPendingDisplayStepDiff() * animatedValue);

        _alphaHide = Math.round(255 * (1 - animatedValue));
        _alphaShow = Math.round(255 * animatedValue);

        if (_pendingPosition != -1 && _pendingLine != null) {
            _chartDrawData.get(_pendingPosition).applyAlpha(_alphaHide, _alphaShow, _pendingLine.isSelected());
        }
    }

    private void setIdle() {
        _animationType = ANIMATION_NONE;
        _yStepPx = (float) _axisStep / getCurrentDisplayStepY();
    }

    public static final Creator<ChartStateData> CREATOR = new Creator<ChartStateData>() {
        @Override
        public ChartStateData createFromParcel(Parcel in) {
            return new ChartStateData(in);
        }

        @Override
        public ChartStateData[] newArray(int size) {
            return new ChartStateData[size];
        }
    };

    public int getCurrentDisplayStepY() {
        return _currentDisplayStepY;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_currentDisplayMaxY);
        dest.writeInt(_currentDisplayStepY);
        dest.writeParcelable(_chartData, flags);
        dest.writeInt(_currentStart);
        dest.writeInt(_currentEnd);
    }

    public void animateSelection(LineData lineData, boolean isSelected) {
        lineData.setSelected(isSelected);

        if (isAnimating() && pendingLinePresent()) {
            float animatedValue = (float) _valueAnimator.getAnimatedValue();
            if (_animationType == ANIMATION_FROM_EMPTY && _pendingLine.isSelected()) {
                _currentDisplayStepY = ChartsHelper.getYValueStep(_chartData);
                _currentDisplayMaxY = _currentDisplayStepY * (STEP_COUNT - 1);
            } else if (_animationType != ANIMATION_FROM_EMPTY) {
                _currentDisplayStepY = (int) (_currentDisplayStepY + getPendingDisplayStepDiff() * animatedValue);
                _currentDisplayMaxY = (int) (_currentDisplayMaxY + getPendingDisplayMaxDiff() * animatedValue);
            }
            _chartDrawData.get(_pendingPosition).applyAlpha(0, 255, _pendingLine.isSelected());
            _valueAnimator.removeAllUpdateListeners();
            _valueAnimator.removeAllListeners();

            _pendingLine = null;
            _pendingPosition = -1;
            _valueAnimator.end();
            _valueAnimator.addUpdateListener(this);
            _valueAnimator.addListener(this);
        }

        boolean fromEmpty = false;
        boolean toEmpty = false;

        int selectedCount = 0;
        for (LineData ld : _chartData.getLines()) if (ld.isSelected()) selectedCount++;

        if (selectedCount == 0) toEmpty = true;
        else if (selectedCount == 1 && lineData.isSelected()) fromEmpty = true;

        if (fromEmpty) {
            _animationType = ANIMATION_FROM_EMPTY;
            _chartData.findExtrema(_currentStart, _currentEnd);
            _currentDisplayStepY = ChartsHelper.getYValueStep(_chartData);
            _currentDisplayMaxY = _currentDisplayStepY * (STEP_COUNT - 1);
        } else if (toEmpty) {
            _animationType = ANIMATION_TO_EMPTY;
            _pendingDisplayMaxY = _currentDisplayMaxY;
            _pendingDisplayStepY = _currentDisplayStepY;
        } else {
            _chartData.findExtrema(_currentStart, _currentEnd);
            _pendingDisplayStepY = ChartsHelper.getYValueStep(_chartData);
            _pendingDisplayMaxY = _pendingDisplayStepY * (STEP_COUNT - 1);
            _animationType = _pendingDisplayStepY != _currentDisplayStepY ? ANIMATION_COMPLEX : ANIMATION_SIMPLE;
        }

        _pendingLine = lineData;
        _pendingPosition = getChartData().getLines().indexOf(_pendingLine);
        _valueAnimator.start();
    }

    private void animateMaxY() {
        //if (isAnimating()) return;
        if (isAnimating()) {

            float animatedValue = (float) _valueAnimator.getAnimatedValue();
            if (_animationType == ANIMATION_FROM_EMPTY && _pendingLine.isSelected()) {
                _currentDisplayStepY = ChartsHelper.getYValueStep(_chartData);
                _currentDisplayMaxY = _currentDisplayStepY * (STEP_COUNT - 1);
            } else if (_animationType != ANIMATION_FROM_EMPTY) {
                _currentDisplayStepY = (int) (_currentDisplayStepY + getPendingDisplayStepDiff() * animatedValue);
                _currentDisplayMaxY = (int) (_currentDisplayMaxY + getPendingDisplayMaxDiff() * animatedValue);
            }
            _valueAnimator.removeAllUpdateListeners();
            _valueAnimator.removeAllListeners();
            _valueAnimator.end();
            _valueAnimator.addUpdateListener(this);
            _valueAnimator.addListener(this);
        }

        //_chartData.findExtrema(_currentStart, _currentEnd);
        _pendingDisplayStepY = ChartsHelper.getYValueStep(_chartData);
        _pendingDisplayMaxY = _pendingDisplayStepY * (STEP_COUNT - 1);
        _animationType = _pendingDisplayStepY != _currentDisplayStepY ? ANIMATION_COMPLEX : ANIMATION_SIMPLE;
        _valueAnimator.start();
    }

    private void finishAnimateSelection() {
        if (_animationType == ANIMATION_FROM_EMPTY) {
            _currentDisplayStepY = ChartsHelper.getYValueStep(_chartData);
            _currentDisplayMaxY = _currentDisplayStepY * (STEP_COUNT - 1);
        } else {
            _currentDisplayStepY = _pendingDisplayStepY;
            _currentDisplayMaxY = _pendingDisplayMaxY;
        }
        _pendingDisplayStepY = 0;
        _pendingDisplayMaxY = 0;
        _pendingLine = null;
        _pendingPosition = -1;

        _isEmpty = _animationType == ANIMATION_TO_EMPTY;
    }

    private int getPendingDisplayStepDiff() {
        return _pendingDisplayStepY - _currentDisplayStepY;
    }

    private int getPendingDisplayMaxDiff() {
        return _pendingDisplayMaxY - _currentDisplayMaxY;
    }

    public int getPendingDisplayStepY() {
        return _pendingDisplayStepY;
    }

    public ChartData getChartData() {
        return _chartData;
    }

    public int getAnimationType() {
        return _animationType;
    }

    public void addAnimationListener(ValueAnimator.AnimatorUpdateListener listener) {
        _animationListeners.add(listener);
    }

    private boolean isAnimating() { return _valueAnimator.isRunning(); }

    public float getYStepPx() {
        return _yStepPx;
    }

    public int getAlphaShow() {
        return _alphaShow;
    }

    public int getAlphaHide() {
        return _alphaHide;
    }

    public boolean isStateEmpty() {
        return _isEmpty;
    }

    private boolean pendingLinePresent() { return _pendingPosition != -1 && _pendingLine != null; }

    public void setup(Resources resources) {
        _axisStep = resources.getDimensionPixelSize(R.dimen.chart_viewport_axis_step);
        _limitSliderLeft = resources.getDimensionPixelSize(R.dimen.chart_viewport_padding);
        _limitSliderRight = Resources.getSystem().getDisplayMetrics().widthPixels - _limitSliderLeft;
        _sliderMinWidth = (int) ((_limitSliderRight - _limitSliderLeft) * MIN_SHARE);
        _currentSliderWidth = _limitSliderRight - _limitSliderLeft;
        _xStepPx = ((float) (_limitSliderRight - _limitSliderLeft) / (getChartData().getDates().size() - 1));

        _chartDrawData.clear();
        int highlightColor = resources.getColor(R.color.colorChipText);
        int borderStroke = resources.getDimensionPixelSize(R.dimen.chart_viewport_line_width);
        float labelsSize = resources.getDimension(R.dimen.chart_selector_view_labels_size);
        for (LineData lineData : _chartData.getLines()) {
            _chartDrawData.add(new ChartDrawData(lineData, borderStroke, labelsSize, highlightColor));
        }

        setIdle();
    }

    public void setMaxLines(int baseline) {
        for (ChartDrawData drawData : getChartDrawData()) {
            drawData.setMaxLines(new float[getChartData().getDates().size() * 4 - 2]);
        }

        LineData lineData;
        ChartDrawData drawData;
        for (int i = 0; i < getChartData().getDates().size(); i++) {
            float currentPrevX = getXStepPx() * i + _limitSliderLeft;

            for (int v = 0; v < getChartData().getLines().size(); v++) {
                lineData = getChartData().getLines().get(v);
                drawData = getChartDrawData().get(v);
                float currentPrevY = baseline - lineData.getValues().get(i) * ((float) _axisStep / getChartData().getWrapMaxYValue());

                drawData.getMaxLines()[i * 4] = currentPrevX;
                drawData.getMaxLines()[i * 4 + 1] = currentPrevY;

                if (i != 0) {
                    drawData.getMaxLines()[i * 4 - 2] = currentPrevX;
                    drawData.getMaxLines()[i * 4 - 1] = currentPrevY;
                }
            }
        }
    }

    public List<ChartDrawData> getChartDrawData() {
        return _chartDrawData;
    }

    public int getAxisStep() {
        return _axisStep;
    }

    public float getXStepPx() {
        return _xStepPx;
    }

    public int getSliderWidth() { return _currentSliderWidth; }

    public void slideSlider(RectF slider, RectF decorationLeft, RectF decorationRight, float dx) {
        if (slider.right + dx > _limitSliderRight) {
            dx = _limitSliderRight - slider.right;
        } else if (slider.left + dx < _limitSliderLeft) {
            dx = _limitSliderLeft - slider.left;
        }

        slider.offset(dx, 0);
        decorationLeft.offset(dx, 0);
        decorationRight.offset(dx, 0);
    }

    public void slideSliderLeft(RectF slider, RectF decorationLeft, float eventX) {
        float dx = slider.left - eventX;
        if (slider.left - dx > slider.right - _sliderMinWidth) {
            dx = slider.left - (slider.right - _sliderMinWidth);
        } else if (slider.left - dx < _limitSliderLeft) {
            dx = slider.left - _limitSliderLeft;
        }
        slider.set(slider.left - dx, slider.top, slider.right, slider.bottom);
        decorationLeft.offset(-dx, 0);
    }

    public void slideSliderRight(RectF slider, RectF decorationRight, float eventX) {
        float dx = slider.right - eventX;
        if (slider.right - dx > _limitSliderRight) {
            dx = slider.right - _limitSliderRight;
        } else if (slider.right - dx < slider.left + _sliderMinWidth) {
            dx = slider.right - (slider.left + _sliderMinWidth);
        }
        slider.set(slider.left, slider.top, slider.right - dx, slider.bottom);
        decorationRight.offset(-dx, 0);
    }

    public void checkMaxAnimation(RectF sliderRect, RectF sliderBackgroundRect) {
        float share = (sliderRect.width()) / sliderBackgroundRect.width();
        float leftShare = (sliderRect.left - sliderBackgroundRect.left) / sliderBackgroundRect.width();
        _currentStart = (int) (leftShare * (getChartData().getDates().size() - 1));
        _currentEnd = (int) ((leftShare + share) * (getChartData().getDates().size()));

        int prevMaxY = _chartData.getMaxYValue();
        _chartData.findExtrema(_currentStart, _currentEnd);

        if (prevMaxY != _chartData.getMaxYValue()) {
            Log.d(ChartStateData.class.getSimpleName(), "new max y");
            animateMaxY();
        }
    }
}
