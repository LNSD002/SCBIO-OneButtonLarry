package org.scbio.onebuttonlarry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

public class GameActivity extends Activity {

	private GameView gameView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_game);
		
		gameView = (GameView) findViewById(R.id.GameView);
		gameView.setParent(this);		
	}


	@Override
	protected void onResume() {
		super.onResume();
		MusicManager.resume();
	}
	
	@Override
	public void onBackPressed() {
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("score", 222l); //TODO Test code
		setResult(RESULT_OK, returnIntent);
		finish();
		super.onBackPressed();
	}

}
