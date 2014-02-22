package org.scbio.onebuttonlarry;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class MusicManager {
	private static final String TAG = "MusicManager";
	private static MediaPlayer mPlayer;

	public static void start(Context context, int resid) 
	{
		if (mPlayer != null) return;

		mPlayer = MediaPlayer.create(context, resid);

		if (mPlayer == null) {
			Log.e(TAG, "MediaPlayer not created");
		} else {
			try {
				mPlayer.setLooping(true);
				mPlayer.start();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		Log.d(TAG, "Background music playing: "+mPlayer.isPlaying());
	}

	public static void pause() 
	{
		if(mPlayer == null) return;
		if(!mPlayer.isPlaying()) return;

		try {
			mPlayer.pause();
		} catch (Exception e) {
			Log.e(TAG, "MediaPlayer chrashed @ pause()", e);
			Log.e(TAG, e.getMessage(), e);
		}	

	}

	public static void resume() 
	{
		if(mPlayer == null) return;
		if(mPlayer.isPlaying()) return;

		try {
			mPlayer.start();
		} catch (Exception e) {
			Log.e(TAG, "MediaPlayer chrashed @ resume()", e);
			Log.e(TAG, e.getMessage(), e);
		}	
	}

	public static void release() 
	{
		if (mPlayer == null) return;
		try {
				if (mPlayer.isPlaying()) 
				{
					mPlayer.stop();
				}
				mPlayer.reset();
				mPlayer.release();
			
		} catch (Exception e) {
			Log.e(TAG, "MediaPlayer crashed @ release()", e);
			Log.e(TAG, e.getMessage(), e);
		}
	}
}