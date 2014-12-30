/**
 * 
 */
package org.moca;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;

/**id
 * @author markyen
 *
 */
public class SelectableImageView extends ImageView {
	private static final String TAG = SelectableImageView.class.toString();
	
	private long imageId = -1;
	private ScalingImageAdapter adapter;
	
	/**
	 * @param context
	 */
	public SelectableImageView(Context context, ScalingImageAdapter adapter) {
		super(context);
		this.adapter = adapter;
	}
	
	public long getImageId() {
		return imageId;
	}

	public void setImageId(long imageId) {
		this.imageId = imageId;
	}
	
	private boolean showBorder() {
		if(imageId != -1) {
			return adapter.isSelected(imageId);
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(TAG,"draw");
		super.onDraw(canvas);
		
		if (showBorder()) {
//			Log.v("mocapicture","selected!");
			Rect r_left = new Rect(0,0,8,canvas.getHeight());
			Rect r_top = new Rect(0,0,canvas.getWidth(),8);
			Rect r_right = new Rect(canvas.getWidth()-8,0,canvas.getWidth(),canvas.getHeight());
			Rect r_bottom = new Rect(0,canvas.getHeight()-8,canvas.getWidth(),canvas.getHeight());
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.YELLOW);
			canvas.drawRect(r_left, paint);
			canvas.drawRect(r_top, paint);
			canvas.drawRect(r_right, paint);
			canvas.drawRect(r_bottom, paint);
		}
	}
}
