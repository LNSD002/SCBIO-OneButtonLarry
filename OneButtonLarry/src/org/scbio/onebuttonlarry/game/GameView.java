package org.scbio.onebuttonlarry.game;

import java.util.ArrayList;
import java.util.Random;

import org.scbio.onebuttonlarry.PreferencesManager;
import org.scbio.onebuttonlarry.R;
import org.scbio.onebuttonlarry.game.GameStage.OnStageFinishListener;
import org.scbio.onebuttonlarry.stage.GapJumpStage;
import org.scbio.onebuttonlarry.stage.PlatformsStage;
import org.scbio.onebuttonlarry.stage.RockStage;
import org.scbio.onebuttonlarry.stage.RunStopStage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class GameView extends View implements OnStageFinishListener{

	private static final int PROCESS_PERIOD = 50;
	private static final int TOTAL_RANDSTAGES = 3;

	private Activity parent;
	private GameThread thread = new GameThread();
	private boolean gameSoundEffects = true;
	private ArrayList<String> stagesPlayed = new ArrayList<String>(TOTAL_RANDSTAGES);

	private GameStage currentStage;
	private GameStage nextStage;
	private OnGameListener onGameListener;

	private long totalTaps = 0;

	/**
	 * GameView constructors
	 */
	public GameView(Context context) {
		super(context);

		setGameSoundEffectsState(PreferencesManager.loadMusicPreference(context));
		currentStage = new GapJumpStage(getContext(), this);

		this.setBackgroundResource(currentStage.getStageBackground());
		currentStage.setOnStageFinishListener(this);
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setGameSoundEffectsState(PreferencesManager.loadMusicPreference(context));
		currentStage = new GapJumpStage(getContext(), this);

		this.setBackgroundResource(currentStage.getStageBackground());
		currentStage.setOnStageFinishListener(this);
	}

	/**
	 * Set parent method
	 * @param parent Parent activity
	 */
	public void setParent(Activity parent) {
		this.parent = parent;
	}

	public boolean areGameSoundEffectsEnabled() {
		return gameSoundEffects;
	}

	public void setGameSoundEffectsState(boolean gameSoundEffects) {
		this.gameSoundEffects = gameSoundEffects;
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
		if(!thread.isPaused())
		{	
			totalTaps++;
			parent.runOnUiThread(new Runnable()
			{			
				@Override
				public void run() {
					TextView tIndicator = (TextView) parent.findViewById(R.id.tapsIndicator);
					tIndicator.setText(String.valueOf(totalTaps));
				}
			});

			this.currentStage.onTap();
		}
		return super.onTouchEvent(event);	
	}

	/*
	 * On stage finished listener method.
	 * Called when a Stage is finished in order to set next stage. 
	 */
	@Override
	public void onStageFinish() { 
		if(stagesPlayed.size()<TOTAL_RANDSTAGES)
		{
			Random rand = new Random();
			do{			
				switch (rand.nextInt(TOTAL_RANDSTAGES)) {
				case 0:
					nextStage = new PlatformsStage(getContext(), this);
					break;
				case 1:
					nextStage = new RunStopStage(getContext(), this);
					break;
				default:
				case 2:
					nextStage = new RockStage(getContext(), this);
					break;
				}
			}while(stagesPlayed.indexOf(nextStage.getClass().toString())>=0);

			stagesPlayed.add(nextStage.getClass().toString());
			setNextStage();
		}else{
			endGame();
		}
	}

	private void setNextStage() {
		nextStage.setOnStageFinishListener(this);
		parent.runOnUiThread(new Runnable()
		{			
			@Override
			public void run() {
				GameView game = (GameView) parent.findViewById(R.id.GameView);
				game.setBackgroundResource(nextStage.getStageBackground());
			}
		});
		currentStage = nextStage;
	}

	/**
	 * Game Thread class.
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
							Log.e(GameThread.class.toString(), 
									"GameThread run() crashes @wait()", e);
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
	}

	public void endGame(){
		finishGame();
		if(onGameListener != null) 
			this.onGameListener.onGameEnd(totalTaps);
	}

	/**
	 * Compound callback method.
	 * Formed by onFinish() and onPause().
	 */
	public interface OnGameListener{
		public void onGamePause();
		public void onGameEnd(long taps);
	}

	public void setOnGameListener(OnGameListener onGameListener) {
		this.onGameListener = onGameListener;
	}

	public OnGameListener getOnGameListener() {
		return onGameListener;
	}
}
