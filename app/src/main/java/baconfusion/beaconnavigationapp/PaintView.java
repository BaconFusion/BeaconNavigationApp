package baconfusion.beaconnavigationapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by swong on 26.09.2016.
 */
public class PaintView extends View implements PositionNotifier{

        private float sw = 2.0f;
        private int widthOfView;
        private int heightOfView;
        private static Paint paint;
        float x, y;
        float[] b_x, b_y;
        int[] b_i;
        int len = 0;

    // public Position pos;
        public static Canvas canvas;

        public PaintView (Context context) {
            super(context);
            this.setUp();
        }

        public PaintView (Context context, AttributeSet attrs) {
            super(context, attrs);
            this.setUp();
        }

       public void onDataArrived(float x, float y, float[] b_x, float[] b_y, int[] b_i) {
           Log.d("AAAAYYYYYY" , "UHUHUHUHUHUHUHUHUHUHUHUHUHUHU " + x + " // " + y);
            this.x = x;
            this.y = y;
            this.b_x = b_x;
            this.b_y = b_y;
            this.b_i = b_i;
            this.len = b_i.length;
            invalidate();
        }


        private void setUp() {
            paint = new Paint();
            paint.setAntiAlias(true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldW, int oldH) {
            widthOfView = w;
            heightOfView = h;
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(sw);
            paint.setStrokeCap(Paint.Cap.ROUND);
            // x-Achse
            canvas.drawLine(0.0f, heightOfView / 2, widthOfView, heightOfView / 2, paint);
            //y-Achse
            canvas.drawLine(widthOfView / 2, 0, widthOfView / 2, heightOfView, paint);
            paint.setStrokeWidth(5.0f);
            //widthOfView -= sw;
            //heightOfView -= sw;
            //nochmal gut skalieren
            float scale_x = (float) widthOfView / 40.0f;
            float scale_y = (float) heightOfView / 40.0f;
            float mid_x = (float) widthOfView / 2.0f;
            float mid_y = (float) heightOfView / 2.0f;
            paint.setColor(Color.RED);
            float draw_x = mid_x + ( x*scale_x);
            float draw_y = mid_y - ( y*scale_y);
            canvas.drawPoint(draw_x, draw_y, paint);
            //draw Beacons
            paint.setColor(Color.BLUE);
            for(int i = 0; i < len; i++) {
                float x_i = mid_x + ( b_x[i] * scale_x);
                float y_i = mid_y - ( b_y[i] * scale_y);
                canvas.drawPoint(x_i, y_i, paint);
                String ident = ""+b_i[i];
                canvas.drawText(ident, x_i, y_i, paint);
            }
        }
}
