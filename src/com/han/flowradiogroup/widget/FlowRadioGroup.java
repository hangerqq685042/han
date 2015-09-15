package com.han.flowradiogroup.widget;

import java.util.ArrayList;
import java.util.List;

import com.han.flowradiogroup.R;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;



/* this layout group can be used as RadioGroup,or in other words,instead of RadioGroup!
 * 
 * 1.use FowRadioGroup.LayoutParams instead of other LayoutParams while use FowRadioGroup
 * 
 * 
 * Created by han on 2013/5/5.
 *
 * */
public class FlowRadioGroup extends ViewGroup {
	
	public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int LAYOUT_DIRECTION_LTR = 0;
    public static final int LAYOUT_DIRECTION_RTL = 1;

    private final RadioConfiguration config;
    List<RadioLineDefinition> lines = new ArrayList<RadioLineDefinition>();
    
    
    

    private int mCheckedId = -1;
	// tracks children radio buttons checked state
	private CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
	// when true, mOnCheckedChangeListener discards events
	private boolean mProtectFromCheckedChange = false;
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private PassThroughHierarchyChangeListener mPassThroughListener;

	// 存放当前的radioButton
	private ArrayList<RadioButton> radioButtons;
    
    

    public FlowRadioGroup(Context context) {
        super(context);
        this.config = new RadioConfiguration(context, null);
        
        setOrientation(VERTICAL);
		init();
        
    }

    public FlowRadioGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.config = new RadioConfiguration(context, attributeSet);
        
        init();
        
    }

    public FlowRadioGroup(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.config = new RadioConfiguration(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingRight() - this.getPaddingLeft();
        final int sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();
        final int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        final int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        final int controlMaxLength = this.config.getOrientation() == HORIZONTAL ? sizeWidth : sizeHeight;
        final int controlMaxThickness = this.config.getOrientation() == HORIZONTAL ? sizeHeight : sizeWidth;
        final int modeLength = this.config.getOrientation() == HORIZONTAL ? modeWidth : modeHeight;
        final int modeThickness = this.config.getOrientation() == HORIZONTAL ? modeHeight : modeWidth;

        lines.clear();
        RadioLineDefinition currentLine = new RadioLineDefinition(controlMaxLength, config);
        lines.add(currentLine);

        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );

            lp.clearCalculatedFields(this.config.getOrientation());
            if (this.config.getOrientation() == FlowRadioGroup.HORIZONTAL) {
                lp.setLength(child.getMeasuredWidth());
                lp.setThickness(child.getMeasuredHeight());
            } else {
                lp.setLength(child.getMeasuredHeight());
                lp.setThickness(child.getMeasuredWidth());
            }

            boolean newLine = lp.newLine || (modeLength != MeasureSpec.UNSPECIFIED && !currentLine.canFit(child));
            if (newLine) {
                currentLine = new RadioLineDefinition(controlMaxLength, config);
                if (this.config.getOrientation() == VERTICAL && this.config.getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                    lines.add(0, currentLine);
                } else {
                    lines.add(currentLine);
                }
            }

            if (this.config.getOrientation() == HORIZONTAL && this.config.getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                currentLine.addView(0, child);
            } else {
                currentLine.addView(child);
            }
        }

        this.calculateLinesAndChildPosition(lines);

        int contentLength = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            RadioLineDefinition l = lines.get(i);
            contentLength = Math.max(contentLength, l.getLineLength());
        }
        int contentThickness = currentLine.getLineStartThickness() + currentLine.getLineThickness();

        int realControlLength = this.findSize(modeLength, controlMaxLength, contentLength);
        int realControlThickness = this.findSize(modeHeight, controlMaxThickness, contentThickness);

        this.applyGravityToLines(lines, realControlLength, realControlThickness);

        for (int i = 0; i < linesCount; i++) {
            RadioLineDefinition line = lines.get(i);
            this.applyGravityToLine(line);
            this.applyPositionsToViews(line);
        }

        /* need to take padding into account */
        int totalControlWidth = this.getPaddingLeft() + this.getPaddingRight();
        int totalControlHeight = this.getPaddingBottom() + this.getPaddingTop();
        if (this.config.getOrientation() == HORIZONTAL) {
            totalControlWidth += contentLength;
            totalControlHeight += contentThickness;
        } else {
            totalControlWidth += contentThickness;
            totalControlHeight += contentLength;
        }
        this.setMeasuredDimension(resolveSize(totalControlWidth, widthMeasureSpec), resolveSize(totalControlHeight, heightMeasureSpec));
    }

    private int findSize(int modeSize, int controlMaxSize, int contentSize) {
        int realControlLength;
        switch (modeSize) {
            case MeasureSpec.UNSPECIFIED:
                realControlLength = contentSize;
                break;
            case MeasureSpec.AT_MOST:
                realControlLength = Math.min(contentSize, controlMaxSize);
                break;
            case MeasureSpec.EXACTLY:
                realControlLength = controlMaxSize;
                break;
            default:
                realControlLength = contentSize;
                break;
        }
        return realControlLength;
    }

    private void calculateLinesAndChildPosition(List<RadioLineDefinition> lines) {
        int prevLinesThickness = 0;
        final int linesCount = lines.size();
        for (int i = 0; i < linesCount; i++) {
            final RadioLineDefinition line = lines.get(i);
            line.addLineStartThickness(prevLinesThickness);
            prevLinesThickness += line.getLineThickness();
            int prevChildThickness = 0;
            final List<View> childViews = line.getViews();
            final int numChildViews = childViews.size();
            for (int j = 0; j < numChildViews; j++) {
                View child = childViews.get(j);
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                layoutParams.setInlineStartLength(prevChildThickness);
                prevChildThickness += layoutParams.getLength() + layoutParams.getSpacingLength();
            }
        }
    }

    private void applyPositionsToViews(RadioLineDefinition line) {
        final List<View> childViews = line.getViews();
        final int childCount = childViews.size();
        for (int i = 0; i < childCount; i++) {
            final View child = childViews.get(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (this.config.getOrientation() == HORIZONTAL) {
                layoutParams.setPosition(
                        this.getPaddingLeft() + line.getLineStartLength() + layoutParams.getInlineStartLength(),
                        this.getPaddingTop() + line.getLineStartThickness() + layoutParams.getInlineStartThickness());
                child.measure(
                        MeasureSpec.makeMeasureSpec(layoutParams.getLength(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(layoutParams.getThickness(), MeasureSpec.EXACTLY)
                );
            } else {
                layoutParams.setPosition(
                        this.getPaddingLeft() + line.getLineStartThickness() + layoutParams.getInlineStartThickness(),
                        this.getPaddingTop() + line.getLineStartLength() + layoutParams.getInlineStartLength());
                child.measure(
                        MeasureSpec.makeMeasureSpec(layoutParams.getThickness(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(layoutParams.getLength(), MeasureSpec.EXACTLY)
                );
            }
        }
    }

    private void applyGravityToLines(List<RadioLineDefinition> lines, int realControlLength, int realControlThickness) {
        final int linesCount = lines.size();
        if (linesCount <= 0) {
            return;
        }

        final int totalWeight = linesCount;
        RadioLineDefinition lastLine = lines.get(linesCount - 1);
        int excessThickness = realControlThickness - (lastLine.getLineThickness() + lastLine.getLineStartThickness());
        int excessOffset = 0;
        for (int i = 0; i < linesCount; i++) {
            final RadioLineDefinition child = lines.get(i);
            int weight = 1;
            int gravity = this.getGravity();
            int extraThickness = Math.round(excessThickness * weight / totalWeight);

            final int childLength = child.getLineLength();
            final int childThickness = child.getLineThickness();

            Rect container = new Rect();
            container.top = excessOffset;
            container.left = 0;
            container.right = realControlLength;
            container.bottom = childThickness + extraThickness + excessOffset;

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraThickness;
            child.addLineStartLength(result.left);
            child.addLineStartThickness(result.top);
            child.setLength(result.width());
            child.setThickness(result.height());
        }
    }

    private void applyGravityToLine(RadioLineDefinition line) {
        final List<View> views = line.getViews();
        final int viewCount = views.size();
        if (viewCount <= 0) {
            return;
        }

        float totalWeight = 0;
        for (int i = 0; i < viewCount; i++) {
            final View prev = views.get(i);
            LayoutParams plp = (LayoutParams) prev.getLayoutParams();
            totalWeight += this.getWeight(plp);
        }

        View lastChild = views.get(viewCount - 1);
        LayoutParams lastChildLayoutParams = (LayoutParams) lastChild.getLayoutParams();
        int excessLength = line.getLineLength() - (lastChildLayoutParams.getLength() + lastChildLayoutParams.getInlineStartLength());
        int excessOffset = 0;
        for (int i = 0; i < viewCount; i++) {
            final View child = views.get(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

            float weight = this.getWeight(layoutParams);
            int gravity = this.getGravity(layoutParams);
            int extraLength = Math.round(excessLength * weight / totalWeight);

            final int childLength = layoutParams.getLength() + layoutParams.getSpacingLength();
            final int childThickness = layoutParams.getThickness() + layoutParams.getSpacingThickness();

            Rect container = new Rect();
            container.top = 0;
            container.left = excessOffset;
            container.right = childLength + extraLength + excessOffset;
            container.bottom = line.getLineThickness();

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraLength;
            layoutParams.setInlineStartLength(result.left + layoutParams.getInlineStartLength());
            layoutParams.setInlineStartThickness(result.top);
            layoutParams.setLength(result.width() - layoutParams.getSpacingLength());
            layoutParams.setThickness(result.height() - layoutParams.getSpacingThickness());
        }
    }

    private int getGravity(LayoutParams lp) {
        return lp.gravitySpecified() ? lp.gravity : this.config.getGravity();
    }

    private float getWeight(LayoutParams lp) {
        return lp.weightSpecified() ? lp.weight : this.config.getWeightDefault();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = this.getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x + lp.leftMargin, lp.y + lp.topMargin,
                    lp.x + lp.leftMargin + child.getMeasuredWidth(), lp.y + lp.topMargin + child.getMeasuredHeight());
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        this.drawDebugInfo(canvas, child);
        return more;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this.getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    private void drawDebugInfo(Canvas canvas, View child) {
        if (!this.config.isDebugDraw()) {
            return;
        }

        Paint childPaint = this.createPaint(0xffffff00);
        Paint newLinePaint = this.createPaint(0xffff0000);

        LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (lp.rightMargin > 0) {
            float x = child.getRight();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y - 4.0f, x + lp.rightMargin, y, childPaint);
            canvas.drawLine(x + lp.rightMargin - 4.0f, y + 4.0f, x + lp.rightMargin, y, childPaint);
        }

        if (lp.leftMargin > 0) {
            float x = child.getLeft();
            float y = child.getTop() + child.getHeight() / 2.0f;
            canvas.drawLine(x, y, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y - 4.0f, x - lp.leftMargin, y, childPaint);
            canvas.drawLine(x - lp.leftMargin + 4.0f, y + 4.0f, x - lp.leftMargin, y, childPaint);
        }

        if (lp.bottomMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getBottom();
            canvas.drawLine(x, y, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x - 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
            canvas.drawLine(x + 4.0f, y + lp.bottomMargin - 4.0f, x, y + lp.bottomMargin, childPaint);
        }

        if (lp.topMargin > 0) {
            float x = child.getLeft() + child.getWidth() / 2.0f;
            float y = child.getTop();
            canvas.drawLine(x, y, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x - 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
            canvas.drawLine(x + 4.0f, y - lp.topMargin + 4.0f, x, y - lp.topMargin, childPaint);
        }

        if (lp.newLine) {
            if (this.config.getOrientation() == HORIZONTAL) {
                float x = child.getLeft();
                float y = child.getTop() + child.getHeight() / 2.0f;
                canvas.drawLine(x, y - 6.0f, x, y + 6.0f, newLinePaint);
            } else {
                float x = child.getLeft() + child.getWidth() / 2.0f;
                float y = child.getTop();
                canvas.drawLine(x - 6.0f, y, x + 6.0f, y, newLinePaint);
            }
        }
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(2.0f);
        return paint;
    }

    public int getOrientation() {
        return this.config.getOrientation();
    }

    public void setOrientation(int orientation) {
        this.config.setOrientation(orientation);
        this.requestLayout();
    }

    public boolean isDebugDraw() {
        return this.config.isDebugDraw();
    }

    public void setDebugDraw(boolean debugDraw) {
        this.config.setDebugDraw(debugDraw);
        this.invalidate();
    }

    public float getWeightDefault() {
        return this.config.getWeightDefault();
    }

    public void setWeightDefault(float weightDefault) {
        this.config.setWeightDefault(weightDefault);
        this.requestLayout();
    }

    public int getGravity() {
        return this.config.getGravity();
    }

    public void setGravity(int gravity) {
        this.config.setGravity(gravity);
        this.requestLayout();
    }

    public int getLayoutDirection() {
        if (this.config == null){
            // Workaround for android sdk that wants to use virtual methods within constructor.
            return LAYOUT_DIRECTION_LTR;
        }

        return this.config.getLayoutDirection();
    }

    public void setLayoutDirection(int layoutDirection) {
        this.config.setLayoutDirection(layoutDirection);
        this.requestLayout();
    }

    public static class LayoutParams extends MarginLayoutParams {
        public boolean newLine = false;
        @ViewDebug.ExportedProperty(mapping = {
                @ViewDebug.IntToString(from = Gravity.NO_GRAVITY, to = "NONE"),
                @ViewDebug.IntToString(from = Gravity.TOP, to = "TOP"),
                @ViewDebug.IntToString(from = Gravity.BOTTOM, to = "BOTTOM"),
                @ViewDebug.IntToString(from = Gravity.LEFT, to = "LEFT"),
                @ViewDebug.IntToString(from = Gravity.RIGHT, to = "RIGHT"),
                @ViewDebug.IntToString(from = Gravity.CENTER_VERTICAL, to = "CENTER_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_VERTICAL, to = "FILL_VERTICAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER_HORIZONTAL, to = "CENTER_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.FILL_HORIZONTAL, to = "FILL_HORIZONTAL"),
                @ViewDebug.IntToString(from = Gravity.CENTER, to = "CENTER"),
                @ViewDebug.IntToString(from = Gravity.FILL, to = "FILL")
        })
        public int gravity = Gravity.NO_GRAVITY;
        public float weight = -1.0f;

        private int spacingLength;
        private int spacingThickness;
        private int inlineStartLength;
        private int length;
        private int thickness;
        private int inlineStartThickness;
        private int x;
        private int y;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.readStyleParameters(context, attributeSet);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public boolean gravitySpecified() {
            return this.gravity != Gravity.NO_GRAVITY;
        }

        public boolean weightSpecified() {
            return this.weight >= 0;
        }

        private void readStyleParameters(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowRadioGroup_LayoutParams);
            try {
                this.newLine = a.getBoolean(R.styleable.FlowRadioGroup_LayoutParams_radio_new_line, false);
                this.gravity = a.getInt(R.styleable.FlowRadioGroup_LayoutParams_android_layout_gravity, Gravity.NO_GRAVITY);
                this.weight = a.getFloat(R.styleable.FlowRadioGroup_LayoutParams_radio_weight, -1.0f);
            } finally {
                a.recycle();
            }
        }


        void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getInlineStartLength() {
            return inlineStartLength;
        }

        void setInlineStartLength(int inlineStartLength) {
            this.inlineStartLength = inlineStartLength;
        }

        int getLength() {
            return length;
        }

        void setLength(int length) {
            this.length = length;
        }

        int getThickness() {
            return thickness;
        }

        void setThickness(int thickness) {
            this.thickness = thickness;
        }

        int getInlineStartThickness() {
            return inlineStartThickness;
        }

        void setInlineStartThickness(int inlineStartThickness) {
            this.inlineStartThickness = inlineStartThickness;
        }

        int getSpacingLength() {
            return spacingLength;
        }

        int getSpacingThickness() {
            return spacingThickness;
        }

        void clearCalculatedFields(int orientation) {
            if (orientation == FlowRadioGroup.HORIZONTAL) {
                this.spacingLength = this.leftMargin + this.rightMargin;
                this.spacingThickness = this.topMargin + this.bottomMargin;
            }else{
                this.spacingLength = this.topMargin + this.bottomMargin;
                this.spacingThickness = this.leftMargin + this.rightMargin;
            }
        }
    }
	
    
    
    
    
    
    
    
    



	private void init() {
		mChildOnCheckedChangeListener = new CheckedStateTracker();
		mPassThroughListener = new PassThroughHierarchyChangeListener();
		super.setOnHierarchyChangeListener(mPassThroughListener);
		radioButtons = new ArrayList<RadioButton>();
	}

	@Override
	public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
		// the user listener is delegated to our pass-through listener
		mPassThroughListener.mOnHierarchyChangeListener = listener;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// checks the appropriate radio button as requested in the XML file
		if (mCheckedId != -1) {
			mProtectFromCheckedChange = true;
			setCheckedStateForView(mCheckedId, true);
			mProtectFromCheckedChange = false;
			setCheckedId(mCheckedId);
		}
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (child instanceof RadioButton) {
			final RadioButton button = (RadioButton) child;
			radioButtons.add(button);
			
			if (button.isChecked()) {
				mProtectFromCheckedChange = true;
				if (mCheckedId != -1) {
					setCheckedStateForView(mCheckedId, false);
				}
				mProtectFromCheckedChange = false;
				setCheckedId(button.getId());
			}
		} else if (child instanceof ViewGroup) {// 如果是复合控件
			// 遍历复合控件
			ViewGroup vg = ((ViewGroup) child);
			setCheckedView(vg);
		}

		super.addView(child, index, params);
	}
	
//	@Override
//	public void addView(View child) {
//		if (child instanceof RadioButton) {
//			final RadioButton button = (RadioButton) child;
//			radioButtons.add(button);
//			
//			if (button.isChecked()) {
//				mProtectFromCheckedChange = true;
//				if (mCheckedId != -1) {
//					setCheckedStateForView(mCheckedId, false);
//				}
//				mProtectFromCheckedChange = false;
//				setCheckedId(button.getId());
//			}
//		} else if (child instanceof ViewGroup) {// 如果是复合控件
//			// 遍历复合控件
//			ViewGroup vg = ((ViewGroup) child);
//			setCheckedView(vg);
//		}
//		super.addView(child);
//	}
	
	/** 查找复合控件并设置radiobutton */
	private void setCheckedView(ViewGroup vg) {
		int len = vg.getChildCount();
		for (int i = 0; i < len; i++) {
			if (vg.getChildAt(i) instanceof RadioButton) {// 如果找到了，就设置check状态
				final RadioButton button = (RadioButton) vg.getChildAt(i);
				// 添加到容器
				radioButtons.add(button);
				if (button.isChecked()) {
					mProtectFromCheckedChange = true;
					if (mCheckedId != -1) {
						setCheckedStateForView(mCheckedId, false);
					}
					mProtectFromCheckedChange = false;
					setCheckedId(button.getId());
				}
			} else if (vg.getChildAt(i) instanceof ViewGroup) {// 迭代查找并设置
				ViewGroup childVg = (ViewGroup) vg.getChildAt(i);
				setCheckedView(childVg);
			}
		}
	}

	/** 查找复合控件并设置id */
	private void setCheckedId(ViewGroup vg) {
		int len = vg.getChildCount();
		for (int i = 0; i < len; i++) {
			if (vg.getChildAt(i) instanceof RadioButton) {// 如果找到了，就设置check状态
				final RadioButton button = (RadioButton) vg.getChildAt(i);
				int id = button.getId();
				// generates an id if it's missing
				if (id == View.NO_ID) {
					id = button.hashCode();
					button.setId(id);
				}
				button.setOnCheckedChangeListener(mChildOnCheckedChangeListener);
			} else if (vg.getChildAt(i) instanceof ViewGroup) {// 迭代查找并设置
				ViewGroup childVg = (ViewGroup) vg.getChildAt(i);
				setCheckedId(childVg);
			}
		}
	}

	/** 查找radioButton控件 */
	public RadioButton findRadioButton(ViewGroup group) {
		RadioButton resBtn = null;
		int len = group.getChildCount();
		for (int i = 0; i < len; i++) {
			if (group.getChildAt(i) instanceof RadioButton) {
				resBtn = (RadioButton) group.getChildAt(i);
			} else if (group.getChildAt(i) instanceof ViewGroup) {
				resBtn = findRadioButton((ViewGroup) group.getChildAt(i));
				findRadioButton((ViewGroup) group.getChildAt(i));
				break;
			}
		}
		return resBtn;
	}

	/** 返回当前radiobutton控件的count */
	public int getRadioButtonCount() {
		return radioButtons.size();
	}

	/** 返回当前index的radio */
	public RadioButton getRadioButton(int index) {
		return radioButtons.get(index);
	}

	/**
	 * <p>
	 * Sets the selection to the radio button whose identifier is passed in
	 * parameter. Using -1 as the selection identifier clears the selection;
	 * such an operation is equivalent to invoking {@link #clearCheck()}.
	 * </p>
	 * 
	 * @param id
	 *            the unique id of the radio button to select in this group
	 * 
	 * @see #getCheckedRadioButtonId()
	 * @see #clearCheck()
	 */
	public void check(int id) {
		// don't even bother
		if (id != -1 && (id == mCheckedId)) {
			return;
		}

		if (mCheckedId != -1) {
			setCheckedStateForView(mCheckedId, false);
		}

		if (id != -1) {
			setCheckedStateForView(id, true);
		}

		setCheckedId(id);
	}

	private void setCheckedId(int id) {
		mCheckedId = id;
		if (mOnCheckedChangeListener != null) {
			mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
		}
	}

	private void setCheckedStateForView(int viewId, boolean checked) {
		View checkedView = findViewById(viewId);
		if (checkedView != null && checkedView instanceof RadioButton) {
			((RadioButton) checkedView).setChecked(checked);
		}
	}

	/**
	 * <p>
	 * Returns the identifier of the selected radio button in this group. Upon
	 * empty selection, the returned value is -1.
	 * </p>
	 * 
	 * @return the unique id of the selected radio button in this group
	 * 
	 * @see #check(int)
	 * @see #clearCheck()
	 */
	public int getCheckedRadioButtonId() {
		return mCheckedId;
	}

	/**
	 * <p>
	 * Clears the selection. When the selection is cleared, no radio button in
	 * this group is selected and {@link #getCheckedRadioButtonId()} returns
	 * null.
	 * </p>
	 * 
	 * @see #check(int)
	 * @see #getCheckedRadioButtonId()
	 */
	public void clearCheck() {
		check(-1);
	}

	/**
	 * <p>
	 * Register a callback to be invoked when the checked radio button changes
	 * in this group.
	 * </p>
	 * 
	 * @param listener
	 *            the callback to call on checked state change
	 */
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}
	
	

	/**
	 * <p>
	 * Interface definition for a callback to be invoked when the checked radio
	 * button changed in this group.
	 * </p>
	 */
	public interface OnCheckedChangeListener {
		/**
		 * <p>
		 * Called when the checked radio button has changed. When the selection
		 * is cleared, checkedId is -1.
		 * </p>
		 * 
		 * @param group
		 *            the group in which the checked radio button has changed
		 * @param checkedId
		 *            the unique identifier of the newly checked radio button
		 */
		public void onCheckedChanged(FlowRadioGroup group, int checkedId);
	}

	private class CheckedStateTracker implements
			CompoundButton.OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// prevents from infinite recursion
			if (mProtectFromCheckedChange) {
				return;
			}

			mProtectFromCheckedChange = true;
			if (mCheckedId != -1) {
				setCheckedStateForView(mCheckedId, false);
			}
			mProtectFromCheckedChange = false;

			int id = buttonView.getId();
			setCheckedId(id);
		}
	}

	/**
	 * <p>
	 * A pass-through listener acts upon the events and dispatches them to
	 * another listener. This allows the table layout to set its own internal
	 * hierarchy change listener without preventing the user to setup his.
	 * </p>
	 */
	private class PassThroughHierarchyChangeListener implements
			ViewGroup.OnHierarchyChangeListener {
		private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

		public void onChildViewAdded(View parent, View child) {
			if (parent == FlowRadioGroup.this
					&& child instanceof RadioButton) {
				int id = child.getId();
				// generates an id if it's missing
				if (id == View.NO_ID) {
					id = child.hashCode();
					child.setId(id);
				}
				((RadioButton) child)
						.setOnCheckedChangeListener(mChildOnCheckedChangeListener);
			} else if (parent == FlowRadioGroup.this
					&& child instanceof ViewGroup) {// 如果是复合控件
				// 查找并设置id
				setCheckedId((ViewGroup) child);
			}

			if (mOnHierarchyChangeListener != null) {
				mOnHierarchyChangeListener.onChildViewAdded(parent, child);
			}
		}

		public void onChildViewRemoved(View parent, View child) {
			if (parent == FlowRadioGroup.this
					&& child instanceof RadioButton) {
				((RadioButton) child).setOnCheckedChangeListener(null);
			} else if (parent == FlowRadioGroup.this
					&& child instanceof ViewGroup) {
				findRadioButton((ViewGroup) child).setOnCheckedChangeListener(
						null);
			}
			if (mOnHierarchyChangeListener != null) {
				mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
			}
		}
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
}
