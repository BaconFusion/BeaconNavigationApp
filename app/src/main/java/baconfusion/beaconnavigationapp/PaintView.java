package baconfusion.beaconnavigationapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
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
            this.x = x;
            this.y = y;
            this.b_x = b_x;
            this.b_y = b_y;
            this.b_i = b_i;
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
            paint.setColor(Color.BLACK);
            // x-Achse
            canvas.drawLine(0.0f, heightOfView / 2, widthOfView, heightOfView / 2, paint);
            //y-Achse
            canvas.drawLine(widthOfView / 2, 0, widthOfView / 2, heightOfView, paint);

            //Orientierungspunkte
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(10.0f);
            //get.Beacons_pos(); Beacons.x, Beacons.y;
            canvas.drawPoint(0.0f, 0.0f, paint);
            paint.setColor(Color.RED);
            canvas.drawPoint(0.0f, heightOfView, paint);
            paint.setColor(Color.BLUE);
            canvas.drawPoint(widthOfView, 0.0f, paint);

            //nochmal gut skalieren
            float scale_x = widthOfView / 20;
            float scale_y = heightOfView / 20;
            float mid_x = widthOfView / 2;
            float mid_y = heightOfView / 2;
            paint.setStrokeWidth(10.0f);
            paint.setColor(Color.RED);
            //x += 5.0f;
            //y += -5.0f;
            //draw for point (5,5
            //draw own position
            canvas.drawPoint(mid_x + x, mid_y + y, paint);

            System.out.println("width: " + widthOfView);
            System.out.println("height: " + heightOfView);

            //draw Beacons
            paint.setColor(Color.BLUE);
            //canvas.drawPoint(mid_x - 30.0f, mid_y - 30.0f, paint);
            /*int len = b_y.length;
            for(int i = 0; i < len; i++) {
                float x = mid_x + ( b_x[i] * scale_x);
                float y = -1 *(mid_y +(b_y[i] * scale_y));
                canvas.drawPoint(x, y, paint);
                String ident = ""+b_i[i];
                canvas.drawText(ident, x, y, paint);
            }*/
        }
}
