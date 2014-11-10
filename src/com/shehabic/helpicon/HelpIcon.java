package com.shehabic.helpicon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

public class HelpIcon
{
	/*  LBRT  : Left|Bottom|Right|Top
		0001  01       Top
		0010  02       Right
		0011  03       Right Top
		0100  04       Bottom
		0101  05       
		0110  06       Bottom Right
		0111  07
		1000  08       Left
		1001  09       Left Top
		1010  10       
		1011  11
		1100  12       Left Bottom
		1101  13
		1110  14
		1111  15
	 */

	public static final int POSITION_TOP 			= 1;
	public static final int POSITION_RIGHT 			= 2;
	public static final int POSITION_TOP_RIGHT 		= 3;
	public static final int POSITION_BOTTOM 		= 4;
	public static final int POSITION_BOTTOM_RIGHT 	= 6;
	public static final int POSITION_TOP_LEFT 	  	= 9;
	public static final int POSITION_LEFT 		  	= 8;
	public static final int POSITION_BOTTOM_LEFT 	= 12;
	
	public static final int ALIGN_HORIZONTAL_LEFT 	= 1;
	public static final int ALIGN_HORIZONTAL_CENTER = 2;
	public static final int ALIGN_HORIZONTAL_RIGHT 	= 4;

	public static final int ALIGN_VERTICAL_TOP 		= 1;
	public static final int ALIGN_VERTICAL_BOTTOM 	= 2;
	public static final int ALIGN_VERTICAL_CENTER 	= 4;
	
	public boolean isAnimating = false;


	protected AnimationHandlerInterface animationHandler;

	public Activity activity;
	protected View sourceView;
	protected int positionX;
	protected int positionY;
	protected int position;
	protected Item helpIcon;
	protected int verticalAlignment = ALIGN_VERTICAL_CENTER;
	protected int horizontalAlignment = ALIGN_HORIZONTAL_CENTER;
	
	protected boolean added = false;

	public static class Item
	{
		public int x;
		public int y;
		public int width;
		public int height;
		public int marginLeft = 0;
		public int marginRight = 0;
		public int marginTop = 0;
		public int marginBottom = 0;

		public View view;

		public Item(View view, int width, int height)
		{
			this.view = view;
			this.width = width;
			this.height = height;
			x = 0;
			y = 0;
		}

		public void setMargin(int top, int right, int left, int bottom)
		{
			this.marginTop = top;
			this.marginRight = right;
			this.marginBottom = bottom;
			this.marginLeft = left;
		}
	}

	public HelpIcon(Activity activity, View sourceView, Item helpIcon, int position, int horizontalAlignment,
	        int verticalAlignment)
	{
		this.activity = activity;
		this.sourceView = sourceView;
		this.helpIcon = helpIcon;
		this.position = position;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
	}
	
	protected OnClickListener onclick;

	protected void setOnClick(OnClickListener onclick)
	{
		this.onclick = onclick;
	}
	
	public void setAnimationHandler(AnimationHandlerInterface animationHandler) {
		this.animationHandler = animationHandler;
	}
	
	public void detach(View v)
	{
		((ViewGroup) getActivityContentView()).removeView(v);
		added = false;
	}
	
	public void detach()
	{
		detach(helpIcon.view);
	}
	
	protected View getActivityContentView()
	{
		return ((Activity) sourceView.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
	}

	protected Point getScreenSize()
	{
		Point size = new Point();
		((Activity) sourceView.getContext()).getWindowManager().getDefaultDisplay().getSize(size);
		return size;
	}

	protected Point getActionViewCoordinates()
	{
		int[] coords = new int[2];
		sourceView.getLocationOnScreen(coords);
		
		return new Point(coords[0], coords[1]);
	}

	public Point getActionViewCenter()
	{
		Point point = getActionViewCoordinates();
		point.x += sourceView.getMeasuredWidth() / 2;
		point.y += sourceView.getMeasuredHeight() / 2;

		return point;
	}
	
	public View getHelpIcon()
	{
		return helpIcon.view;
	}

	protected void calculateHelpIconPosition()
	{
		Point center = getActionViewCenter();
		
		// Adjusting X (Left)
		if (isPositioned(position, POSITION_RIGHT)) {
			helpIcon.x = center.x + (sourceView.getMeasuredWidth() / 2);
		} else if (isPositioned(position, POSITION_LEFT)) {
			helpIcon.x = center.x - (sourceView.getMeasuredWidth() / 2) - (helpIcon.width);
		} else {
			helpIcon.x = center.x - (helpIcon.width / 2);
		}

		// Adjusting Y (Top)
		if (isPositioned(position, POSITION_TOP)) {
			helpIcon.y = center.y - helpIcon.height - (sourceView.getMeasuredHeight() / 2);
		} else if (isPositioned(position, POSITION_BOTTOM)) {
			helpIcon.y = center.y + (sourceView.getMeasuredHeight() / 2);
		} else {
			helpIcon.y = center.y - (helpIcon.height / 2);
		} 

		// Special Cases based on alignment & position
		if (position == POSITION_TOP || position == POSITION_BOTTOM) {
			if (horizontalAlignment == ALIGN_HORIZONTAL_LEFT) {
				helpIcon.x = center.x - (sourceView.getMeasuredWidth() / 2);
			} else if (horizontalAlignment == ALIGN_HORIZONTAL_RIGHT) {
				helpIcon.x = center.x + (sourceView.getMeasuredWidth() / 2) - helpIcon.width;
			}
		}

		if (position == POSITION_LEFT || position == POSITION_RIGHT) {
			if (verticalAlignment == ALIGN_VERTICAL_TOP) {
				helpIcon.y = center.y - (sourceView.getMeasuredHeight() / 2);
			} else if (verticalAlignment == ALIGN_VERTICAL_BOTTOM) {
				helpIcon.y = center.y + (sourceView.getMeasuredHeight() / 2) - helpIcon.height;
			}
		}
		
		helpIcon.y -= isPositioned(position, POSITION_TOP) ? helpIcon.marginTop : 0;
		helpIcon.y += isPositioned(position, POSITION_BOTTOM) ? helpIcon.marginBottom : 0;
		helpIcon.x -= isPositioned(position, POSITION_LEFT) ? helpIcon.marginLeft : 0;
		helpIcon.x += isPositioned(position, POSITION_RIGHT) ? helpIcon.marginRight : 0;
		
	}
	
	protected boolean isPositioned(int value, int position)
	{
		return (value & position) == position;
	}

	public void show()
	{
		show(true);
	}

	public void show(boolean animated)
	{
		if (isAnimating) {
			return;
		}
		isAnimating = true;
		
		calculateHelpIconPosition();
		addView();
		if (animated && animationHandler != null) {
			helpIcon.view.setVisibility(View.GONE);
			animationHandler.show(helpIcon.view, this);
		} else {
			helpIcon.view.setVisibility(View.VISIBLE);
			isAnimating = false;
		}
		
		if (this.onclick != null) {
			helpIcon.view.setOnClickListener(this.onclick);
		}
	}

	protected void addView()
	{
		if (added) {
			return;
		}
		Log.d("Adding", "Yes");
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(helpIcon.width, helpIcon.height, Gravity.TOP
		        | Gravity.LEFT);
		params.setMargins(helpIcon.x, helpIcon.y, 0, 0);
		if (helpIcon.view.getParent() != null) {
			((ViewGroup) helpIcon.view.getParent()).removeView(helpIcon.view);
		}
		((ViewGroup) getActivityContentView()).addView(helpIcon.view, params);
		added = true;
	}

	public void hide(Boolean animated)
	{
		if (isAnimating) {
			return;
		}
		isAnimating = true;
		if (animated && animationHandler != null) {
			animationHandler.hide(helpIcon.view, this);
		} else {
			detach();
			isAnimating = false;
		}
	}

	public void hide()
	{
		hide(true);
	}

	public void updatePosition()
	{
		calculateHelpIconPosition();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(helpIcon.width, helpIcon.height, Gravity.TOP
		        | Gravity.LEFT);
		params.setMargins(helpIcon.x, helpIcon.y, 0, 0);
		getHelpIcon().setLayoutParams(params);
	}
	
	@SuppressLint("NewApi")
    protected void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
	    if (Build.VERSION.SDK_INT < 16) {
	        v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
	    } else {
	        v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
	    }
	}
	
	protected ViewTreeObserver.OnGlobalLayoutListener onFirstRendered;
	
	public void setOnRendered(final ViewTreeObserver.OnGlobalLayoutListener onFirstRendered) {
		this.onFirstRendered = onFirstRendered;
		this.helpIcon.view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				removeOnGlobalLayoutListener(getHelpIcon(), this);
				if (onFirstRendered != null) {
					onFirstRendered.onGlobalLayout();
				}
			}
		});
	}

	public static class Builder
	{
		protected View srcView;
		protected Item hlpIcon;
		protected int position;
		protected int positionX;
		protected int positionY;
		protected Activity activity;
		protected int horizontalAlignment = ALIGN_HORIZONTAL_CENTER;
		protected int verticalAlignment = ALIGN_VERTICAL_CENTER;
		protected OnClickListener onclick;
		protected AnimationHandlerInterface animationHandler;
		protected ViewTreeObserver.OnGlobalLayoutListener onFirstRendered;

		public Builder(Activity activity)
		{
			this.activity = activity;
		}
		
		public Builder setAnimationHandler(AnimationHandlerInterface animationHandler) {
			this.animationHandler = animationHandler;
			
			return this;
		}

		public Builder setSourceView(View srcView)
		{
			this.srcView = srcView;

			return this;
		}

		public Builder setPosition(int position)
		{
			this.position = position;

			return this;
		}

		public Builder setHelpIcon(View helpIcon)
		{
			setHelpIcon(helpIcon, POSITION_TOP);

			return this;
		}

		public Builder setHelpIcon(View helpIcon, int positionX, int positionY)
		{
			this.hlpIcon = new Item(helpIcon, helpIcon.getMeasuredWidth(), helpIcon.getMeasuredHeight());
			this.positionX = positionX;
			this.positionY = positionY;

			return this;
		}

		public Builder setHelpIcon(View helpIcon, int position)
		{
			this.hlpIcon = new Item(helpIcon, helpIcon.getMeasuredWidth(), helpIcon.getMeasuredHeight());
			this.position = position;

			return this;
		}

		public Builder setHelpIcon(int resId, Context context)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(resId, null, false);
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			// Log.d("Measured W/H", view.getMeasuredWidth() + "," + view.getMeasuredHeight());
			this.hlpIcon = new Item(view, view.getMeasuredWidth(), view.getMeasuredHeight());

			return this;
		}

		public Builder setAlignment(int horizontal, int vertical)
		{
			this.horizontalAlignment = horizontal;
			this.verticalAlignment = vertical;

			return this;
		}

		public Builder setVerticalAlignment(int vertical)
		{
			this.verticalAlignment = vertical;

			return this;
		}

		public Builder setHorizontalAlignment(int horizontal)
		{
			this.horizontalAlignment = horizontal;

			return this;
		}

		public Builder setMarginRight(int val)
		{
			this.hlpIcon.marginRight = val;
			
			return this;
		}

		public Builder setMarginTop(int val)
		{
			this.hlpIcon.marginTop = val;
			
			return this;
		}
		
		public Builder setMarginLeft(int val)
		{
			this.hlpIcon.marginLeft = val;
			
			return this;
		}
		
		public Builder setMarginBottom(int val)
		{
			this.hlpIcon.marginBottom = val;
			
			return this;
		}
		
		public Builder setMargin(int top, int right, int bottom, int left)
		{
			this.hlpIcon.setMargin(top, right, left, bottom);
			
			return this;
		}
		
		public Builder setOnClick(OnClickListener onclick) {
			this.onclick = onclick; 
			
			return this;
		}
		
		public Builder setOnRendered(ViewTreeObserver.OnGlobalLayoutListener onFirstRendered) 
		{
			this.onFirstRendered = onFirstRendered;
			
			return this;
		}
		
		public Builder setOnMeasure() {
			return this;
		}
		
		public HelpIcon build()
		{
			HelpIcon helpIcon =  new HelpIcon(this.activity, this.srcView, this.hlpIcon, this.position, this.horizontalAlignment,
			        this.verticalAlignment);
			if (this.onclick != null) {
				helpIcon.setOnClick(this.onclick);
			}
			
			if (this.onFirstRendered != null) {
				helpIcon.setOnRendered(this.onFirstRendered);
			}
			
			if (this.animationHandler != null) {
				helpIcon.setAnimationHandler(this.animationHandler);
			}
			
			return helpIcon;
		}
	}
}
