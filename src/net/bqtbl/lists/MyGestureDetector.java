package net.bqtbl.lists;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.Toast;


public class MyGestureDetector extends SimpleOnGestureListener {
	public Context context;
	public String phno;

	public MyGestureDetector(Context con) {
		this.context = con;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return super.onDown(e);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Toast.makeText(context, "in Double tap", Toast.LENGTH_SHORT).show();

		return true;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Toast.makeText(context, "in single tap up", Toast.LENGTH_SHORT).show();
		// put your second activity.
		return super.onSingleTapUp(e);
	}
}