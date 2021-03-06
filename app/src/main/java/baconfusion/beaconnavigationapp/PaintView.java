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
            paint.setStrokeWidth(2.0f);
            paint.setStrokeCap(Paint.Cap.ROUND);
            /*// x-Achse
            canvas.drawLine(0.0f, heightOfView / 2, widthOfView, heightOfView / 2, paint);
            //y-Achse
            canvas.drawLine(widthOfView / 2, 0, widthOfView / 2, heightOfView, paint);*/
            sw = 10.0f;
            //x_achse
            canvas.drawLine(0.0f + sw, heightOfView - sw, widthOfView - sw, heightOfView - sw, paint);
            //y_achse
            canvas.drawLine(0.0f + sw, heightOfView - sw, 0.0f + sw, 0.0f + sw, paint);
            paint.setStrokeWidth(sw);
            //widthOfView -= sw;
            //heightOfView -= sw;
            //nochmal gut skalieren, skaliere, suche min und max und scale mit max-min
            float delta_x = delta(b_x);
            float delta_y = delta(b_y);
            float scale_x = (float) widthOfView-sw / delta_x; //wd /delta_x
            float scale_y = (float) heightOfView-sw / delta_y;
            float mid_x = (float) widthOfView / 2.0f;
            float mid_y = (float) heightOfView / 2.0f;
            float x_axe = 0.0f + sw;
            float y_axe = (float) heightOfView - sw;
            //draw Beacons
            //paint.setColor(Color.BLUE);
            for(int i = 0; i < len; i++) {
                //float x_i = mid_x + ( b_x[i] * scale_x);
                //float y_i = mid_y - ( b_y[i] * scale_y);
                float x_i = x_axe + ( b_x[i] * scale_x);
                float y_i = y_axe - ( b_y[i] * scale_y);
                paint.setColor(b_i[i]);
                canvas.drawPoint(x_i, y_i, paint);
                String ident = ""+b_i[i];
                canvas.drawText(ident, x_i, y_i, paint);
            }
            //draw own position
            paint.setColor(Color.RED);
            //float draw_x = mid_x + ( x*scale_x);
            //float draw_y = mid_y - ( y*scale_y);
            float draw_x = x_axe + ( x*scale_x);
            float draw_y = y_axe - ( y*scale_y);
            canvas.drawPoint(draw_x, draw_y, paint);
        }

        float delta(float[] x){
            float max_x = 0;
            for(int i = 0; i < len; i++){
                if (max_x < x[i])
                    max_x = x[i];
            }
            float min_x = 0;
            for(int i = 0; i < len; i++) {
                if(min_x > x[i])
                    min_x = x[i];
            }
            return max_x - min_x;
        }
}
