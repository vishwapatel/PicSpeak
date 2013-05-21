package com.vishwa.picspeak;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import android.speech.tts.TextToSpeech;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	public final static String S3_BUCKET_URL ="https://s3.amazonaws.com/mosswords/"; 
	private ImageView mImageView;
	private String mCurrentPath;
	private int mCurrentIndex;
	private Button mHintPhraseButton;
	private Button mHintRhymeButton;
	private Button mHintPronounceButton;
	private Button mMicButton;
	private Button mHelpButton;
	private Button mSkipButton;
	private MediaPlayer mMediaPlayer;
	private Bitmap mPhotoBitmap = null;
    private TextView mScoreTextView;

	private ProgressBar mProgressBar;

	private StatsDbAdapter mStatsDb;
	
	private boolean mListenerIsReady = false;
	private TextToSpeech mTextToSpeech;
	private TreeMap<String, String[]> mWordHints; 
	private int mRhymeUsed; 
	private int mTotalScore = 0;
	
	private int mHintWordUsed = 0;
	private int mHintPhraseUsed = 0;
	private int mHintRhymeUsed = 0;
	private String mUserGuess = new String();

	private Scores mUserScores;
	private int mSetScore = 0;
	private int mStreak = 0;
	private boolean mNewStreak = false;
	private int mNumHintsUsed = 0;
	private int mNumTries = 0;
	private String mFeedbackResult = "";
	private ArrayList<String> mCurrentSet;

	private AlertDialog mAlertDialog;
	private TextView mDialogScoreTextView;
	private int mNumCorrect = 0;

	private AsyncTask<String, Integer, Void> mDownloadHintsTask;
	private AsyncTask<String, Integer, Void> mDownloadFilesTask;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.image);
        //for finding image files
        mCurrentIndex = 0;
        mCurrentPath = getIntent().getStringExtra("currentSetPath");
        mCurrentSet = getIntent().getStringArrayListExtra("currentSet");
        
        //scores
        mUserScores = new Scores(this.getApplicationContext());
        mTotalScore = mUserScores.getTotalScore();
        mSetScore = 0;
        mStreak = 0;
        
        mStatsDb = new StatsDbAdapter(this.getApplicationContext());
        mStatsDb.open();
        
        //set score view
        mScoreTextView = (TextView) findViewById(R.id.score);
    	mScoreTextView.setText(Integer.toString(mSetScore));
    	
    	mProgressBar = (ProgressBar)findViewById(R.id.progressBarGame);
    	Resources res = getResources();
    	mProgressBar.setProgressDrawable(res.getDrawable( R.drawable.game_progress));
    	mProgressBar.setMax(100);
    	mProgressBar.setProgress(0);
    	//download images, download mWordHints
    	mDownloadHintsTask = new LoadHintsAsyncTask().execute("");
        mDownloadFilesTask = new LoadFilesAsyncTask().execute("");
        
        //create TextToSpeech
        if(mTextToSpeech == null){
        mTextToSpeech = new TextToSpeech(this, new TextToSpeechListener());
        
        }
    	try {
			loadImage();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    	//Buttons
        mHintPhraseButton = (Button) findViewById(R.id.hintbuttona);
        mHintRhymeButton = (Button) findViewById(R.id.hintbuttonb);
        mHintPronounceButton = (Button) findViewById(R.id.hintbuttonc);
        mMicButton = (Button) findViewById(R.id.micbutton);
        mSkipButton = (Button) findViewById(R.id.skipbutton);
        mHelpButton = (Button) findViewById(R.id.helpbutton);
      
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        mHintPhraseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				mHintPhraseUsed = 1;
				playSoundText("phrase");
				if(mNumHintsUsed < 3)
					mNumHintsUsed++;	
			}
		});
        
        mHintRhymeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				mHintRhymeUsed = 1;
				playSoundText("rhyme");
				if(mNumHintsUsed < 3)
					mNumHintsUsed++;
			}
		});
        
        mHintPronounceButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				mHintWordUsed = 1;
				playSoundText("word");
				if(mNumHintsUsed < 3)
					mNumHintsUsed++;
			}
		});
        
        mHelpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), HelpTextActivity.class);
				startActivity(i);
			}
		});
        
        mMicButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

		        try {
		            startActivityForResult(intent, 1);
		        } catch (ActivityNotFoundException a) {
		        	a.printStackTrace();
		            Toast.makeText(getApplicationContext(),
				                   "Oops! Your device doesn't support Speech to Text",
				                   Toast.LENGTH_SHORT).show();
		        }
			}
		});
        
        mSkipButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mUserScores.getHighestStreak() < mStreak) {
					mUserScores.setHighestStreak(mStreak);
					mNewStreak = true;
				}

				mStreak = 0;
				nextImage();
			}
		});
        
    }
	//Get path to the cache directory file needed 
	private String buildCachePath(String extension){
		if(getApplicationContext() != null){
			if(getApplicationContext().getCacheDir() != null){
				if(getApplicationContext().getCacheDir().getPath() != null){
					if(mCurrentSet == null){
						return "curr set null!";
					}
					else {
						return getApplicationContext().getCacheDir().getPath()+"/"+mCurrentSet.get(mCurrentIndex)+extension; 
					}
				}
			}	
		}
		return "error encountered";
	}

	/**
	 *Async Task to load the images from the bucket and save them to the cache directory
	 */
	private class LoadFilesAsyncTask extends AsyncTask<String, Integer, Void>{
		@Override
		protected Void doInBackground(String... set) {
			try {
				//make sure currentSet has been defined already
				if(mCurrentSet != null){
					for (String word: mCurrentSet){
						//set url for each image
						URL url = new URL(S3_BUCKET_URL + mCurrentPath + 
								"/" + word + ".jpg");
						//create file to be saved in cache directory with word.jpg file naming
						File file = new File(getApplicationContext().getCacheDir(),word+".jpg");
						//if this file doesn't exist already (not already downloaded)
						if (file.exists() == false){
							//Write the file to the cache dir
							BufferedInputStream imageStream = new BufferedInputStream(url.openConnection().getInputStream());
							ByteArrayBuffer baf = new ByteArrayBuffer(50);
							int current = 0;
							while ((current = imageStream.read()) != -1){
								baf.append((byte) current);
							}
							FileOutputStream outputStream = new FileOutputStream(file);
							outputStream.write(baf.toByteArray());
							outputStream.close();
						}
					}
				} 
			}
			catch (MalformedURLException e1) {
			} 
			catch (IOException e) {
			}
			return null;
		}
	}

	private class LoadMissingImageAsyncTask extends AsyncTask<URL,Void, Bitmap>{
		ProgressDialog dialog;
		@Override
	    protected void onPreExecute() {
	        dialog = new ProgressDialog(MainActivity.this);
	        dialog.setMessage("Loading Image!");
	        dialog.show();
	    }
		
		@Override
		protected Bitmap doInBackground(URL... urls) {
			Bitmap missingBitmap = null;
			try{
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				Bitmap first = BitmapFactory.decodeStream(urls[0].openConnection().getInputStream(), null, options);
				int width = options.outWidth;
				int height = options.outHeight;
				int divider = 1;
				 //Set a divider value to divide the image size by to make it fit within the desired bounds
				 if (width > 2000 || height > 2000){
					 divider = Double.valueOf(Math.max(Math.ceil(width/2000.0),Math.ceil(height/2000.0))).intValue();
				 }
				 //Now with the correct sample size, actually load the bitmap
				 options.inJustDecodeBounds = false;
				 options.inSampleSize = divider;
				 missingBitmap = BitmapFactory.decodeStream(urls[0].openConnection().getInputStream(), null, options);
			}
			catch (IOException e){
				e.printStackTrace();
			}
			return missingBitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap result){
			dialog.dismiss();
			mImageView.setImageBitmap(result);
		}	
	}
	

	/**
	 * Async task to load mWordHints from the bucket for the current set and store them in a map
	 */
	private class LoadHintsAsyncTask extends AsyncTask<String, Integer, Void>{

		@Override
		protected Void doInBackground(String... set) {
			//Use a map of each word to an array of it's mWordHints
			mWordHints = new TreeMap<String, String[]>();
			try {
				URL ur = new URL(S3_BUCKET_URL + mCurrentPath + 
				"/" + "mWordHints.txt");
				//make a reader from the mWordHints file in the bucket
				BufferedReader hintReader = new BufferedReader(new InputStreamReader(ur.openStream()));
				String lineRead;
				int linenumber = 0;
				String word = "";
				String sentence = "";
				String Rhyme1 = "";
				String Rhyme2 = "";
				//read a line, based on which line number it is, we know what kind of hint it is
				//all based on text file conventions
				while ((lineRead = hintReader.readLine()) != null){
					switch(linenumber) {
					case 0: word = lineRead; break;
					case 1: sentence = lineRead; break;
					case 2: Rhyme1 = lineRead; break;
					case 3: 
						Rhyme2 = lineRead; 
						if(!word.equals("") && !sentence.equals("") && !Rhyme1.equals("") && !Rhyme2.equals("")){
							String [] hts = {sentence, Rhyme1, Rhyme2};
							mWordHints.put(word, hts);
						}
						break;
					}
					linenumber++;
					//if we've reached an empty line, means we're moving on to next word's mWordHints, reset linenumber
					if(lineRead.length() == 0){
						linenumber = 0;
					}
				} 
			}catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	

	/**
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * Loads the image needed from the cache directory and sets it to the image view
	 */
	private void loadImage() throws ClientProtocolException, IOException, InterruptedException, ExecutionException {	
		mPhotoBitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		if(!(new File(buildCachePath(".jpg"))).exists()){
			AsyncTask<URL, Void, Bitmap> loadImage = new LoadMissingImageAsyncTask().execute(new URL(S3_BUCKET_URL + mCurrentPath + 
					"/" + mCurrentSet.get(mCurrentIndex) + ".jpg"));
			}
		try{
			//First just determine the size of the bitmap file
			 options.inJustDecodeBounds = true;
			 Bitmap first = BitmapFactory.decodeFile(buildCachePath(".jpg"),options);
			 int width = options.outWidth;
			 int height = options.outHeight;
			 int divider = 1;
			 //Set a divider value to divide the image size by to make it fit within the desired bounds
			 if (width > 2000 || height > 2000){
				 divider = Double.valueOf(Math.max(Math.ceil(width/2000.0),Math.ceil(height/2000.0))).intValue();
			 }
			 //Now with the correct sample size, actually load the bitmap
			 options.inJustDecodeBounds = false;
			 options.inSampleSize = divider;
			 mPhotoBitmap = BitmapFactory.decodeFile(buildCachePath(".jpg"),options);
		}
		catch(Exception e){
		}	
		if (mPhotoBitmap != null){
			//If it's the first image of the set, just display it
			if(mCurrentIndex == 0){
				mImageView.setImageBitmap(mPhotoBitmap);
			}
			//If not, use animation to change the image
			else{
				imageViewAnimatedChange(getApplicationContext(), mImageView, mPhotoBitmap);
			}
		}	
	}

	
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}

	public static void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out); 
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in); 
        anim_out.setAnimationListener(new AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image); 
                anim_in.setAnimationListener(new AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }


	private void playSoundText(String hint){
		if (mListenerIsReady == false){
			Toast.makeText(this, "Hold on! I'm not ready yet! Try again in a second!", Toast.LENGTH_SHORT).show();
		}
		else {
			String text = "";
			String[] hintarray = mWordHints.get(mCurrentSet.get(mCurrentIndex));
			if (hintarray != null && hintarray.length > 1){
				if (hint.equals("word")){
					text = mCurrentSet.get(mCurrentIndex);
				}
				if (hint.equals("phrase")){
					text = hintarray[0];
				}
				if (hint.equals("rhyme")){
					text = hintarray[mRhymeUsed+1];
					if(mRhymeUsed == (hintarray.length-2)){
						mRhymeUsed = 0;
					}
					else{
						mRhymeUsed++;
					}
				}
			}
			mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private class TextToSpeechListener implements TextToSpeech.OnInitListener{
		@Override
		public void onInit(int arg0) {
			mListenerIsReady = true;
		}
	}

    public void nextImage(){
    	mTextToSpeech.stop();
    	double inc = 100.00/(mCurrentSet.size());
    	int currprog = mProgressBar.getProgress();
    	mProgressBar.setProgress(currprog + (int)Math.round(inc));
    	
    	mCurrentIndex++;
		mRhymeUsed = 0;
		if(checkEndOfSet() == true){
			return;
		}
		else{
		try {
			loadImage();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mNumHintsUsed = 0;
		mNumTries = 0;
		}
    }
    
    /**
     * @return true if set has been completed, false if not
     * Method to check if the set has been completed and record values as necessary for end of set
     */
    private boolean checkEndOfSet(){
    	boolean end = false;
    	if(mCurrentSet == null){
    		return true;
    	}
    	if(mCurrentIndex >= mCurrentSet.size()){
    		end = true;

    		//update scores info in db so that EndSet can get correct updated results
    		if(mSetScore > mUserScores.getHighScore(mCurrentPath)) {
    			mUserScores.setHighScore(mCurrentPath, mSetScore);
    		}

    		//check highest streak compared to current streak
    		if(mUserScores.getHighestStreak() < mStreak) {
    			mUserScores.setHighestStreak(mStreak);
    			mNewStreak = true;
    		}
    		
  		    finish();
  		    
  		    boolean newNumCorrect = false;
    		int prevNumOfCorrectAnswers = mUserScores.getNumCompleted(mCurrentPath);
    		if(mNumCorrect > prevNumOfCorrectAnswers)
    		{
    			mUserScores.setNumCompleted(mCurrentPath, mNumCorrect);
    			newNumCorrect = true;
    		}

    		mUserScores.setTotalScore(mTotalScore);
    		
    		Intent i = new Intent(this, EndSet.class);
    		i.putExtra("set", mCurrentPath);
    		i.putExtra("setscore", mSetScore);
    		i.putExtra("newstreak", mNewStreak);
    		i.putExtra("newNumCorrect", newNumCorrect);
    		i.putExtra("numCorrect", mNumCorrect);
    		startActivityForResult(i,2);
    	}
    	return end;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case 1:  //speech recognition result
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String correctAnswer = mCurrentSet.get(mCurrentIndex);
                
                mUserGuess = result.get(0);
                for(String str: result)
                {
                	if(str.equals(correctAnswer) || str.contains(correctAnswer))
                	{
                		giveFeedback(true, str);
                		return;
                	}
                }
                giveFeedback(false, result.get(0));
            }
            break;
        
        case 2: //endset result
        	if(resultCode == RESULT_OK) {
        		setResult(RESULT_OK);
        		finish();
        	}
        }
    }
    
    /**
	 * Method that displays a dialog showing the user whether they said the right 
	 * answer or not, and giving them the option of continuing or trying again.
	 * @param isSuccess  whether or not the user said the correct word
	 * @param word_said  the word that the user said
	 */
	private void giveFeedback(boolean isSuccess, String word_said) {
		//build the dialog
		AlertDialog.Builder b = new AlertDialog.Builder(this);

		b.setCancelable(false);
		
		//get parts of the dialog view for later assignment
		View dialog_view = this.getLayoutInflater().inflate(R.layout.feedback_dialog, null);
		
		ImageView icon = (ImageView) dialog_view.findViewById(R.id.dialog_icon);
		TextView resulttext = (TextView) dialog_view.findViewById(R.id.dialog_result);
		TextView feedbacktext = (TextView) dialog_view.findViewById(R.id.dialog_feedback);
		TextView pointstext = (TextView) dialog_view.findViewById(R.id.dialog_points);
		TextView hintstext = (TextView) dialog_view.findViewById(R.id.dialog_hints);
		mDialogScoreTextView = (TextView) dialog_view.findViewById(R.id.dialog_setscore);

		if(isSuccess) {  //only give them continue button if they got it right
			mNumCorrect++;
			
			resulttext.setText("Correct!");
			icon.setImageResource(R.drawable.checkmark);
			feedbacktext.setText("You said: " + word_said);
			pointstext.setText("+ 3");
			hintstext.setText("- " + Integer.toString(mNumHintsUsed));
			mDialogScoreTextView.setText(Integer.toString(mSetScore));
			mDialogScoreTextView.setTextColor(getResources().getColor(R.color.yellow));
			
			//call the asynctask to increment the previous total to the new total
			new IncrementScoreAsyncTask().execute(mSetScore, mSetScore + 3 - mNumHintsUsed, 3 - mNumHintsUsed);
			
			mFeedbackResult="continue";

			b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					mSetScore += 3-mNumHintsUsed;
					mTotalScore += 3-mNumHintsUsed;
					mStreak++;
		        	TextView mScoreTextView = (TextView) findViewById(R.id.score);

		        	mScoreTextView.setText(Integer.toString(mSetScore));
		        	
		        	if(mNumHintsUsed < 3)
		        		animateScore();
		    		
		        	mStatsDb.addStat(mCurrentSet.get(mCurrentIndex), mNumTries, mNumHintsUsed, mHintWordUsed, mHintPhraseUsed, mHintRhymeUsed, mUserGuess, 1);
		        	mHintWordUsed = 0;
		        	mHintPhraseUsed = 0;
		        	mHintRhymeUsed = 0;
		        	mUserGuess = new String();
		        	nextImage();
				}
			});

		}
		else if(isSuccess == false && mNumTries >= 2) {  //got it wrong, but time to move on
			
			resulttext.setText("Try the next picture!");
			resulttext.setTextSize(30);
			icon.setImageResource(R.drawable.wrong);
			feedbacktext.setText("The correct answer was: " + mCurrentSet.get(mCurrentIndex));
			pointstext.setVisibility(View.INVISIBLE);
			hintstext.setVisibility(View.INVISIBLE);
			mDialogScoreTextView.setText(Integer.toString(mSetScore));
			
			mFeedbackResult="continue";

			b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

					//check if streak that just ended was the highest
					if(mUserScores.getHighestStreak() < mStreak) {
						mUserScores.setHighestStreak(mStreak);
						mNewStreak = true;
					}

		        	mStatsDb.addStat(mCurrentSet.get(mCurrentIndex), mNumTries, mNumHintsUsed, mHintWordUsed, mHintPhraseUsed, mHintRhymeUsed, mUserGuess, 0);
		        	mHintWordUsed = 0;
		        	mHintPhraseUsed = 0;
		        	mHintRhymeUsed = 0;
		        	mUserGuess = new String();
		        	
					mStreak = 0;
					nextImage();
				}
			});

		}
		else {
			
			resulttext.setText("Not quite!");
			icon.setImageResource(R.drawable.wrong);
			feedbacktext.setText("You said: " + word_said);
			pointstext.setVisibility(View.INVISIBLE);
			hintstext.setVisibility(View.INVISIBLE);
			mDialogScoreTextView.setText(Integer.toString(mSetScore));
			
			mFeedbackResult="again";

			b.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					mNumTries++;
					//check if streak that just ended was the highest
					if(mUserScores.getHighestStreak() < mStreak) {
						mUserScores.setHighestStreak(mStreak);
						mNewStreak = true;
					}
					
		        	mStatsDb.addStat(mCurrentSet.get(mCurrentIndex), mNumTries, mNumHintsUsed, mHintWordUsed, mHintPhraseUsed, mHintRhymeUsed, mUserGuess, 0); 
		        	mUserGuess = new String();
		        	
					mStreak = 0;
				}
			});	
		}
		
		b.setView(dialog_view);
		mAlertDialog = b.create();
		mAlertDialog.show();  //show the dialog
		

		//play the audio feedback
		if(isSuccess) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.correct);
			mp.start();

			mp.setOnCompletionListener(new OnCompletionListener() {

				public void onCompletion(MediaPlayer mp) {
					mTextToSpeech.speak("Great Job!", TextToSpeech.QUEUE_FLUSH, null);
				}
			});
		}
		else if(mFeedbackResult.equals("continue")) {
			mTextToSpeech.speak("So close! You'll get it next time.", TextToSpeech.QUEUE_FLUSH, null);
		}
		else if(mFeedbackResult.equals("again")) {
			mTextToSpeech.speak("Almost!  Try again", TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	public void animateScore() {
    	mScoreTextView.setTextColor(getResources().getColor(R.color.green));
    	mScoreTextView.setTypeface(null, Typeface.BOLD);
    	mScoreTextView.setTextSize(40);
    	
		RotateAnimation rotateTextAnimation = new RotateAnimation(0, 360, 40, 30);
		rotateTextAnimation.setDuration(2000);
		
		mScoreTextView.startAnimation(rotateTextAnimation);
		Handler mHandler = new Handler();
		mHandler.postDelayed(runnable, 3000);
	
	}
	
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			mScoreTextView.setTextColor(getResources().getColor(R.color.yellow));
			mScoreTextView.setTypeface(null, Typeface.NORMAL);
			mScoreTextView.setTextSize(40);
		}
		
	};
	

	   @Override
	   protected void onDestroy() {
		  mCurrentIndex = 0;
		  if(mTextToSpeech != null){
			  mListenerIsReady = false;
			  mTextToSpeech.stop();
			  mTextToSpeech.shutdown(); 
			  mTextToSpeech = null;
		  }
		  
		  mStatsDb.close();
		  mUserScores.closeDb();
	      super.onDestroy();

	   }

	   @Override
	    protected void onPause() {
	        super.onPause();

	    }

	   @Override
	    protected void onStop() {
	        super.onStop();

	    }

	    @Override
	    protected void onResume() {
	        super.onResume();
	    }

		public AsyncTask.Status getDownloadHintsStatus() {
			return mDownloadHintsTask.getStatus();
		}

		public AsyncTask.Status getDownloadFilesStatus() {
			return mDownloadFilesTask.getStatus();
		}
		
		
		/**
		 * AsyncTask class used for animating the score updating.
		 * Takes in prev_total, new_total and counts 
		 * prev_total up to new_total.
		 */
		private class IncrementScoreAsyncTask extends AsyncTask<Integer, Integer, Void> {		
			
			protected Void doInBackground(Integer... scores) {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				
				while(!this.isCancelled() && scores[0] <= scores[1]) {	
					
					publishProgress(scores[0]); //Android calls onProgressUpdate
					scores[0]++;
					scores[2]--;
					
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {}
				}
				
				return null;
			}
			
			
			protected void onProgressUpdate(Integer... current) {
				mDialogScoreTextView.setText(current[0].toString());
			}
			
			protected void onPostExecute(Void voids) {
				
			}

		}

}
