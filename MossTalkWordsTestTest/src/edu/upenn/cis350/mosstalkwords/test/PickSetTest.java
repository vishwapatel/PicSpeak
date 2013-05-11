package edu.upenn.cis350.mosstalkwords.test;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.test.ActivityInstrumentationTestCase2;
import edu.upenn.cis350.mosstalkwords.PickSet;

public class PickSetTest extends ActivityInstrumentationTestCase2<PickSet> {

	private PickSet act;
	
	public PickSetTest() {
		super("edu.upenn.cis350.mosstalkwords", PickSet.class);
	}
	
	public void setUp() {
		
		act = (PickSet) this.getActivity();
	}
	
	//WARNING:  THESE TESTS REQUIRE DATA/WIFI TURNED ON TO TEST DOWNLOADING
	
	/**
	 * test if categories and words are downloaded successfully
	 * @throws InterruptedException
	 */
	public void testCatDownload() throws InterruptedException {
		
		int i = 0;
		
		while(act.getDownloadCatsStatus() != AsyncTask.Status.FINISHED && i < 200) {
			Thread.sleep(500);
			i++;
		}
		
		if(i >= 200)
			fail();
	}
	
	
	/**
	 * Test whether we can get a set, and that the set is the 
	 * correct size (the set is random, so cant check if words are right)
	 * @throws InterruptedException
	 */
	public void testGetSet() throws InterruptedException {
		
		//wait for download
		while(act.getDownloadCatsStatus() != AsyncTask.Status.FINISHED ) {
			Thread.sleep(500);
		}
		
		ArrayList<String> res = act.getSet("nonlivingthingshard");
		
		assertNotNull(res);
		
		assertTrue(res.size() == 10);
		
	}
	
	
	
}
