package com.vishwa.picspeak;

import android.content.Context;

public class Scores{

	private int mTotalScore;
	private int mHighestStreak;

	public ScoresDbAdapter mScoresDb;
	/**
	 * Initialize a new Scores object for keeping track of a user's scores.
	 */
	public Scores(Context ctx) {
		mTotalScore = 0;
		mScoresDb = new ScoresDbAdapter(ctx);
		mScoresDb.open();
	}


	/**
	 * Returns the total score (accumulated over all of the 
	 * games the user has played)
	 * @return the total score
	 */
	public int getTotalScore() {
		int totalScore = mScoresDb.getScore("totalscore");
		if(totalScore != -1)
		{
			mTotalScore = totalScore;
			return mTotalScore;
		}
		else
		{
			mScoresDb.addScore("totalscore", mTotalScore);
			return 0;
		}
	}


	/**
	 * Set the total score
	 * @param val the new value 
	 * @return true if success, false if val is < 0
	 */
	public boolean setTotalScore(int val) {
		if(val > 0) {
			mTotalScore = val;
			
			int result = mScoresDb.getScore("totalscore");
			if(result != -1)
			{
				mScoresDb.updateScore("totalscore", mTotalScore);
				return true;
			}
			else
			{
				mScoresDb.addScore("totalscore", mTotalScore);
				return true;
			}
		}
		else {
			return false;
		}
	}
	


	/**
	 * Increment the total score by the amount val
	 * @param val  the amount to increment the total score by
	 * @return true if success, false if val < 0
	 */
	public boolean incTotalScore(int val) {
		if(val > 0) {
			mTotalScore += val;
			setTotalScore(mTotalScore);
			return true;
		}
		else {
			return false;
		}
	}


	/**
	 * Given a stimulus set, returns the high score for that set
	 * @param set the name of the stimulus set
	 * @return the high score, or -1 if set was not a valid name
	 */
	public int getHighScore(String set) {
		
		int setHighScore = mScoresDb.getScore(set+"score");
		if(setHighScore != -1)
		{
			return setHighScore;
		}
		else
		{
			mScoresDb.addScore(set+"score", 0);
			return 0;
		}
	}


	/**
	 * Sets the high score of a stimulus set.
	 * @param set  The stimulus set to set
	 * @param val  The value of the new high score
	 * @return true if success, false if val is negative or if 
	 * the set does not exist
	 */
	public boolean setHighScore(String set, int val) {

		int setHighScore = mScoresDb.getScore(set+"score");
		if(setHighScore != -1)
		{
			mScoresDb.updateScore(set+"score", val);
		}
		else
		{
			mScoresDb.addScore(set+"score", val);
		}
		
		return true;
	}

	public void setNumCompleted(String set, int val) {
		
		int currentNumCompleted = mScoresDb.getScore(set+"completed");
		if(currentNumCompleted != -1)
		{
			mScoresDb.updateScore(set+"completed", val);
		}
		else
		{
			mScoresDb.addScore(set+"completed", val);
		}
		
	}
	
	public int getNumCompleted(String set) {
		
		int currentNumCompleted = mScoresDb.getScore(set+"completed");
		if(currentNumCompleted != -1)
		{
			return currentNumCompleted;
		}
		else
		{
			mScoresDb.addScore(set+"completed", 0);
			return 0;
		}
	}


	/**
	 * Returns the highest streak (over all of the 
	 * games the user has played)
	 * @return the highest streak of correct answers
	 */
	public int getHighestStreak() {
		
		int streak = mScoresDb.getScore("higheststreak");
		if(streak != -1)
		{
			mHighestStreak = streak;
			return mHighestStreak;
		}
		else
		{
			mScoresDb.addScore("higheststreak", 0);
			return 0;
		}
		
	}


	/**
	 * Set the highest streak of correct answers
	 * @param val the new value 
	 * @return true if success, false if val is < 0
	 */
	public boolean setHighestStreak(int val) {
		
		if(val > 0) {
			mHighestStreak = val;
			int score = mScoresDb.getScore("higheststreak");
			if(score != -1)
			{
				mScoresDb.updateScore("higheststreak", mHighestStreak);
				return true;
			}
			else
			{
				mScoresDb.addScore("higheststreak", mHighestStreak);
				return true;
			}
		}
		else {
			return false;
		}
	}
	
	public void closeDb() {
		mScoresDb.close();
	}


}

