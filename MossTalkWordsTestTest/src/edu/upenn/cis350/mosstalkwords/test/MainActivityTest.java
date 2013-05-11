package edu.upenn.cis350.mosstalkwords.test;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import edu.upenn.cis350.mosstalkwords.MainActivity;
import edu.upenn.cis350.mosstalkwords.PickSet;
import edu.upenn.cis350.mosstalkwords.R;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity act;
	private Button hintPhraseButton;
	private Button hintRhymeButton;
	private Button hintPronounceButton;
	private Button skipButton;
	
	public MainActivityTest() {
		super("edu.upenn.cis350.mosstalkwords", MainActivity.class);
	}
	
	public void setUp() {		
		//create a new intent to manually tell MainActivity what stimulus set to use
		//  (this substitutes for what PickSet would normally pass to MainActivity)
		Intent i = new Intent();
		i.putExtra(PickSet.currentSetPath, "nonlivingthingshard");
		
		ArrayList<String> curr_set = new ArrayList<String>();
		curr_set.add("parachute");
		curr_set.add("freezer");
		curr_set.add("marker");
		curr_set.add("icicle");
		curr_set.add("boomerang");
		curr_set.add("calculator");
		curr_set.add("stadium");
		curr_set.add("motorcycle");
		curr_set.add("toothbrush");
		curr_set.add("spatula");
		i.putStringArrayListExtra(PickSet.currentSet,curr_set);
		
		setActivityIntent(i);  //later MainActivity calls to getIntent will return this (we need this to 
								// initialize set directories, etc

		act = (MainActivity) this.getActivity();
		
		
		hintPhraseButton = (Button) act.findViewById(R.id.hintbuttona);
        hintRhymeButton = (Button) act.findViewById(R.id.hintbuttonb);
        hintPronounceButton = (Button) act.findViewById(R.id.hintbuttonc);
        skipButton = (Button) act.findViewById(R.id.skipbutton);
	}
	
	//HINT TESTS DONT WORK IN EMULATOR BECAUSE TEXT TO SPEECH DOESNT WORK
	/**
	 * Test if clicking the Phrase hint button increments the 
	 * numHintsUsed.
	 */
	public void testHintPhrase() throws InterruptedException {
		assertEquals("hints used at startup", 0, act._numHintsUsed);

		while(act.getDownloadHintsStatus() != AsyncTask.Status.FINISHED &&
				act.getDownloadFilesStatus() != AsyncTask.Status.FINISHED) {
			Thread.sleep(500);
		}
		
		act.runOnUiThread(new Runnable() {
			public void run() {
				hintPhraseButton.performClick();
			}
		});
		
		getInstrumentation().waitForIdleSync();  // wait for the UI to finish
		
		assertEquals("hints used after phrase click", 1, act._numHintsUsed);
	}
	
	/**
	 * Test if clicking the Rhyme hint button increments the 
	 * numHintsUsed.
	 * @throws InterruptedException 
	 */
	public void testHintRhyme() throws InterruptedException {
		assertEquals("hints used at startup", 0, act._numHintsUsed);
		
		while(act.getDownloadHintsStatus() != AsyncTask.Status.FINISHED &&
				act.getDownloadFilesStatus() != AsyncTask.Status.FINISHED) {
			Thread.sleep(500);
		}
		
		act.runOnUiThread(new Runnable() {
			public void run() {
				hintRhymeButton.performClick();
			}
		});
		
		getInstrumentation().waitForIdleSync();  // wait for the UI to finish
		
		assertEquals("hints used after rhyme click", 1, act._numHintsUsed);
	}
	
	/**
	 * Test if clicking the Pronounce hint button increments the 
	 * numHintsUsed.
	 * @throws InterruptedException 
	 */
	public void testHintPronounce() throws InterruptedException {
		assertEquals("hints used at startup", 0, act._numHintsUsed);
		
		while(act.getDownloadHintsStatus() != AsyncTask.Status.FINISHED &&
				act.getDownloadFilesStatus() != AsyncTask.Status.FINISHED) {
			Thread.sleep(500);
		}
		
		act.runOnUiThread(new Runnable() {
			public void run() {
				hintPronounceButton.performClick();
			}
		});
		
		getInstrumentation().waitForIdleSync();  // wait for the UI to finish
		
		assertEquals("hints used after pronounce click", 1, act._numHintsUsed);
	}
	
	/**
	 * Test if clicking the skip button resets variables.
	 * @throws InterruptedException 
	 */
	public void testSkip() throws InterruptedException {
		act._numHintsUsed = 3;
		
		while(act.getDownloadHintsStatus() != AsyncTask.Status.FINISHED &&
				act.getDownloadFilesStatus() != AsyncTask.Status.FINISHED) {
			Thread.sleep(500);
		}
		
		act.runOnUiThread(new Runnable() {
			public void run() {
				skipButton.performClick();
			}
		});
		
		getInstrumentation().waitForIdleSync();  // wait for the UI to finish
			
		assertEquals("hints used after skipping", 0, act._numHintsUsed);
		assertEquals("tries after skipping", 0, act._numTries);
		assertEquals("rhyme num after skipping", 0, act._rhymeUsed);
	}


	
	/**
	 * Test whether a successful return from EndSet will cause this 
	 * activity to finish correctly
	 */
	public void testFinish() {
		
		//send a fake notice that EndSet has ended
		act.onActivityResult(2, Activity.RESULT_OK, null);
		
		assertTrue(act.isFinishing());
	}
	
}
