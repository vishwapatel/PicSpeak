package com.vishwa.picspeak;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EndSet extends Activity {

	private String mSet;
	private int mSetScore;
	private boolean mIsNewStreak;
	private boolean mIsNewNumCorrectRecord;
	
	private TextView mTotalScoreTextView;
	private TextView mSetScoreTextView;
	private TextView mHighscoreTextView;
	private ImageView mHighscoreStamp;
	private TextView mCorrectTextView;
	private ImageView star1;
	private ImageView star2;
	private ImageView star3;
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end);
		
		//the name of the mSet
		mSet = getIntent().getStringExtra("mSet");
		TextView settext = (TextView) findViewById(R.id.endset_set);
		settext.setText(getSetName(mSet));
		
		//the mSet score
		mSetScore = getIntent().getIntExtra("mSetScore", 0);
		mSetScoreTextView = (TextView) findViewById(R.id.endset_setscore);
		mSetScoreTextView.setText(Integer.toString(mSetScore));
		
		Scores scores = new Scores(getApplicationContext());
		
		//the high score
		mHighscoreTextView = (TextView) findViewById(R.id.endset_highscore);
		mHighscoreTextView.setText("High Score: " + scores.getHighScore(mSet));
		if(mSetScore == scores.getHighScore(mSet) && mSetScore > 0) {
			//highlight the fact that they got a new high score
			mHighscoreTextView.setTextColor(Color.parseColor("#288C8C"));
			mHighscoreTextView.setTypeface(null, Typeface.BOLD);
			
			//display the high score stamp
			mHighscoreStamp = (ImageView) findViewById(R.id.endset_stamp);
			mHighscoreStamp.setImageResource(R.drawable.high_score_stamp);
		}
		
		//the total score  (mSet it to what it was before this mSet, because we will
		//  animate the new points)
		int prev_total = scores.getTotalScore() - mSetScore;
		
		mTotalScoreTextView = (TextView) findViewById(R.id.endset_totalscore);
		mTotalScoreTextView.setText(Integer.toString(prev_total));
		
		//the streak
		mIsNewStreak = getIntent().getBooleanExtra("newstreak", false);
		TextView streaktext = (TextView) findViewById(R.id.endset_streak);
		streaktext.setText("Longest Streak: " + scores.getHighestStreak());
		
		if(mIsNewStreak && scores.getHighestStreak() > 0) {
			//highlight the fact that they got a new streak 
			streaktext.setTextColor(Color.parseColor("#288C8C"));
			streaktext.setTypeface(null, Typeface.BOLD);
		}
		
		//num mCorrectTextView and star count
		mIsNewNumCorrectRecord = getIntent().getBooleanExtra("mIsNewNumCorrectRecord",false);
		int numCorrect = getIntent().getIntExtra("numCorrect", 0);
		
		mCorrectTextView = (TextView) findViewById(R.id.endset_correct_count);
		mCorrectTextView.setText(Integer.toString(numCorrect) + "/10 Correct");
		
		if(mIsNewNumCorrectRecord) {
			//highlight the fact that they got a new number of mCorrectTextView pics
			mCorrectTextView.setTextColor(Color.parseColor("#288C8C"));
			mCorrectTextView.setTypeface(null, Typeface.BOLD);
		}
		
		star1 = (ImageView) findViewById(R.id.endset_star1);
		star2 = (ImageView) findViewById(R.id.endset_star2);
		star3 = (ImageView) findViewById(R.id.endset_star3);
		
		if(numCorrect >= 6) {
			star1.setImageResource(R.drawable.star);
		}
		
		if(numCorrect >= 8) {
			star2.setImageResource(R.drawable.star);
		}
		
		if(numCorrect == 10) {
			star3.setImageResource(R.drawable.star);
		}
		
		
		//call the asynctask to increment the previous total to the new total
		new IncScore().execute(prev_total, scores.getTotalScore(), mSetScore);
		
	}
	
	/**
	 * When user clicks finish, end this activity
	 * @param view
	 */
	public void onEndButtonClick(View view){
		setResult(RESULT_OK);
		finish();
	}
	
	
	public String getSetName(String mSet) {
		
		if(mSet.equals("livingthingseasy")) {
			return "Living Things Easy";
		}
		else if(mSet.equals("livingthingsmedium")) {
			return "Living Things Medium";
		}
		else if(mSet.equals("livingthingshard")) {
			return "Living Things Hard";
		}
		else if(mSet.equals("nonlivingthingseasy")) {
			return "Non Living Things Easy";
		}
		else if(mSet.equals("nonlivingthingsmedium")) {
			return "Non Living Things Medium";
		}
		else if(mSet.equals("nonlivingthingshard")) {
			return "Non Living Things Hard";
		}
		else{
			return null;
		}
		
	}
	
	
	
	/**
	 * AsyncTask class used for animating the score updating.
	 * Takes in prev_total, new_total, and set_score, and counts 
	 * prev_total up to new_total while counting set_score down to 0.
	 */
	private class IncScore extends AsyncTask<Integer, Integer, Void> {		
		
		protected Void doInBackground(Integer... scores) {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			
			while(this.isCancelled() == false && scores[0] <= scores[1] && scores[2] >= 0) {	
				
				publishProgress(scores[0], scores[2]); //Android calls onProgressUpdate
				scores[0]++;
				scores[2]--;
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}
			}
			
			return null;
		}
		
		
		protected void onProgressUpdate(Integer... current) {
			mTotalScoreTextView.setText(current[0].toString());
			mSetScoreTextView.setText(current[1].toString());
		}
		
		protected void onPostExecute(Void voids) {
			mSetScoreTextView.setVisibility(View.INVISIBLE);
		}

	}

}
