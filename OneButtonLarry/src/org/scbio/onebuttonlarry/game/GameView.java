package org.scbio.onebuttonlarry.game;

import java.util.HashMap;

import org.scbio.onebuttonlarry.game.GameStage.OnStageFinishListener;
import org.scbio.onebuttonlarry.stage.GapJump;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View implements OnStageFinishListener{

	private static final int PROCESS_PERIOD = 50;

	private Activity parent;
	private GameThread thread = new GameThread();

	private GameStage currentStage = new GapJump(getContext(), this);
	private GameStage nextStage;
	private OnGameListener onGameListener;

	private HashMap<String, Long> scores = new HashMap<String, Long>();
	private long totalScore = 0;

	/**
	 * GameView constructors
	 */
	public GameView(Context context) {
		super(context);
		currentStage.setOnStageFinishListener(this);
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		currentStage.setOnStageFinishListener(this);
	}

	/**
	 * Set parent method
	 * @param parent Parent activity
	 */
	public void setParent(Activity parent) {
		this.parent = parent;
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		if(currentStage != null)
			this.currentStage.onDrawStage(canvas);

		super.onDraw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{	
		super.onSizeChanged(w, h, oldw, oldh);
		if(currentStage != null)
			this.currentStage.onSizeChanged(w,h,oldw,oldh);

		before = System.currentTimeMillis();
		thread.start(); 
	}

	/*
	 * On Touch view call.
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		/*if(!thread.isPaused() && event.equals(MotionEvent.ACTION_DOWN))
		{*/
			this.currentStage.onTap();
		//s}
		return super.onTouchEvent(event);	
	}

	/*
	 * On stage finished listener method.
	 * Called when a Stage is finished in order to get number of taps performed. 
	 */
	@Override
	public void onStageFinish(long taps) { // TODO Need completion
		totalScore += taps;


		nextStage.setOnStageFinishListener(this);
		currentStage = nextStage;
	}

	/**
	 * Game Thread class.
	 * 
	 */

	protected class GameThread extends Thread{

		private boolean pause, running;

		public synchronized void pauseGameThread() {
			pause = true;      
		}

		public synchronized void resumeGameThread() {
			pause = false;
			notify();
		}  

		public void finishGameThread() {
			running = false;
			if(pause) resumeGameThread();
		}

		public boolean isPaused() {
			return pause;      
		}

		@Override
		public void run() 
		{	

			running = true;
			while (running) 
			{
				updateGame();

				synchronized(this) 
				{
					while(pause){
						try{
							wait();
						}catch (Exception e){
							Log.e(GameThread.class.toString(), "GameThread run() crashes @wait()", e);
						}
					}
				}
			}

		}

	}

	private long before = 0;
	private void updateGame(){
		long now = System.currentTimeMillis();

		// Processing period not completed. Do nothing.
		if (before + PROCESS_PERIOD > now) {
			return;
		}

		// Delay calculation. For real-time.          
		double delay = (now - before) / PROCESS_PERIOD;
		before = now; 		
		
		if(delay < 5)
			currentStage.updatePhysics(delay);
	}

	public void resumeGame(){
		this.thread.resumeGameThread();
	}

	public void pauseGame(){
		this.thread.pauseGameThread();

		if(onGameListener != null) 
			this.onGameListener.onGamePause();
	}

	public void finishGame(){
		this.thread.finishGameThread();
		if(onGameListener != null) 
			this.onGameListener.onGameFinish();
	}

	/**
	 * Compound callback method.
	 * Formed by onFinish() and onPause().
	 */
	public interface OnGameListener{
		public void onGamePause();
		public void onGameFinish();
	}

	public void setOnGameListener(OnGameListener onGameListener) {
		this.onGameListener = onGameListener;
	}

	public OnGameListener getOnGameListener() {
		return onGameListener;
	}
}
