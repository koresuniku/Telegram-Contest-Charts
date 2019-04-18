package org.telegram.charts.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import org.telegram.charts.R;
import org.telegram.charts.model.ChartStateData;
import org.telegram.charts.model.LineData;

import java.util.ArrayList;
import java.util.List;

public class ChartSelectorView extends View {

    private ChartStateData _chartStateData;
    private ChartSelectorListener _chartSelectorListener;

    private int _chartMargin;
    private int _cornerRadius;
    private int _gapHorizontal;
    private int _gapVertical;
    private int _chipInset;
    private int _halfBorderStroke;

    private final Paint _checkMarkPaint = new Paint();
    private final Paint _highlightPaint = new Paint();

    private final List<RectF> _chips = new ArrayList<>();
    private int _cornerDiameter;
    private int _width;
    private int _height;
    private float _labelTextOffset;
    private RectF _pressedChip;
    private final RectF _chipSelectedBordered = new RectF();

    private final float[] _checkMarkPath = new float[8];
    private final float[] _checkMarkOffsets = new float[5];

    private final ValueAnimator _highlightAnimator = ObjectAnimator.ofFloat(0f, 0.2f);

    public ChartSelectorView(Context context) {
        super(context);
        init();
    }

    public ChartSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        //setClickable(true);

        _chartMargin = getResources().getDimensionPixelSize(R.dimen.chart_viewport_padding);
        _cornerRadius = getResources().getDimensionPixelSize(R.dimen.chart_selector_view_corner_radius);
        _gapHorizontal = getResources().getDimensionPixelSize(R.dimen.chart_selector_view_gap_horizontal);
        _gapVertical = getResources().getDimensionPixelSize(R.dimen.chart_selector_view_gap_vertical);
        _chipInset = getResources().getDimensionPixelSize(R.dimen.chart_selector_view_chip_inset);
        int highlightColor = getResources().getColor(R.color.colorChipText);
        _halfBorderStroke =getResources().getDimensionPixelSize(R.dimen.chart_viewport_line_width) / 2;

        _cornerDiameter = _cornerRadius * 2;

        _highlightPaint.setStyle(Paint.Style.FILL);
        _highlightPaint.setColor(getResources().getColor(R.color.colorChipHighlight));
        _highlightPaint.setAntiAlias(true);

        _checkMarkPaint.setColor(highlightColor);
        _checkMarkPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics()));
        _checkMarkPaint.setStyle(Paint.Style.STROKE);
        _checkMarkPaint.setStrokeCap(Paint.Cap.ROUND);
        _checkMarkPaint.setStrokeJoin(Paint.Join.ROUND);
        _checkMarkPaint.setAntiAlias(true);

        initCheckMark();

        //_gestureDetector = new GestureDetector(getContext(), _gestureListener);

        _highlightAnimator.setDuration(250);
        _highlightAnimator.setInterpolator(new LinearInterpolator());
        _highlightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (_pressedChip != null) {
                    postInvalidate(
                            (int) _pressedChip.left, (int) _pressedChip.top,
                            (int) _pressedChip.right, (int) _pressedChip.bottom);
                }
            }
        });
    }

    private void initCheckMark() {
        _checkMarkOffsets[0] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.5f, getResources().getDisplayMetrics());
        _checkMarkOffsets[1] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics());
        _checkMarkOffsets[2] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        _checkMarkOffsets[3] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7f, getResources().getDisplayMetrics());
        _checkMarkOffsets[4] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
    }

    public void setChartStateData(ChartStateData chartStateData) {
        _chartStateData = chartStateData;

        if (!_chartStateData.getChartDrawData().isEmpty()) {
            Paint paint = _chartStateData.getChartDrawData().get(0).getLabelDefaultPaint();
            _labelTextOffset = (float) (paint.getFontMetricsInt().descent - paint.getFontMetricsInt().ascent) * 0.3f;
        }

        _chartStateData.addAnimationListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (_pressedChip != null) {
                    postInvalidate(
                            (int) _pressedChip.left, (int) _pressedChip.top,
                            (int) _pressedChip.right, (int) _pressedChip.bottom);
                }
            }
        });

        measureContents();
    }

    private void measureContents() {
        _chips.clear();
        int limit = getResources().getDisplayMetrics().widthPixels - _chartMargin;
        int lines = 1;

        int currentX = _chartMargin;
        int currentY = 0;
        for (int i = 0; i < _chartStateData.getChartData().getLines().size(); i++) {
            LineData lineData = _chartStateData.getChartData().getLines().get(i);
            Paint paint = _chartStateData.getChartDrawData().get(i).getLabelDefaultPaint();
            int chipWidth = (int) (_cornerDiameter + paint.measureText(lineData.getName()) + _cornerRadius - _chipInset);
            int endX;
            if (currentX + _gapHorizontal + chipWidth > limit) {
                currentY += _cornerDiameter + _gapVertical;
                currentX = _chartMargin;
                lines++;
            }

            endX = currentX + chipWidth;
            _chips.add(new RectF(currentX, currentY, endX, currentY + _cornerDiameter));
            currentX = endX + _gapHorizontal;
        }

        _width = getResources().getDisplayMetrics().widthPixels;
        _height = lines * _cornerDiameter + (lines - 1) * _gapVertical;
        setMeasuredDimension(_width, _height);

        requestLayout();
        invalidate();
    }

    public void setChartSelectorListener(ChartSelectorListener chartSelectorListener) {
        _chartSelectorListener = chartSelectorListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(_width, _height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (_chartStateData.getChartData().getLines() != null && !_chartStateData.getChartData().getLines().isEmpty() && !_chips.isEmpty()) {
            List<LineData> lines = _chartStateData.getChartData().getLines();
            RectF chip;
            LineData lineData;
            ChartDrawData chartDrawData;
            for (int i = 0; i < lines.size(); i++) {
                chip = _chips.get(i);
                lineData = lines.get(i);
                chartDrawData = _chartStateData.getChartDrawData().get(i);

                drawChipUnset(canvas, chip, lineData.getName(), chartDrawData);
                drawChipDefault(canvas, chip, lineData.getName(), chartDrawData);
            }
            if (_pressedChip != null) {
                _highlightPaint.setAlpha((int) ((float) _highlightAnimator.getAnimatedValue() * 255));
                canvas.drawRoundRect(_pressedChip, _cornerRadius, _cornerRadius, _highlightPaint);
            }
        }
    }

    private void drawChipUnset(Canvas canvas, RectF chip, String name, ChartDrawData chartDrawData) {
        _chipSelectedBordered.set(chip);
        _chipSelectedBordered.inset(_halfBorderStroke, _halfBorderStroke);

        canvas.drawRoundRect(_chipSelectedBordered, _cornerRadius, _cornerRadius, chartDrawData.getUnsetPaint());
        canvas.drawText(name, chip.centerX(), chip.top + _cornerRadius + _labelTextOffset, chartDrawData.getLabelUnsetPaint());
    }

    private void drawChipDefault(Canvas canvas, RectF chip, String name, ChartDrawData chartDrawData) {
        canvas.drawRoundRect(chip, _cornerRadius, _cornerRadius, chartDrawData.getDefaultPaint());
        canvas.drawText(name,
                chip.right - _cornerRadius + _chipInset,
                chip.top + _cornerRadius + _labelTextOffset,
                chartDrawData.getLabelDefaultPaint());

        drawCheckMark(canvas,
                chip.left + _cornerRadius,
                chip.top + _cornerRadius,
                chartDrawData.getLabelDefaultPaint());
    }

    private void drawCheckMark(Canvas canvas, float centerX, float centerY, Paint paint) {
        _checkMarkPath[0] = centerX - _checkMarkOffsets[0];
        _checkMarkPath[1] = centerY + _checkMarkOffsets[1];
        _checkMarkPath[2] = centerX;
        _checkMarkPath[3] = centerY + _checkMarkOffsets[2];

        _checkMarkPath[4] = _checkMarkPath[2];
        _checkMarkPath[5] = _checkMarkPath[3];
        _checkMarkPath[6] = centerX + _checkMarkOffsets[3];
        _checkMarkPath[7] = centerY - _checkMarkOffsets[4];

        _checkMarkPaint.setAlpha(paint.getAlpha());
        canvas.drawLines(_checkMarkPath, _checkMarkPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                for (RectF chip : _chips) {
                    if (chip.contains(event.getX(), event.getY())) {
                        _pressedChip = chip;
                        _highlightAnimator.start();
                        return true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (_pressedChip != null) {
                    LineData lineData = _chartStateData.getChartData().getLines().get(_chips.indexOf(_pressedChip));
                    _chartSelectorListener.onLineSelected(lineData, !lineData.isSelected());
                }
            }
            case MotionEvent.ACTION_CANCEL: {
                if (_pressedChip != null) {
                    _highlightAnimator.reverse();
                }
            }

        }
        return false;
    }
}
