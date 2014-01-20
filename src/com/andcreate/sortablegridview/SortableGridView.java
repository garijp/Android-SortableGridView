/*
 * Copyright 2014 gari_jp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andcreate.sortablegridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class SortableGridView extends GridView {
    @SuppressWarnings("unused")
    private static final String TAG = SortableGridView.class.getSimpleName();
    
    private static final float DRAG_VIEW_SCALE = 1.4f;
    
    private DragAndDropListener mListener;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private ImageView mDragView = null;
    private Bitmap mDragBitmap = null;
    private int mDragViewWidth = 0;
    private int mDragViewHeight = 0;
    private int mFromPosition = AdapterView.INVALID_POSITION;
    private boolean isSortMode = false;
    
    public SortableGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        mWindowParams.x = getLeft();
        mWindowParams.y = getTop();
    }
    
    public SortableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setOnDragAndDropListener(DragAndDropListener listener) {
        mListener = listener;
    }
    
    public void setSortMode(boolean sortMode) {
        isSortMode = sortMode;
        
        if (sortMode) {
            animateAllItems();
        } else {
            cancelAnimations();
        }
    }
    
    public boolean getSortMode() {
        return isSortMode;
    }
    
    private void animateAllItems() {
        Animation rotateAnimation = createFastRotateAnimation();
        
        for (int i = 0; i < getCount(); i++) {
            View child = getChildAt(i);
            child.startAnimation(rotateAnimation);
        }
    }
    
    private void cancelAnimations() {
        for (int i = 0; i < getCount(); i++) {
            View child = getChildAt(i);
            child.clearAnimation();
        }
    }
    
    private Animation createFastRotateAnimation() {
        Animation rotate = new RotateAnimation(-2.0f, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        
        rotate.setRepeatMode(Animation.REVERSE);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(60);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        
        return rotate;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSortMode) {
            return super.onTouchEvent(event);
        }
        
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mFromPosition = pointToPosition(x, y);
                if (mFromPosition == AdapterView.INVALID_POSITION) {
                    return false;
                }
                startDrag();
                updateLayout(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                updateLayout(x, y);
                return true;
            case MotionEvent.ACTION_UP:
                endDrag();
                if (mListener != null) {
                    int toPosition = pointToPosition((int)event.getX(), (int)event.getY());
                    if (toPosition != AdapterView.INVALID_POSITION) {
                        mListener.dropped(mFromPosition, toPosition);
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                endDrag();
                return true;
            default:
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    private void updateLayout(int x, int y) {
        mWindowParams.x = getLeft() - getPaddingLeft() + x - (mDragViewWidth / 2);
        mWindowParams.y = getTop() - getPaddingTop() + y;
        
        mWindowManager.updateViewLayout(mDragView, mWindowParams);
    }
    
    private void startDrag() {
        View fromPositionView = getChildByPosition(mFromPosition);
        mDragBitmap = Bitmap.createBitmap(fromPositionView.getWidth(), fromPositionView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(mDragBitmap);
        fromPositionView.draw(canvas);
        fromPositionView.clearAnimation();
        fromPositionView.setVisibility(View.INVISIBLE);
        
        if (mDragView != null) {
            mWindowManager.removeView(mDragView);
        }
        
        mDragView = new ImageView(getContext());
        mDragView.setImageBitmap(mDragBitmap);
        
        mDragViewWidth = (int)(fromPositionView.getWidth() * DRAG_VIEW_SCALE);
        mDragViewHeight = (int)(fromPositionView.getHeight() * DRAG_VIEW_SCALE);
        
        mWindowParams.width = mDragViewWidth;
        mWindowParams.height = mDragViewHeight;
        
        mWindowManager.addView(mDragView, mWindowParams);
    }
    
    private void endDrag() {
        View fromPositionView = getChildByPosition(mFromPosition);
        Animation rotateAnimation = createFastRotateAnimation();
        fromPositionView.setVisibility(View.VISIBLE);
        fromPositionView.startAnimation(rotateAnimation);
        
        mWindowManager.removeView(mDragView);
        mDragView.clearAnimation();
        mDragView = null;
        mDragBitmap = null;
    }
    
    private View getChildByPosition(int position) {
        return getChildAt(position - getFirstVisiblePosition());
    }
}
