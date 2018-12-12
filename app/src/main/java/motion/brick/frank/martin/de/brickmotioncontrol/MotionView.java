package motion.brick.frank.martin.de.brickmotioncontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MotionView extends View {

    private MotionInputListener motionInputListener;

    private float x;
    private float y;

    private final RectF rect = new RectF(0,0,0,0);
    private final Paint blackLine = new Paint();
    private final Paint whiteFill = new Paint();
    private final Paint blackFill = new Paint();

    public MotionView(Context context) {
        super(context);
        init(null, 0);
    }

    public MotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MotionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        blackLine.setStyle(Paint.Style.STROKE);
        blackLine.setStrokeWidth(3f);
        blackLine.setARGB(0xff, 0x0,0x0, 0x0);
        whiteFill.setStyle(Paint.Style.FILL);
        whiteFill.setARGB(0xff, 0xFF,0xFF, 0xFF);
        blackFill.setStyle(Paint.Style.FILL);
        blackFill.setARGB(0xff, 0x0,0x0, 0x0);
        addTouchListener();
    }

    private void addTouchListener() {
        setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    invalidate();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float xcenter = getWidth() / 2f;
                    float ycenter = getHeight() / 2f;
                    float dx = xcenter - event.getX();
                    float dy = ycenter - event.getY();
                    dx = (float)MathUtil.limit(-1*xcenter, dx, xcenter);
                    dy = (float)MathUtil.limit(-1*ycenter, dy, ycenter);
                    x = dx;
                    y = dy;
                    if(motionInputListener != null){
                        motionInputListener.motion(-100f * (dx / xcenter),100f * (dy / ycenter));
                    }
                    invalidate();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    motionInputListener.motion(0,0);
                    x = 0;
                    y = 0;
                    invalidate();
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rect.set(10,10,getWidth()-10, getHeight()-10);
        canvas.drawOval(rect, blackLine);
        float dx = rect.centerX() - x;
        float dy = rect.centerY() - y;
        canvas.drawLine(rect.centerX(), rect.centerY(), dx,dy, blackLine);
    }


    public void setMotionInputListener(MainActivity motionInputListener) {
        this.motionInputListener = motionInputListener;
    }
}
