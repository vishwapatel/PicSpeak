package edu.upenn.cis350.mosstalkwords.test;

import android.content.Intent;
import android.graphics.Typeface;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import edu.upenn.cis350.mosstalkwords.EndSet;
import edu.upenn.cis350.mosstalkwords.R;
import edu.upenn.cis350.mosstalkwords.Scores;

public class EndSetTest extends ActivityInstrumentationTestCase2<EndSet> {

	private EndSet act;
	private Button finishButton;
	
	public EndSetTest() {
		super("edu.upenn.cis350.mosstalkwords", EndSet.class);
	}
	
	public void setUp() {		
		//create a new intent to manually tell EndSet what stimulus set to use
		//  (this substitutes for what MainActivity would normally pass to EndSet)
		Intent i = new Intent();
		i.putExtra("set", "nonlivingthingseasy");
		i.putExtra("setscore", 15);
		i.putExtra("newstreak", true);
		i.putExtra("newNumCorrect", true);
		i.putExtra("numCorrect", 8);
		
		setActivityIntent(i);  //later EndSet calls to getIntent will return this (we need this to 
								// initialize set directories, etc)

	}
	
	
	//check to make sure EndSet gets sent data
	public void testIntent() {
		
		act = (EndSet) this.getActivity();
		
		assertTrue(act.set.equals("nonlivingthingseasy"));
		assertTrue(act.newStreak);
		assertTrue(act.setscore == 15);
		assertTrue(act.newNumCorrect);
		
		act.finish();
		
	}
	
	
	/*
	//test reading/writing from the database
	public void testDb() {
		
		Scores s = new Scores(this.getInstrumentation().getTargetContext());
		
		s.setHighestStreak(30);
		s.setHighScore("nonlivingthingseasy", 10);
		
		s.closeDb();
		
		s = new Scores(this.getInstrumentation().getTargetContext());
		
		assertEquals("get streak", 30, s.getHighestStreak());
		
		assertEquals("get highscore", 10, s.getHighScore("nonlivingthingseasy"));
		s.closeDb();

	}
	*/
	
	/**
	 * Tests if the activity's layout gets updated correctly
	 * for the input parameters passed via the Intent.
	 */
	public void testLayout() {
		
		act = (EndSet) this.getActivity();
		
		assertTrue(act.setscoretext.getText().toString().equals("15"));
		
		assertTrue(act.correct.getText().toString().equals("8/10 Correct"));
		
		act.finish();

	}
	
	
	
	/**
	 * Test if clicking the finish button closes the activity
	 */
	public void testFinish() {
		
		act = (EndSet) this.getActivity();
		finishButton = (Button) act.findViewById(R.id.endset_button);
		
		act.runOnUiThread(new Runnable() {
			public void run() {
				finishButton.performClick();
			}
		});
		
		getInstrumentation().waitForIdleSync();  // wait for the UI to finish
			
		assertTrue("endset finished", act.isFinishing());
	}

	
	
}
