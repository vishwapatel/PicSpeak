package com.vishwa.picspeak;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import org.apache.http.util.ByteArrayBuffer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

public class PickSet extends Activity {
	private String mCurrentSetPath = "mCurrentSetPath";
	private String mCurrentSet = "mCurrentSet";
	private ArrayList<String> mCategoriesList;
	private ListView mCategoriesListView;
	private String mDifficulty;
	private String mCategory;
	private Scores mUserScores;
	
	private Button mReportButton;
	
	private StatsDbAdapter mStatsDb;
	
	private AsyncTask<String, Integer, Boolean> mLoadCategories;
	private TreeMap<String, ArrayList<String>> mCategoryWords;
	private TreeMap<String, Integer> mCategorySizes = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> mNumCategoryWordsFinished = new TreeMap<String, Integer>();
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick);
		
		if(!isOnline())
		{
			createAlert("No internet detected!", "This app cannot run without the internet!");
		}
		else
		{
			mUserScores = new Scores(getApplicationContext());
			//initialize mCategory and mDifficulty
			mCategory = "livingthings";
			mDifficulty = "easy";
			
			mLoadCategories = new LoadCategoriesAsyncTask().execute("");
			
			mStatsDb = new StatsDbAdapter(this.getApplicationContext());
			mStatsDb.open();
			
			mReportButton = (Button) findViewById(R.id.reportButton);
			
	        mReportButton.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View arg0) {
					String email_body = mStatsDb.getStats();
					
					Intent email = new Intent(Intent.ACTION_SEND);		  
					email.putExtra(Intent.EXTRA_SUBJECT, "PicSpeak Report");
					email.putExtra(Intent.EXTRA_TEXT, email_body);
					email.setType("message/rfc822");
					startActivity(Intent.createChooser(email, "Choose an Email client :"));
				}
	        	
	        });
		}
	}

	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	private void createAlert(String errorTitle, String errorMessage)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setTitle(errorTitle);
 		dialog.setMessage(errorMessage);
 		dialog.setIcon(android.R.drawable.ic_dialog_alert);
 		dialog.setPositiveButton("Close", new DialogInterface.OnClickListener(){
 			
 			public void onClick(DialogInterface arg0, int arg1) {
 				finish();
 				return;
 			}
 		});
 		dialog.show();
	}

	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  updateListView();
	}
	
	 private boolean checkUnlocked(String cat, String diff){
		 String mCategory = cat.replaceAll("\\s","");
		 mCategory = mCategory.toLowerCase();
		 if (diff.equals("Easy")){
			return true; 
		 }
		 if(diff.equals("Medium")){
			 int prevLevelScore = mUserScores.getNumCompleted(mCategory+"easy");
			 if(prevLevelScore >= 6){
				 return true;
			 }
		 }
		 if(diff.equals("Hard")){
			 int prevLevelScore = mUserScores.getNumCompleted(mCategory+"medium");
			 if(prevLevelScore >= 6){
				 return true;
			 }
		 }
		 return false;
	 }
	 private ArrayList<Set> getSetList(){
		 String [] cats = getResources().getStringArray(R.array.stimulus_array);
		 String [] diffs = getResources().getStringArray(R.array.difficulty_array);
		 ArrayList<Set> setlist = new ArrayList<Set>();
		 for (String cat : cats){
			 for (String diff : diffs){
				 String mCategory = cat+diff;
				 mCategory = mCategory.replaceAll("\\s","");
				 mCategory = mCategory.toLowerCase();
				 String score = getPercentageOfCategoryCompleted(mCategory);
				 boolean unlocked = checkUnlocked(cat, diff);
				 setlist.add(new Set(score, cat, diff, unlocked));
			 }
		 }
		 return setlist;
	 }
	 public void setListViewInfo(){
		ArrayList<Set> sets = getSetList();
		SetAdapter adapter= new SetAdapter(this,R.layout.listview_item, sets); 
		mCategoriesListView = (ListView)findViewById(R.id.listView1);
		View header = (View)getLayoutInflater().inflate(R.layout.listview_header, null);
		mCategoriesListView.addHeaderView(header);
		mCategoriesListView.setAdapter(adapter);
		mCategoriesListView.setOnItemClickListener(new SetSelectedListener());      
	}
	 
	 public void updateListView(){
		 ArrayList<Set> newSetList = getSetList();
		 mCategoriesListView = (ListView)findViewById(R.id.listView1);
		 HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) mCategoriesListView.getAdapter();
		 ArrayAdapter<Set> adap = (ArrayAdapter<Set>) headerViewListAdapter.getWrappedAdapter();
		 if(adap != null){
			 adap.clear();
			 for(Set set : newSetList){
				 adap.add(set);
			 }
		 }   
	 }
	    
	
	public String getPercentageOfCategoryCompleted(String mCategory)
	{
		int numCompleted = mUserScores.getNumCompleted(mCategory);
		mNumCategoryWordsFinished.put(mCategory, numCompleted);
		return mNumCategoryWordsFinished.get(mCategory) + "/" + 10;
	}

	public void start(View view){
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra(mCurrentSetPath, mCategory+mDifficulty);
		ArrayList<String> newSet = getSet(mCategory+mDifficulty);
		i.putStringArrayListExtra(mCurrentSet, newSet);
		AsyncTask<String, Integer, Boolean> downloadFirstFile = new LoadOneFile().execute(mCategory+mDifficulty+"/",newSet.get(0));
		startActivityForResult(i,1);
	}

	public ArrayList<String> getSet(String key){
		ArrayList<String> fullSet = mCategoryWords.get(key);
		Collections.shuffle(fullSet);
		if (fullSet.size() > 10){
			ArrayList<String> newSet = new ArrayList<String>();
			for(int i = 0; i<10; i++){
				newSet.add(fullSet.get(i));
			}
			return newSet;
		}
		else{
			return fullSet;
		}
	}
	
	public class SetSelectedListener implements OnItemClickListener {

		@SuppressLint("DefaultLocale")
		public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {
			Set chosen = (Set)parent.getItemAtPosition(pos);
			if(chosen.isSetLocked()){
				popLockedAlert();
			}
			else{
			mCategory = chosen.getSetCategory();
			mCategory = mCategory.replaceAll("\\s","");
			mCategory = mCategory.toLowerCase();
			mDifficulty = chosen.getSetDifficulty();
			mDifficulty = mDifficulty.toLowerCase();
			start(view);
			}
			
		}

		private void popLockedAlert() {
			AlertDialog alertDialog = new AlertDialog.Builder(PickSet.this).create();
			alertDialog.setTitle("Level Locked!");
			alertDialog.setMessage("That level is locked. Try an easier level first!");
			alertDialog.setIcon(R.drawable.padlock_2);
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.show();
		}
	}
	
	
	
	private class LoadCategoriesAsyncTask extends AsyncTask<String, Integer, Boolean>{
		@Override
		protected Boolean doInBackground(String... set) {
			mCategoriesList = new ArrayList<String>();
			mCategoryWords = new TreeMap<String, ArrayList<String>>();
			boolean b = false;
			try {
					URL ur = new URL(MainActivity.S3_BUCKET_URL + "mCategoriesList.txt");
					BufferedReader categoryReader = new BufferedReader(new InputStreamReader(ur.openStream()));
					String lineRead;
					while ((lineRead = categoryReader.readLine()) != null){
						b = true;
						mCategoriesList.add(lineRead);
					}
					categoryReader.close();
					for (String cat: mCategoriesList){
						try{
						URL urwords = new URL(MainActivity.S3_BUCKET_URL + cat + "/words.txt");
						BufferedReader wordsReader = new BufferedReader(new InputStreamReader(urwords.openStream()));
						String word;
						ArrayList<String> wordslist = new ArrayList<String>();
						int count = 0;
						while ((word = wordsReader.readLine()) != null){
							wordslist.add(word);
							count++;
						}

						mCategoryWords.put(cat, wordslist);
						mCategorySizes.put(cat, count);
						
						int numCompleted = mUserScores.getNumCompleted(cat);
						mNumCategoryWordsFinished.put(cat, numCompleted);
						
						wordsReader.close();
						}
						catch(FileNotFoundException e){
							e.printStackTrace();
						}
					}
			}catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		 return b;
		}
		

		@Override
		protected void onPostExecute(Boolean result) {
			setListViewInfo();
			
		}

	}
	
	private class LoadOneFile extends AsyncTask<String, Integer, Boolean>{
		private final ProgressDialog dialog = new ProgressDialog(PickSet.this);
		@Override
		protected void onPreExecute (){
			this.dialog.setMessage("Downloading Set! Get Ready!"); 
			this.dialog.show();
		}
		@Override
		protected void onPostExecute (Boolean result){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.dialog.dismiss();
		}
		@Override
		protected Boolean doInBackground(String... firstImagePath) {
			boolean b = false;

			try {
					URL ur = new URL(MainActivity.S3_BUCKET_URL + firstImagePath[0]+firstImagePath[1] + ".jpg");
					File file = new File(getApplicationContext().getCacheDir(),  firstImagePath[1] +".jpg");
					if (file.exists() == false){
					URLConnection ucon = ur.openConnection();
					InputStream is = ucon.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current = 0;
					while ((current = bis.read()) != -1)
						baf.append((byte) current);
					
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(baf.toByteArray());
					fos.close();
					b = true;
				}
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return b;
		}

	}
	
	
	public AsyncTask.Status getLoadCategoriesStatus() {
		return mLoadCategories.getStatus();
	}
	
	@Override 
	public void onRestart() {     
	  super.onRestart(); 
	  updateListView();
	}
	
	@Override
	protected void onDestroy() {
		mStatsDb.close();
		super.onDestroy();
	}
	
	
	
}
