package org.telegram.charts.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.telegram.charts.ChartsHelper;
import org.telegram.charts.R;
import org.telegram.charts.model.ChartStateData;
import org.telegram.charts.model.LineData;
import org.telegram.charts.view.ChartDrawData;
import org.telegram.charts.view.ChartSelectorListener;

import java.util.Date;

import static org.telegram.charts.ChartsHelper.MIN_SHARE;
import static org.telegram.charts.ChartsHelper.STEP_COUNT;

public class ChartViewportView extends View implements ChartSelectorListener {

    private ChartStateData _chartStateData;

    private final TextPaint _titleTextPaint = new TextPaint();
    private final Point _titleTextPosition = new Point();

    private final Paint _axesPaint = new Paint();
    private final Paint _yValuesPaint = new TextPaint();
    private final Paint _sliderBackgroundPaint = new Paint();
    private final Paint _sliderPaint = new Paint();
    private final Paint _sliderDecorationPaint = new Paint();
    private final Paint _sliderClearPaint = new Paint();
    private final Paint _datePaint = new Paint();

    private int _contentPadding;
    private int _contentTopOffset;
    private int _yLabelsBottomOffset;
    private int _baseline;
    private int _sliderHeight;
    private int _sliderCornerRadius;
    private int _sliderHorizontalBorder;
    private int _sliderSideWidth;
    private int _sliderDecorationHeight;
    private int _sliderDecorationWidth;
    private int _sliderDecorationCornerRadius;
    private int _datesY;
    private int _dateStepX;
    private final Rect _contentRect = new Rect();
    private final RectF _sliderBackgroundRect = new RectF();
    private final Matrix _sliderScaleMatrix = new Matrix();
    private final RectF _sliderRect = new RectF();
    private final RectF _sliderDecorationLeftRect = new RectF();
    private final RectF _sliderDecorationRightRect = new RectF();
    private final Path _sliderClipPath = new Path();
    private final SliderGestureListener _gestureListener = new SliderGestureListener();
    private final GestureDetector _gestureDetector = new GestureDetector(getContext(), _gestureListener);
    private final RectF _sliderInterceptRect = new RectF();
    private final Matrix _linesScaleMatrix = new Matrix();
    private final Date _date = new Date();


    public ChartViewportView(Context context) {
        super(context);
        init();
    }

    public ChartViewportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartViewportView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);

        _contentPadding = getResources().getDimensionPixelSize(R.dimen.chart_viewport_padding);
        _contentTopOffset = getResources().getDimensionPixelSize(R.dimen.chart_viewport_content_top_offset);
        _yLabelsBottomOffset = getResources().getDimensionPixelSize(R.dimen.chart_viewport_y_labels_bottom_offset);
        _sliderHeight = getResources().getDimensionPixelSize(R.dimen.chart_slider_height);
        _sliderCornerRadius = getResources().getDimensionPixelSize(R.dimen.chart_slider_corner_radius);
        _sliderHorizontalBorder = getResources().getDimensionPixelSize(R.dimen.chart_slider_horizontal_border);
        _sliderSideWidth = getResources().getDimensionPixelSize(R.dimen.chart_slider_side_width);
        _sliderDecorationHeight = getResources().getDimensionPixelSize(R.dimen.chart_slider_decoration_height);
        _sliderDecorationWidth = getResources().getDimensionPixelSize(R.dimen.chart_slider_decoration_width);
        _sliderDecorationCornerRadius = _sliderSideWidth / 2;

        _titleTextPaint.setTextSize(getResources().getDimension(R.dimen.chart_title_size));
        _titleTextPaint.setColor(getResources().getColor(R.color.colorTitle));
        _titleTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        _titleTextPaint.setAntiAlias(true);

        int titleTextHeight = (int) ((_titleTextPaint.getFontMetricsInt().descent - _titleTextPaint.getFontMetricsInt().ascent) * 0.75f);
        _titleTextPosition.set(_contentPadding, titleTextHeight + _contentPadding);

        _axesPaint.setColor(getResources().getColor(R.color.colorBackground));
        _axesPaint.setStrokeWidth(getResources().getDimension(R.dimen.chart_viewport_axis_thickness));
        _axesPaint.setAntiAlias(true);

        _yValuesPaint.setTextAlign(Paint.Align.LEFT);
        _yValuesPaint.setTextSize(getResources().getDimension(R.dimen.chart_viewport_labels_size));
        _yValuesPaint.setColor(getResources().getColor(R.color.colorOnSurface));
        _yValuesPaint.setAntiAlias(true);

        _sliderBackgroundPaint.setColor(getResources().getColor(R.color.colorScrollBackground));
        _sliderBackgroundPaint.setAntiAlias(true);

        _sliderPaint.setColor(getResources().getColor(R.color.colorScrollSelector));
        _sliderPaint.setAntiAlias(true);

        _sliderDecorationPaint.setColor(getResources().getColor(R.color.colorChipText));
        _sliderDecorationPaint.setAntiAlias(true);

        _sliderClearPaint.setColor(Color.TRANSPARENT);
        _sliderClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
        _sliderClearPaint.setAntiAlias(true);

        _datePaint.setColor(getResources().getColor(R.color.colorOnSurface));
        _datePaint.setTextAlign(Paint.Align.CENTER);
        _datePaint.setTextSize(getResources().getDimension(R.dimen.chart_viewport_labels_size));
        _datePaint.setAntiAlias(true);

        _gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            _baseline = _contentTopOffset + _chartStateData.getAxisStep() * STEP_COUNT;
            _contentRect.set(
                    _contentPadding, _contentTopOffset,
                    right - _contentPadding, _baseline);
            _sliderRect.set(
                    _contentPadding,
                    bottom - _sliderHeight,
                    _contentPadding + _chartStateData.getSliderWidth(),
                    bottom);
            _sliderDecorationLeftRect.set(
                    _sliderRect.left + _sliderSideWidth * 0.5f - _sliderDecorationWidth * 0.5f,
                    _sliderRect.centerY() - _sliderDecorationHeight * 0.5f,
                    _sliderRect.left + _sliderSideWidth * 0.5f + _sliderDecorationWidth * 0.5f,
                    _sliderRect.centerY() + _sliderDecorationHeight * 0.5f);
            _sliderDecorationRightRect.set(_sliderDecorationLeftRect);
            _sliderDecorationRightRect.offset(_sliderRect.width() - _sliderSideWidth, 0);
            _sliderBackgroundRect.set(
                    _contentRect.left, _sliderRect.top + _sliderHorizontalBorder,
                    _contentRect.right, _sliderRect.bottom - _sliderHorizontalBorder);
            _sliderScaleMatrix.setScale(1, _sliderBackgroundRect.height() / (_chartStateData.getAxisStep() * (STEP_COUNT + 1)));
            _sliderClipPath.addRoundRect(_sliderBackgroundRect, _sliderCornerRadius, _sliderCornerRadius, Path.Direction.CCW);
            _chartStateData.setMaxLines(_baseline);
            _datesY = _baseline + _contentPadding;
            _dateStepX = (_contentRect.width() - _contentPadding * 2) / (STEP_COUNT - 1);
        }
    }

    @Override
    public void onLineSelected(LineData lineData, boolean isSelected) {
        _chartStateData.animateSelection(lineData, isSelected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, getResources().getDimensionPixelSize(R.dimen.chart_viewport_height));
    }

    public void setChartStateData(ChartStateData chartStateData) {
        _chartStateData = chartStateData;
        _chartStateData.addAnimationListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) { postInvalidate(); }
        });
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        tryDraw(canvas);
    }

    private void tryDraw(Canvas canvas) {
        if (canvas != null && shouldDrawContent()) {
            drawTitle(canvas);
            drawAxes(canvas);
            drawLines(canvas);
            drawSlider(canvas);
            drawDatesLeft(canvas);
            //drawDatesRight(canvas);

//
//            switch (_gestureListener._currentScroll) {
//                case SliderGestureListener.RIGHT: {
//                    drawDatesRight(canvas);
//                    break;
//                }
//                case SliderGestureListener.LEFT: {
//                    drawDatesLeft(canvas);
//                    break;
//                }
//                default: {
//                    drawDatesLeft(canvas);
//                    break;
//                }
//            }
        }
    }

    private boolean shouldDrawContent() {
        return _chartStateData != null && !_chartStateData.getChartDrawData().isEmpty();
    }

    private void drawTitle(Canvas canvas) {
        canvas.drawText(_chartStateData.getChartData().getTitle(), _titleTextPosition.x, _titleTextPosition.y, _titleTextPaint);
    }

    private void drawAxes(Canvas canvas) {
        int currentY = _baseline;
        int yValueStep = _chartStateData.getCurrentDisplayStepY();
        int currentValue = 0;

        switch (_chartStateData.getAnimationType()) {
            case ChartStateData.ANIMATION_NONE: {
                if (!_chartStateData.isStateEmpty()) {
                    _axesPaint.setAlpha(255);
                    _yValuesPaint.setAlpha(255);
                    for (int i = 0; i < STEP_COUNT; i++) {
                        canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                        canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);
                        currentY -= _chartStateData.getAxisStep();
                        currentValue += yValueStep;
                    }
                }
                break;
            }
            case ChartStateData.ANIMATION_SIMPLE: {
                _axesPaint.setAlpha(255);
                _yValuesPaint.setAlpha(255);
                for (int i = 0; i < STEP_COUNT; i++) {
                    canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                    canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);
                    currentY -= _chartStateData.getAxisStep();
                    currentValue += yValueStep;
                }
                break;
            }
            case ChartStateData.ANIMATION_COMPLEX: {
                _axesPaint.setAlpha(_chartStateData.getAlphaHide());
                _yValuesPaint.setAlpha(_chartStateData.getAlphaHide());

                for (int i = 1; i < STEP_COUNT; i++) {
                    currentValue = yValueStep * i;
                    currentY = (int) (_baseline - currentValue * _chartStateData.getYStepPx());
                    canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                    canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);
                }

                currentValue = 0;
                currentY = _baseline;
                _axesPaint.setAlpha(255);
                _yValuesPaint.setAlpha(255);
                canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);

                _axesPaint.setAlpha(_chartStateData.getAlphaShow());
                _yValuesPaint.setAlpha(_chartStateData.getAlphaShow());
                int yValueStepPending = _chartStateData.getPendingDisplayStepY();
                for (int i = 1; i < STEP_COUNT; i++) {
                    currentValue = yValueStepPending * i;
                    currentY = (int) (_baseline - currentValue * _chartStateData.getYStepPx());
                    canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                    canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);
                }
                break;
            }
            case ChartStateData.ANIMATION_TO_EMPTY: {
                _axesPaint.setAlpha(_chartStateData.getAlphaHide());
                _yValuesPaint.setAlpha(_chartStateData.getAlphaHide());
                for (int i = 0; i < STEP_COUNT; i++) {
                    canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                    canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);
                    currentY -= _chartStateData.getAxisStep();
                    currentValue += yValueStep;
                }
                break;
            }
            case ChartStateData.ANIMATION_FROM_EMPTY: {
                _axesPaint.setAlpha(_chartStateData.getAlphaShow());
                _yValuesPaint.setAlpha(_chartStateData.getAlphaShow());
                for (int i = 0; i < STEP_COUNT; i++) {
                    canvas.drawLine(_contentRect.left, currentY, _contentRect.right, currentY, _axesPaint);
                    canvas.drawText(ChartsHelper.getReadableYValueString(currentValue), _contentRect.left, currentY - _yLabelsBottomOffset, _yValuesPaint);
                    currentY -= _chartStateData.getAxisStep();
                    currentValue += yValueStep;
                }
                break;
            }
        }
    }

    private void drawLines(Canvas canvas) {
        float share = (_sliderRect.width()) / _sliderBackgroundRect.width();
        float leftShare = (_sliderRect.left) / _sliderBackgroundRect.width();
        float translation = (_sliderBackgroundRect.width()) * (leftShare / share) * -1;

        for (ChartDrawData drawData : _chartStateData.getChartDrawData()) {
            drawData.resetLines(_chartStateData.getChartData().getDates().size() * 4 - 2);
        }

        LineData lineData;
        ChartDrawData drawData;
        for (int i = 0; i < _chartStateData.getChartData().getDates().size(); i++) {
            float currentPrevX = _chartStateData.getXStepPx() * i + _sliderBackgroundRect.left;

            for (int v = 0; v < _chartStateData.getChartData().getLines().size(); v++) {
                lineData = _chartStateData.getChartData().getLines().get(v);
                drawData = _chartStateData.getChartDrawData().get(v);
                float currentPrevY = _baseline - lineData.getValues().get(i) * _chartStateData.getYStepPx();

                drawData.getLines()[i * 4] = currentPrevX;
                drawData.getLines()[i * 4 + 1] = currentPrevY;

                if (i != 0) {
                    drawData.getLines()[i * 4 - 2] = currentPrevX;
                    drawData.getLines()[i * 4 - 1] = currentPrevY;
                }
            }
        }

        canvas.save();
        canvas.translate(translation + _sliderBackgroundRect.left, 0);
        float[] dst = new float[_chartStateData.getChartData().getDates().size() * 4 - 2];
        _linesScaleMatrix.setScale(1f / share, 1);

        for (int v = 0; v < _chartStateData.getChartDrawData().size(); v++) {
            drawData = _chartStateData.getChartDrawData().get(v);
            _linesScaleMatrix.mapPoints(dst, drawData.getLines());
            canvas.drawLines(dst, drawData.getLinePaint());
            if (share == 1) drawData.setMaxLines(drawData.getLines());
        }

        canvas.restore();
    }

    private void drawSlider(Canvas canvas) {
        float[] dst = new float[_chartStateData.getChartData().getDates().size() * 4 - 2];

        canvas.save();
        canvas.translate(0, getHeight() - _sliderHeight);
        ChartDrawData drawData;
        for (int v = 0; v < _chartStateData.getChartDrawData().size(); v++) {
            drawData = _chartStateData.getChartDrawData().get(v);
            _sliderScaleMatrix.mapPoints(dst, drawData.getMaxLines());
            canvas.drawLines(dst, drawData.getLineSliderPaint());
        }
        canvas.restore();

        canvas.save();
        canvas.clipRect(_sliderRect.left + _sliderSideWidth, _sliderBackgroundRect.top,
                _sliderRect.right - _sliderSideWidth, _sliderBackgroundRect.bottom,
                Region.Op.XOR);
        canvas.drawRoundRect(_sliderBackgroundRect, _sliderCornerRadius, _sliderCornerRadius, _sliderBackgroundPaint);
        canvas.drawRoundRect(_sliderRect, _sliderCornerRadius, _sliderCornerRadius, _sliderPaint);
        canvas.restore();

        canvas.drawRoundRect(
                _sliderDecorationLeftRect,
                _sliderDecorationCornerRadius, _sliderDecorationCornerRadius,
                _sliderDecorationPaint);

        canvas.drawRoundRect(
                _sliderDecorationRightRect,
                _sliderDecorationCornerRadius, _sliderDecorationCornerRadius,
                _sliderDecorationPaint);
    }

    private void drawDatesLeft(Canvas canvas) {
        float share = (_sliderRect.width()) / _sliderBackgroundRect.width();
        float leftShare = (_sliderRect.left - _sliderBackgroundRect.left) / _sliderBackgroundRect.width();
        int level = (int) ((1 - share) / MIN_SHARE);
        level = (int) ((1f / MIN_SHARE) - level);
        //if (level == 1) level = 2;
        Date firstDate = _chartStateData.getChartData().getDates().get(0);
        int dateCount;
        if (level == 0) dateCount = STEP_COUNT;
        else dateCount = (int) ((1f / (level * MIN_SHARE)) * STEP_COUNT);
        long millisStep = _chartStateData.getChartData().getDates().get(_chartStateData.getChartData().getDates().size() - 1).getTime() - firstDate.getTime();
        millisStep /= (dateCount - 1);
        float rem = share % MIN_SHARE;
        float progress = (rem / MIN_SHARE);
        float offset = dateCount * (leftShare) * _dateStepX;

        _datePaint.setAlpha((int) (255 * progress));
        _date.setTime(firstDate.getTime());
        for (int i = 0; i < dateCount; i++) {
                canvas.drawText
                        (ChartsHelper.DAY_FORMAT.format(_date),
                                _contentPadding * 2 + i * _dateStepX - offset + (1 - progress) * _dateStepX * 0.5f, _datesY,
                                _datePaint);
            _date.setTime(_date.getTime() + millisStep);
        }

        if (progress != 0 && Math.round(level) != 1) {
            share -= MIN_SHARE;
            level = (int) ((1 - share) / MIN_SHARE);
            level = (int) ((1f / MIN_SHARE) - level);
            if (level == 0) dateCount = STEP_COUNT;
            else dateCount = (int) ((1f / (level * MIN_SHARE)) * STEP_COUNT);
            millisStep = _chartStateData.getChartData().getDates().get(_chartStateData.getChartData().getDates().size() - 1).getTime() - firstDate.getTime();
            millisStep /= (dateCount - 1);
            rem = share % MIN_SHARE;
            progress = 1 - (rem / MIN_SHARE);
            offset = dateCount * leftShare * _dateStepX;
            _datePaint.setAlpha((int) (255 * (progress)));
            _date.setTime(firstDate.getTime());
            for (int i = 0; i < dateCount; i++) {
                canvas.drawText
                        (ChartsHelper.DAY_FORMAT.format(_date),
                                _contentPadding * 2 + i * _dateStepX - offset - (1 - progress) * _dateStepX * 0.5f, _datesY,
                                _datePaint);
                _date.setTime(_date.getTime() + millisStep);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            getParent().requestDisallowInterceptTouchEvent(false);
            _gestureListener.cancelScroll();
        }
        return _gestureDetector.onTouchEvent(event);
    }

    private class SliderGestureListener extends GestureDetector.SimpleOnGestureListener {
        static final int SLIDE = 1;
        static final int LEFT = 2;
        static final int RIGHT = 3;

        private int _currentScroll;

        @Override
        public boolean onDown(MotionEvent e) {
            boolean intercepted = tryInterceptSlide(e) || tryInterceptLeft(e) || tryInterceptRight(e);
            if (intercepted) getParent().requestDisallowInterceptTouchEvent(true);
            else cancelScroll();
            return intercepted;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(ChartViewportView.class.getSimpleName(), "distX: " + distanceX);

            switch (_currentScroll) {
                case SLIDE: {
                    _chartStateData.slideSlider(_sliderRect, _sliderDecorationLeftRect, _sliderDecorationRightRect, -distanceX);
                    _chartStateData.checkMaxAnimation(_sliderRect, _sliderBackgroundRect);
                    invalidate();
                    break;
                }
                case LEFT: {
                    _chartStateData.slideSliderLeft(_sliderRect, _sliderDecorationLeftRect, e2.getX());
                    _chartStateData.checkMaxAnimation(_sliderRect, _sliderBackgroundRect);
                    invalidate();
                    break;
                }
                case RIGHT: {
                    _chartStateData.slideSliderRight(_sliderRect, _sliderDecorationRightRect, e2.getX());
                    _chartStateData.checkMaxAnimation(_sliderRect, _sliderBackgroundRect);
                    invalidate();
                    break;
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        private boolean tryInterceptSlide(MotionEvent event) {
            _sliderInterceptRect.set(_sliderRect);
            _sliderInterceptRect.inset(_sliderSideWidth * 2f, _sliderHorizontalBorder);
            boolean isSlide = _sliderInterceptRect.contains(event.getX(), event.getY());
            if (isSlide) _currentScroll = SLIDE;
            return isSlide;
        }

        private boolean tryInterceptLeft(MotionEvent event) {
            _sliderInterceptRect.set(
                    _sliderRect.left - _sliderSideWidth * 2f, _sliderRect.top,
                    _sliderRect.left + _sliderSideWidth * 3.5f, _sliderRect.bottom);
            boolean isLeft = _sliderInterceptRect.contains(event.getX(), event.getY());
            if (isLeft) _currentScroll = LEFT;
            return isLeft;
        }

        private boolean tryInterceptRight(MotionEvent event) {
            _sliderInterceptRect.set(
                    _sliderRect.right - _sliderSideWidth * 3.5f, _sliderRect.top,
                    _sliderRect.right + _sliderSideWidth * 2f, _sliderRect.bottom);
            boolean isRight = _sliderInterceptRect.contains(event.getX(), event.getY());
            if (isRight) _currentScroll = RIGHT;
            return isRight;
        }

        void cancelScroll() { _currentScroll = 0; }
    }
}
