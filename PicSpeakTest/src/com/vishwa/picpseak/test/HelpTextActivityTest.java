package com.vishwa.picspeak.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import com.vishwa.picspeak.HelpTextActivity;
import com.vishwa.picspeak.R;

public class HelpTextActivityTest extends ActivityInstrumentationTestCase2<HelpTextActivity> {

	private HelpTextActivity act;
	private Button finishButton;
	
	public HelpTextActivityTest() {
		super("edu.upenn.cis350.mosstalkwords", HelpTextActivity.class);
	}
	
	public void setUp() {		
		act = (HelpTextActivity) this.getActivity();
	}
	
	
	/**
	 * Test if clicking the finish button closes the activity
	 */
	public void testFinish() {
		
		act = (HelpTextActivity) this.getActivity();
		finishButton = (Button) act.findViewById(R.id.backbutton);
		
		act.runOnUiThread(new Runnable() {
			public void run() {
				finishButton.performClick();
			}
		});
		
		getInstrumentation().waitForIdleSync();  // wait for the UI to finish
			
		assertTrue("endset finished", act.isFinishing());
	}

	
	
}
