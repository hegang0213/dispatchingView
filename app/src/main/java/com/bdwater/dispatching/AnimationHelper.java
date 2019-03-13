package com.bdwater.dispatching;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationHelper {
	public static void fadeIn(Context context, View v) {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
		anim.setFillAfter(true);
		v.startAnimation(anim);
	}

	public static void fading(Context context, View v) {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.fading);
		anim.setFillAfter(true);
		v.startAnimation(anim);
	}
	public static void fadeOut(Context context, View v) {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
		v.startAnimation(anim);
	}
	public static void flyIn(Context context, View v) {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.fly_in);
		v.startAnimation(anim);
	}
	public static void flyIn(Context context, View v, int startOffset) {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.fly_in);
		anim.setStartOffset(startOffset);
		v.startAnimation(anim);
	}
}
