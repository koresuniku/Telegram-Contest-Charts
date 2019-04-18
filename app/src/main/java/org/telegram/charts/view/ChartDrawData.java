package org.telegram.charts.view;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;

import org.telegram.charts.model.LineData;

public class ChartDrawData {

    private final Paint _defaultPaint = new Paint();
    private final Paint _unsetPaint = new Paint();
    private final TextPaint _labelDefaultPaint = new TextPaint();
    private final TextPaint _labelUnsetPaint = new TextPaint();
    private final Paint _linePaint = new Paint();
    private final Paint _lineSliderPaint = new Paint();
    private float[] _lines;
    private float[] _maxLines;

    public ChartDrawData(LineData lineData, int strokeWidth, float labelsTextSize, int highlightColor) {
        _defaultPaint.setStyle(Paint.Style.FILL);
        _defaultPaint.setColor(lineData.getColor());
        _defaultPaint.setAntiAlias(true);

        _unsetPaint.setStyle(Paint.Style.STROKE);
        _unsetPaint.setStrokeWidth(strokeWidth);
        _unsetPaint.setColor(lineData.getColor());
        _unsetPaint.setAntiAlias(true);

        _labelDefaultPaint.setTextAlign(Paint.Align.RIGHT);
        _labelDefaultPaint.setTextSize(labelsTextSize);
        _labelDefaultPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) _labelDefaultPaint.setLetterSpacing(0.0125f);
        _labelDefaultPaint.setColor(highlightColor);
        _labelDefaultPaint.setAntiAlias(true);

        _labelUnsetPaint.set(_labelDefaultPaint);
        _labelUnsetPaint.setTextAlign(Paint.Align.CENTER);
        _labelUnsetPaint.setColor(lineData.getColor());

        _linePaint.setStrokeWidth(strokeWidth);
        _linePaint.setStrokeCap(Paint.Cap.ROUND);
        _linePaint.setStrokeJoin(Paint.Join.ROUND);
        _linePaint.setColor(lineData.getColor());
        _linePaint.setAntiAlias(true);

        _lineSliderPaint.setStrokeWidth(strokeWidth * 0.5f);
        _lineSliderPaint.setColor(lineData.getColor());
        _linePaint.setAntiAlias(true);


    }

    public Paint getDefaultPaint() {
        return _defaultPaint;
    }

    public Paint getUnsetPaint() {
        return _unsetPaint;
    }

    public TextPaint getLabelDefaultPaint() {
        return _labelDefaultPaint;
    }

    public TextPaint getLabelUnsetPaint() {
        return _labelUnsetPaint;
    }

    public void applyAlpha(int alphaHide, int alphaShow, boolean isSelected) {
        if (isSelected) {
            _defaultPaint.setAlpha(alphaShow);
            _labelDefaultPaint.setAlpha(alphaShow);
            _unsetPaint.setAlpha(alphaHide);
            _labelUnsetPaint.setAlpha(alphaHide);
            _linePaint.setAlpha(alphaShow);
            _lineSliderPaint.setAlpha(alphaShow);
        } else {
            _defaultPaint.setAlpha(alphaHide);
            _labelDefaultPaint.setAlpha(alphaHide);
            _unsetPaint.setAlpha(alphaShow);
            _labelUnsetPaint.setAlpha(alphaShow);
            _linePaint.setAlpha(alphaHide);
            _lineSliderPaint.setAlpha(alphaHide);
        }
    }

    public float[] getLines() {
        return _lines;
    }

    public void resetLines(int size) {
        _lines = new float[size];
    }

    public Paint getLinePaint() {
        return _linePaint;
    }

    public Paint getLineSliderPaint() {
        return _lineSliderPaint;
    }

    public float[] getMaxLines() {
        return _maxLines;
    }

    public void setMaxLines(float[] _maxLines) {
        this._maxLines = _maxLines;
    }
}
