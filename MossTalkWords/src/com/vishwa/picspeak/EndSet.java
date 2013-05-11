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

	public String set;
	public int setscore;
	public boolean newStreak;
	public boolean newNumCorrect;
	
	//public for testing purposes
	public TextView totalscoretext;
	public TextView setscoretext;
	public TextView highscoretext;
	public ImageView highscorestamp;
	public TextView correct;
	public ImageView star1;
	public ImageView star2;
	public ImageView star3;
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end);
		
		//the name of the set
		set = getIntent().getStringExtra("set");
		TextView settext = (TextView) findViewById(R.id.endset_set);
		settext.setText(getSetName(set));
		
		//the set score
		setscore = getIntent().getIntExtra("setscore", 0);
		setscoretext = (TextView) findViewById(R.id.endset_setscore);
		setscoretext.setText(Integer.toString(setscore));
		
		Scores scores = new Scores(getApplicationContext());
		
		//the high score
		highscoretext = (TextView) findViewById(R.id.endset_highscore);
		highscoretext.setText("High Score: " + scores.getHighScore(set));
		if(setscore == scores.getHighScore(set) && setscore > 0) {
			//highlight the fact that they got a new high score
			highscoretext.setTextColor(Color.parseColor("#288C8C"));
			highscoretext.setTypeface(null, Typeface.BOLD);
			
			//display the high score stamp
			highscorestamp = (ImageView) findViewById(R.id.endset_stamp);
			highscorestamp.setImageResource(R.drawable.high_score_stamp);
		}
		
		//the total score  (set it to what it was before this set, because we will
		//  animate the new points)
		int prev_total = scores.getTotalScore() - setscore;
		
		totalscoretext = (TextView) findViewById(R.id.endset_totalscore);
		totalscoretext.setText(Integer.toString(prev_total));
		
		//the streak
		newStreak = getIntent().getBooleanExtra("newstreak", false);
		TextView streaktext = (TextView) findViewById(R.id.endset_streak);
		streaktext.setText("Longest Streak: " + scores.getHighestStreak());
		
		if(newStreak && scores.getHighestStreak() > 0) {
			//highlight the fact that they got a new streak 
			streaktext.setTextColor(Color.parseColor("#288C8C"));
			streaktext.setTypeface(null, Typeface.BOLD);
		}
		
		//num correct and star count
		newNumCorrect = getIntent().getBooleanExtra("newNumCorrect",false);
		int numCorrect = getIntent().getIntExtra("numCorrect", 0);
		
		correct = (TextView) findViewById(R.id.endset_correct_count);
		correct.setText(Integer.toString(numCorrect) + "/10 Correct");
		
		if(newNumCorrect) {
			//highlight the fact that they got a new number of correct pics
			correct.setTextColor(Color.parseColor("#288C8C"));
			correct.setTypeface(null, Typeface.BOLD);
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
		new IncScore().execute(prev_total, scores.getTotalScore(), setscore);
		
	}
	
	/**
	 * When user clicks finish, end this activity
	 * @param view
	 */
	public void onEndButtonClick(View view){
		setResult(RESULT_OK);
		finish();
	}
	
	
	public String getSetName(String set) {
		
		if(set.equals("livingthingseasy")) {
			return "Living Things Easy";
		}
		else if(set.equals("livingthingsmedium")) {
			return "Living Things Medium";
		}
		else if(set.equals("livingthingshard")) {
			return "Living Things Hard";
		}
		else if(set.equals("nonlivingthingseasy")) {
			return "Non Living Things Easy";
		}
		else if(set.equals("nonlivingthingsmedium")) {
			return "Non Living Things Medium";
		}
		else if(set.equals("nonlivingthingshard")) {
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
			totalscoretext.setText(current[0].toString());
			setscoretext.setText(current[1].toString());
		}
		
		protected void onPostExecute(Void voids) {
			setscoretext.setVisibility(View.INVISIBLE);
		}

	}

}
