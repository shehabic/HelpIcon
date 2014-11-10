package com.shehabic.helpicon;

import android.view.View;

public interface AnimationHandlerInterface
{
	public void show(View helpIcon, HelpIcon helpIconLib);
	public void hide(View helpIcon, HelpIcon helpIconLib);
	public boolean animateHide();
	public boolean animateShow();
}
