package com.vishwa.picspeak;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HelpTextActivity extends Activity {
	
	private Button mBackButton;
	private Button mTextToSpeechButton;
	private TextView mHelpTextView;
	
	private TextToSpeech mTextToSpeech;
	
	private String mHelpText = "How to play: Tap the button with the microphone icon on the bottom" + 
            " of the screen to start speaking. After you have spoken the word associated" +
	          " with the picture, you will be told whether the word you spoke was correct or" +
            " incorrect. You may attempt to pronounce the word associated with the picture " +
	          "up to three times.\n\n" +
            "Hints: The hint buttons are located on the top part of the screen.\n" +
            "Tap the 'Phrase' button to hear the word associated with " +
	          "the picture used in a phrase.\nTap the 'Word' button to hear the word " +
            "associated with the picture pronounced.\nTap the 'Rhyme' button to hear " +
	          "a word that rhymes with the word pronounced.\n\nSkipping: Tap the 'Skip' " +
            "button to skip to the next picture. This will end your current streak and you" +
	          " will not receive points for the word.";
	
	private boolean mListenerIsReady = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        if(mTextToSpeech == null)
        {
        	mTextToSpeech = new TextToSpeech(this, new TextToSpeechListener());
        }
        
        mBackButton = (Button) findViewById(R.id.backbutton);
        mTextToSpeechButton = (Button) findViewById(R.id.ttsbutton);
        
        mBackButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
        
        mTextToSpeechButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mTextToSpeechButton.getText().equals("Start Help Text-To-Speech"))
				{
					if(mListenerIsReady)
					{
						mTextToSpeech.speak(mHelpText.replace('\n', ' '), TextToSpeech.QUEUE_FLUSH, null);
						mTextToSpeechButton.setText("Stop Help Reader");
						mListenerIsReady = false;
					}
					else
					{
						Toast.makeText(getApplicationContext(), "Text To Speech is busy", Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					mTextToSpeechButton.setText("Start Help Text-To-Speech");
					mTextToSpeech.stop();
					mListenerIsReady = true;
				}
			}
		});
        
        mHelpTextView = (TextView) findViewById(R.id.helptext);

        mHelpTextView.setText(mHelpText);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		  if(mTextToSpeech != null){
			  mListenerIsReady = false;
			  mTextToSpeech.stop();
			  mTextToSpeech.shutdown(); 
			  mTextToSpeech = null;
		  }
	}

	private class TextToSpeechListener implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener{
		@Override
		public void onInit(int arg0) {
			mListenerIsReady = true;
		}

		@Override
		public void onUtteranceCompleted(String utteranceId) {
			mTextToSpeechButton.setText("Start Help Text-To-Speech");
		}
		
	}

}
