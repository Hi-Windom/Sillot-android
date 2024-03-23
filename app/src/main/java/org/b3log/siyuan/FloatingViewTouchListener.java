package org.b3log.siyuan;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class FloatingViewTouchListener implements View.OnTouchListener {

    private WindowManager windowManager;
    private View floatingView;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    public FloatingViewTouchListener(WindowManager windowManager, View floatingView) {
        this.windowManager = windowManager;
        this.floatingView = floatingView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) v.getLayoutParams();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = layoutParams.x;
                initialY = layoutParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getRawX() - initialTouchX;
                float deltaY = event.getRawY() - initialTouchY;
                int newX = initialX + (int) deltaX;
                int newY = initialY + (int) deltaY;

                int screenWidth = windowManager.getDefaultDisplay().getWidth();
                int screenHeight = windowManager.getDefaultDisplay().getHeight();
                int maxX = screenWidth - v.getWidth();
                int maxY = screenHeight - v.getHeight();

                newX = Math.max(0, Math.min(maxX, newX));
                newY = Math.max(0, Math.min(maxY, newY));

                layoutParams.x = newX;
                layoutParams.y = newY;
                windowManager.updateViewLayout(v, layoutParams);
                return true;
            case MotionEvent.ACTION_UP:
                // Handle the click event if needed
                return true;
        }
        return false;
    }
}
