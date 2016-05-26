package proto.axa.com.gradientanimation;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabianlanglet on 5/26/16.
 */
public class GradientImageView extends ImageView {

    static public final int LIFETIME_DEAFULT = 1600;
    static public final int[] COLORS_DEFAULT = {Color.parseColor("#60FFFFFF"), Color.parseColor("#60000000"), Color.parseColor("#60FFFFFF")};
    static public final boolean START_ANIMATION_ON_CREATION_DEFAULT = true;
    static public final boolean INFINIT_LOOP = true;

    private Paint borderPaint, fillPaint;

    private int colors[] = {};
    private int height = 0, width = 0;

    private boolean startAnimationOnCreation;
    private TimeAnimator gradientAnimation = new TimeAnimator();
    private long lifetime = LIFETIME_DEAFULT, updateTickMs = 25, timeElapsed = 0;
    private long accumulatorMs = 0;
    private float gradientOffset = 0f;
    private Path shapePath, tempPath = new Path();;

    public GradientImageView(Context context) {
        super(context);
    }

    public GradientImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        parseAttrs(attrs, 0);
        onInitialize();
    }

    public GradientImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        parseAttrs(attrs, defStyleAttr);
        onInitialize();
    }

    public void parseAttrs(AttributeSet attrs, int defStyleAttr) {
        setLifetime(LIFETIME_DEAFULT);
        setGradientStatesColors(COLORS_DEFAULT.clone());
        setStartAnimationOnCreation(START_ANIMATION_ON_CREATION_DEFAULT);
    }

    private void onInitialize() {
        setWillNotDraw(false);

        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(2);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setColor(colors[0]);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = getWidth();
        height = getHeight();

        LinearGradient gradient = new LinearGradient(
                0, height / 2, width * colors.length - 1, height / 2,
                colors, null, Shader.TileMode.REPEAT);
        fillPaint.setShader(gradient);

        //Create the circle path to apply the gradient on it
        shapePath = new Path();
        shapePath.addCircle(width/2, height/2, width/2, Path.Direction.CW);

        resolveTimeElapsed();
        if (startAnimationOnCreation) {
            startGradientAnimation();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Draw the gradient and add translation
        canvas.save();
        canvas.translate(gradientOffset, 0);
        shapePath.offset(-gradientOffset, 0f, tempPath);
        canvas.drawPath(tempPath, fillPaint);
        canvas.restore();
    }

    public void setStartAnimationOnCreation(boolean startAnimationOnCreation) {
        this.startAnimationOnCreation = startAnimationOnCreation;
    }

    public void setGradientStatesColors(int[] statesColors) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < statesColors.length; i++) {
            colors.add(statesColors[i]);
            colors.add(statesColors[i]); // purposly: for states
        }

        this.colors = ToInts(colors);
    }

    public void setLifetime(long lifetime) {
        setLifetime(lifetime, 0);
    }

    public void setLifetime(long lifetime, long timeElapsed) {
        this.lifetime = lifetime;
        this.timeElapsed = timeElapsed;
        resolveTimeElapsed();
    }

    public void startGradientAnimation() {
        stopGradientAnimation();
        resolveTimeElapsed();

        final float gradientOffsetCoef = (float) (updateTickMs) / lifetime;
        final int colorsCount = this.colors.length - 1;
        gradientAnimation.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                //totalTime = totalTime % lifetime; // TODO: 24.08.2015 delete this after debugging

                final long gradientWidth = width * colorsCount;
                if (totalTime > (lifetime - timeElapsed)) {
                    if (INFINIT_LOOP) {
                        animation.cancel();
                        accumulatorMs = 0;
                        gradientOffset = 0;
                        animation.start();
                    } else {
                        animation.cancel();
                        gradientOffset = gradientWidth;
                        invalidate();
                    }
                } else {
                    accumulatorMs += deltaTime;

                    final long gradientOffsetsCount = accumulatorMs / updateTickMs;
                    gradientOffset += (gradientOffsetsCount * gradientWidth) * gradientOffsetCoef;
                    accumulatorMs %= updateTickMs;

                    boolean gradientOffsetChanged = (gradientOffsetsCount > 0) ? true : false;
                    if (gradientOffsetChanged) {
                        invalidate();
                    }
                }
            }
        });

        gradientAnimation.start();
    }

    public void stopGradientAnimation() {
        gradientAnimation.cancel();
        accumulatorMs = 0;
        gradientOffset = 0;
    }

    public boolean isGradientAnimationRunning() {
        return gradientAnimation.isRunning();
    }

    public void clearTimeElapsed() {
        timeElapsed = 0;
    }

    public void resolveTimeElapsed() {
        final float gradientOffsetCoef = (float) (timeElapsed) / lifetime;
        final int colorsCount = this.colors.length - 1;
        final long gradientWidth = width * colorsCount;
        gradientOffset = gradientWidth * gradientOffsetCoef;
    }

    public int[] ToInts(List<Integer> list) {
        int[] ints = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ints[i] = list.get(i);
        }
        return ints;
    }

}
