/** ===================================================================================
 * [CUSTOM VIEW PAGER]
 * Implementasi ViewPager yang dimodifikasi sehingga tidak bisa di-slide.
 * (View pager memungkinkan user untuk "berpindah halaman" pada pages of data.)
 * ----------------------------------------------------------------------------------
 * Author: Kemal Amru Ramadhan
 * Refactor & Documentation: Ferdinand Antonius
 * =================================================================================== */


package pinjemin.behavior;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class CustomViewPager extends ViewPager
{
	private boolean enabled;

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.enabled = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.enabled) {
			return super.onTouchEvent(event);
		}

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (this.enabled) {
			return super.onInterceptTouchEvent(event);
		}

		return false;
	}

	public void setPagingEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
