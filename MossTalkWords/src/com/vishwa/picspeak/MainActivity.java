package com.vishwa.picspeak;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import android.speech.tts.TextToSpeech;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.graphics.Color;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
	public final static String currentSavedScore = "edu.upenn.cis350.mosstalkwords.currentSavedScore";
	public final static String bucketSite ="https://s3.amazonaws.com/mosswords/"; 
	private ImageView _imgView;
	private String _currentPath;
	private int _currentIndex;
	private Button _hintPhraseButton;
	private Button _hintRhymeButton;
	private Button _hintPronounceButton;
	private Button _micButton;
	private Button _helpButton;
	private Button _skipButton;
	private MediaPlayer _mediaPlayer;
	private Bitmap currBitmap = null;
    private TextView st;

	private ProgressBar progressBarSet;

	private StatsDbAdapter statsDb;
	
	private boolean _listenerIsReady = false;
	private TextToSpeech soundGenerator;
	private TreeMap<String, String[]> hints; 
	private int _rhymeUsed; 
	private int _totalScore = 0;
	
	private int hintWordUsed = 0;
	private int hintPhraseUsed = 0;
	private int hintRhymeUsed = 0;
	private String userGuess = new String();

	private Scores _scores;
	private int _setScore = 0;
	private int _streak = 0;
	private boolean newStreak = false;
	private int _numHintsUsed = 0;
	private int _numTries = 0;
	private String _feedbackResult = "";
	private ArrayList<String> _currentSet;

	private AlertDialog ad;
	private TextView dialogsetscoretext;
	private int _numCorrect = 0;

	private AsyncTask<String, Integer, Void> downloadHints;
	private AsyncTask<String, Integer, Void> downloadFiles;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _imgView = (ImageView) findViewById(R.id.image);
        //for finding image files
        _currentIndex = 0;
        _currentPath = getIntent().getStringExtra("edu.upenn.cis350.mosstalkwords.currentSetPath");
        _currentSet = getIntent().getStringArrayListExtra("edu.upenn.cis350.mosstalkwords.currentSet");
        
        //scores
        _scores = new Scores(this.getApplicationContext());
        _totalScore = _scores.getTotalScore();
        _setScore = 0;
        _streak = 0;
        
        statsDb = new StatsDbAdapter(this.getApplicationContext());
        statsDb.open();
        
        //set score view
        st = (TextView) findViewById(R.id.score);
    	st.setText(Integer.toString(_setScore));
    	
    	progressBarSet = (ProgressBar)findViewById(R.id.progressBarGame);
    	Resources res = getResources();
    	progressBarSet.setProgressDrawable(res.getDrawable( R.drawable.game_progress));
    	progressBarSet.setMax(100);
    	progressBarSet.setProgress(0);
    	//download images, download hints
    	downloadHints = new LoadHintsTask().execute("");
        downloadFiles = new LoadFilesTask().execute("");
        
        //create TextToSpeech
        if(soundGenerator == null){
        soundGenerator = new TextToSpeech(this, new TextToSpeechListener());
        
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
        _hintPhraseButton = (Button) findViewById(R.id.hintbuttona);
        _hintRhymeButton = (Button) findViewById(R.id.hintbuttonb);
        _hintPronounceButton = (Button) findViewById(R.id.hintbuttonc);
        _micButton = (Button) findViewById(R.id.micbutton);
        _skipButton = (Button) findViewById(R.id.skipbutton);
        _helpButton = (Button) findViewById(R.id.helpbutton);
      
        _mediaPlayer = new MediaPlayer();
        _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        _hintPhraseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				hintPhraseUsed = 1;
				playSoundText("phrase");
				if(_numHintsUsed < 3)
					_numHintsUsed++;	
			}
		});
        
        _hintRhymeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				hintRhymeUsed = 1;
				playSoundText("rhyme");
				if(_numHintsUsed < 3)
					_numHintsUsed++;
			}
		});
        
        _hintPronounceButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				hintWordUsed = 1;
				playSoundText("word");
				if(_numHintsUsed < 3)
					_numHintsUsed++;
			}
		});
        
        _helpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), HelpTextActivity.class);
				startActivity(i);
			}
		});
        
        _micButton.setOnClickListener(new OnClickListener() {
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
        
        _skipButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(_scores.getHighestStreak() < _streak) {
					_scores.setHighestStreak(_streak);
					newStreak = true;
				}

				_streak = 0;
				nextImage();
			}
		});
        
    }
	//Get path to the cache directory file needed 
	private String buildCachePath(String extension){
		if(getApplicationContext() != null){
			if(getApplicationContext().getCacheDir() != null){
				if(getApplicationContext().getCacheDir().getPath() != null){
					if(_currentSet == null){
						return "curr set null!";
					}
					else {
						return getApplicationContext().getCacheDir().getPath()+"/"+_currentSet.get(_currentIndex)+extension; 
					}
				}
			}	
		}
		return "error encountered";
	}

	/**
	 *Async Task to load the images from the bucket and save them to the cache directory
	 */
	private class LoadFilesTask extends AsyncTask<String, Integer, Void>{
		@Override
		protected Void doInBackground(String... set) {
			try {
				//make sure currentSet has been defined already
				if(_currentSet != null){
					for (String word: _currentSet){
						//set url for each image
						URL url = new URL(bucketSite + _currentPath + 
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
							Log.i("info", file.getAbsolutePath() + "saved it!");
						}
						else{
							Log.i("info", file.getAbsolutePath() + "  exists!");
						}
					}
				} 
			}
			catch (MalformedURLException e1) {
				Log.i("info", "MalformedURL exception!");
			} catch (IOException e) {
				Log.i("info", "IO exception!");
			}
			return null;
		}
	}

	private class MissingImageTask extends AsyncTask<URL,Void, Bitmap>{
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
			_imgView.setImageBitmap(result);
		}	
	}
	

	/**
	 * Async task to load hints from the bucket for the current set and store them in a map
	 */
	private class LoadHintsTask extends AsyncTask<String, Integer, Void>{

		@Override
		protected Void doInBackground(String... set) {
			//Use a map of each word to an array of it's hints
			hints = new TreeMap<String, String[]>();
			try {
				URL ur = new URL(bucketSite + _currentPath + 
				"/" + "hints.txt");
				//make a reader from the hints file in the bucket
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
					Log.i("info", lineRead);
					switch(linenumber) {
					case 0: word = lineRead; break;
					case 1: sentence = lineRead; break;
					case 2: Rhyme1 = lineRead; break;
					case 3: 
						Rhyme2 = lineRead; 
						if(!word.equals("") && !sentence.equals("") && !Rhyme1.equals("") && !Rhyme2.equals("")){
							String [] hts = {sentence, Rhyme1, Rhyme2};
							Log.i("info", word + " " + Rhyme1 );
							hints.put(word, hts);
						}
						else{
							Log.i("info", "Hints loading issue!");
						}
						break;
					}
					linenumber++;
					//if we've reached an empty line, means we're moving on to next word's hints, reset linenumber
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
		currBitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		if(!(new File(buildCachePath(".jpg"))).exists()){
			Log.i("info","image does not exist");
			AsyncTask<URL, Void, Bitmap> loadImage = new MissingImageTask().execute(new URL(bucketSite + _currentPath + 
					"/" + _currentSet.get(_currentIndex) + ".jpg"));
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
			 currBitmap = BitmapFactory.decodeFile(buildCachePath(".jpg"),options);
		}
		catch(Exception e){
			Log.i("info", "Bitmap Exception!");
		}	
		if (currBitmap != null){
			//If it's the first image of the set, just display it
			if(_currentIndex == 0){
				_imgView.setImageBitmap(currBitmap);
			}
			//If not, use animation to change the image
			else{
				imageViewAnimatedChange(getApplicationContext(), _imgView, currBitmap);
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
		if (_listenerIsReady == false){
			Toast.makeText(this, "Hold on! I'm not ready yet! Try again in a second!", Toast.LENGTH_SHORT).show();
		}
		else {
			String text = "";
			Log.i("info", _currentSet.get(_currentIndex));
			Log.i("info", Arrays.toString(hints.get(_currentSet.get(_currentIndex))));
			String[] hintarray = hints.get(_currentSet.get(_currentIndex));
			if (hintarray != null && hintarray.length > 1){
				if (hint.equals("word")){
					text = _currentSet.get(_currentIndex);
				}
				if (hint.equals("phrase")){
					text = hintarray[0];
				}
				if (hint.equals("rhyme")){
					text = hintarray[_rhymeUsed+1];
					if(_rhymeUsed == (hintarray.length-2)){
						_rhymeUsed = 0;
					}
					else{
						_rhymeUsed++;
					}
				}
			}
			soundGenerator.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private class TextToSpeechListener implements TextToSpeech.OnInitListener{
		@Override
		public void onInit(int arg0) {
			_listenerIsReady = true;
		}
	}

    public void nextImage(){
    	soundGenerator.stop();
    	double inc = 100.00/(_currentSet.size());
    	int currprog = progressBarSet.getProgress();
    	progressBarSet.setProgress(currprog + (int)Math.round(inc));
    	
    	_currentIndex++;
		_rhymeUsed = 0;
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
		_numHintsUsed = 0;
		_numTries = 0;
		}
    }
    
    /**
     * @return true if set has been completed, false if not
     * Method to check if the set has been completed and record values as necessary for end of set
     */
    private boolean checkEndOfSet(){
    	boolean end = false;
    	if(_currentSet == null){
    		return true;
    	}
    	if(_currentIndex >= _currentSet.size()){
    		end = true;

    		//update scores info in db so that EndSet can get correct updated results
    		if(_setScore > _scores.getHighScore(_currentPath)) {
    			_scores.setHighScore(_currentPath, _setScore);
    		}

    		//check highest streak compared to current streak
    		if(_scores.getHighestStreak() < _streak) {
    			_scores.setHighestStreak(_streak);
    			newStreak = true;
    		}
    		
  		    finish();
  		    
  		    boolean newNumCorrect = false;
    		int prevNumOfCorrectAnswers = _scores.getNumCompleted(_currentPath);
    		if(_numCorrect > prevNumOfCorrectAnswers)
    		{
    			_scores.setNumCompleted(_currentPath, _numCorrect);
    			newNumCorrect = true;
    		}

    		_scores.setTotalScore(_totalScore);
    		
    		Intent i = new Intent(this, EndSet.class);
    		i.putExtra("set", _currentPath);
    		i.putExtra("setscore", _setScore);
    		i.putExtra("newstreak", newStreak);
    		i.putExtra("newNumCorrect", newNumCorrect);
    		i.putExtra("numCorrect", _numCorrect);
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
                String correctAnswer = _currentSet.get(_currentIndex);
                
                userGuess = result.get(0);
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
		dialogsetscoretext = (TextView) dialog_view.findViewById(R.id.dialog_setscore);

		if(isSuccess) {  //only give them continue button if they got it right
			_numCorrect++;
			
			resulttext.setText("Correct!");
			icon.setImageResource(R.drawable.checkmark);
			feedbacktext.setText("You said: " + word_said);
			pointstext.setText("+ 3");
			hintstext.setText("- " + Integer.toString(_numHintsUsed));
			dialogsetscoretext.setText(Integer.toString(_setScore));
			dialogsetscoretext.setTextColor(getResources().getColor(R.color.yellow));
			
			//call the asynctask to increment the previous total to the new total
			new IncScore().execute(_setScore, _setScore + 3 - _numHintsUsed, 3 - _numHintsUsed);
			
			_feedbackResult="continue";

			b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					_setScore += 3-_numHintsUsed;
					_totalScore += 3-_numHintsUsed;
					_streak++;
		        	TextView st = (TextView) findViewById(R.id.score);

		        	st.setText(Integer.toString(_setScore));
		        	
		        	if(_numHintsUsed < 3)
		        		animateScore();
		    		
		        	statsDb.addStat(_currentSet.get(_currentIndex), _numTries, _numHintsUsed, hintWordUsed, hintPhraseUsed, hintRhymeUsed, userGuess, 1);
		        	hintWordUsed = 0;
		        	hintPhraseUsed = 0;
		        	hintRhymeUsed = 0;
		        	userGuess = new String();
		        	nextImage();
				}
			});

		}
		else if(isSuccess == false && _numTries >= 2) {  //got it wrong, but time to move on
			
			resulttext.setText("Try the next picture!");
			resulttext.setTextSize(30);
			icon.setImageResource(R.drawable.wrong);
			feedbacktext.setText("The correct answer was: " + _currentSet.get(_currentIndex));
			pointstext.setVisibility(View.INVISIBLE);
			hintstext.setVisibility(View.INVISIBLE);
			dialogsetscoretext.setText(Integer.toString(_setScore));
			
			_feedbackResult="continue";

			b.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

					//check if streak that just ended was the highest
					if(_scores.getHighestStreak() < _streak) {
						_scores.setHighestStreak(_streak);
						newStreak = true;
					}

		        	statsDb.addStat(_currentSet.get(_currentIndex), _numTries, _numHintsUsed, hintWordUsed, hintPhraseUsed, hintRhymeUsed, userGuess, 0);
		        	hintWordUsed = 0;
		        	hintPhraseUsed = 0;
		        	hintRhymeUsed = 0;
		        	userGuess = new String();
		        	
					_streak = 0;
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
			dialogsetscoretext.setText(Integer.toString(_setScore));
			
			_feedbackResult="again";

			b.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					_numTries++;
					//check if streak that just ended was the highest
					if(_scores.getHighestStreak() < _streak) {
						_scores.setHighestStreak(_streak);
						newStreak = true;
					}
					
		        	statsDb.addStat(_currentSet.get(_currentIndex), _numTries, _numHintsUsed, hintWordUsed, hintPhraseUsed, hintRhymeUsed, userGuess, 0); 
		        	userGuess = new String();
		        	
					_streak = 0;
				}
			});	
		}
		
		b.setView(dialog_view);
		ad = b.create();
		ad.show();  //show the dialog
		

		//play the audio feedback
		if(isSuccess) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.correct);
			mp.start();

			mp.setOnCompletionListener(new OnCompletionListener() {

				public void onCompletion(MediaPlayer mp) {
					soundGenerator.speak("Great Job!", TextToSpeech.QUEUE_FLUSH, null);
				}
			});
		}
		else if(_feedbackResult.equals("continue")) {
			soundGenerator.speak("So close! You'll get it next time.", TextToSpeech.QUEUE_FLUSH, null);
		}
		else if(_feedbackResult.equals("again")) {
			soundGenerator.speak("Almost!  Try again", TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	public void animateScore() {
    	st.setTextColor(getResources().getColor(R.color.green));
    	st.setTypeface(null, Typeface.BOLD);
    	st.setTextSize(40);
    	
		RotateAnimation rotateTextAnimation = new RotateAnimation(0, 360, 40, 30);
		rotateTextAnimation.setDuration(2000);
		
		st.startAnimation(rotateTextAnimation);
		Handler mHandler = new Handler();
		mHandler.postDelayed(runnable, 3000);
	
	}
	
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			st.setTextColor(getResources().getColor(R.color.yellow));
			st.setTypeface(null, Typeface.NORMAL);
			st.setTextSize(40);
		}
		
	};
	

	   @Override
	   protected void onDestroy() {
		   Log.i("info", "onDestroy called");
		  _currentIndex = 0;
		  if(soundGenerator != null){
			  _listenerIsReady = false;
			  soundGenerator.stop();
			  soundGenerator.shutdown(); 
			  soundGenerator = null;
		  }
		  
		  statsDb.close();
		  _scores.closeDb();
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
			return downloadHints.getStatus();
		}

		public AsyncTask.Status getDownloadFilesStatus() {
			return downloadFiles.getStatus();
		}
		
		
		/**
		 * AsyncTask class used for animating the score updating.
		 * Takes in prev_total, new_total and counts 
		 * prev_total up to new_total.
		 */
		private class IncScore extends AsyncTask<Integer, Integer, Void> {		
			
			protected Void doInBackground(Integer... scores) {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				
				while(this.isCancelled() == false && scores[0] <= scores[1]) {	
					
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
				dialogsetscoretext.setText(current[0].toString());
			}
			
			protected void onPostExecute(Void voids) {
				
			}

		}

}
