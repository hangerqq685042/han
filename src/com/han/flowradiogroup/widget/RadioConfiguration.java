package com.han.flowradiogroup.widget;

import com.han.flowradiogroup.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;


/* 
 * 
 * Created by han on 2013/5/5.
 *
 * */
class RadioConfiguration {
    private int orientation = FlowRadioGroup.HORIZONTAL;
    private boolean debugDraw = false;
    private float weightDefault = 0;
    private int gravity = Gravity.LEFT | Gravity.TOP;
    private int layoutDirection = FlowRadioGroup.LAYOUT_DIRECTION_LTR;

    public RadioConfiguration(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowRadioGroup);
        try {
            this.setOrientation(a.getInteger(R.styleable.FlowRadioGroup_android_orientation, FlowRadioGroup.HORIZONTAL));
            this.setDebugDraw(a.getBoolean(R.styleable.FlowRadioGroup_debug_draw, false));
            this.setWeightDefault(a.getFloat(R.styleable.FlowRadioGroup_weight_default, 0.0f));
            this.setGravity(a.getInteger(R.styleable.FlowRadioGroup_android_gravity, Gravity.NO_GRAVITY));
            this.setLayoutDirection(a.getInteger(R.styleable.FlowRadioGroup_radio_direction, FlowRadioGroup.LAYOUT_DIRECTION_LTR));
        } finally {
            a.recycle();
        }
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == FlowRadioGroup.VERTICAL) {
            this.orientation = orientation;
        } else {
            this.orientation = FlowRadioGroup.HORIZONTAL;
        }
    }

    public boolean isDebugDraw() {
        return this.debugDraw;
    }

    public void setDebugDraw(boolean debugDraw) {
        this.debugDraw = debugDraw;
    }

    public float getWeightDefault() {
        return this.weightDefault;
    }

    public void setWeightDefault(float weightDefault) {
        this.weightDefault = Math.max(0, weightDefault);
    }

    public int getGravity() {
        return this.gravity;
    }

    public void setGravity(int gravity) {
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.LEFT;
        }

        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.TOP;
        }

        this.gravity = gravity;
    }

    public int getLayoutDirection() {
        return layoutDirection;
    }

    public void setLayoutDirection(int layoutDirection) {
        if (layoutDirection == FlowRadioGroup.LAYOUT_DIRECTION_RTL) {
            this.layoutDirection = layoutDirection;
        } else {
            this.layoutDirection = FlowRadioGroup.LAYOUT_DIRECTION_LTR;
        }
    }
}
