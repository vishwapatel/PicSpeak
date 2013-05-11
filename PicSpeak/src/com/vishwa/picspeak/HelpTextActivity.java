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
	
	private Button _backButton;
	private Button _textToSpeechButton;
	private TextView _helpText;
	
	private TextToSpeech _soundGenerator;
	
	private String helpText = "How to play: Tap the button with the microphone icon on the bottom" + 
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
	
	private boolean _listenerIsReady = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        
        if(_soundGenerator == null)
        {
        	_soundGenerator = new TextToSpeech(this, new TextToSpeechListener());
        }
        
        _backButton = (Button) findViewById(R.id.backbutton);
        _textToSpeechButton = (Button) findViewById(R.id.ttsbutton);
        
        _backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
        
        _textToSpeechButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(_textToSpeechButton.getText().equals("Start Help Text-To-Speech"))
				{
					if(_listenerIsReady)
					{
						_soundGenerator.speak(helpText.replace('\n', ' '), TextToSpeech.QUEUE_FLUSH, null);
						_textToSpeechButton.setText("Stop Help Reader");
						_listenerIsReady = false;
					}
					else
					{
						Toast.makeText(getApplicationContext(), "Text To Speech is busy", Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					_textToSpeechButton.setText("Start Help Text-To-Speech");
					_soundGenerator.stop();
					_listenerIsReady = true;
				}
			}
		});
        
        _helpText = (TextView) findViewById(R.id.helptext);

        _helpText.setText(helpText);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		  if(_soundGenerator != null){
			  _listenerIsReady = false;
			  _soundGenerator.stop();
			  _soundGenerator.shutdown(); 
			  _soundGenerator = null;
		  }
	}

	private class TextToSpeechListener implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener{
		@Override
		public void onInit(int arg0) {
			_listenerIsReady = true;
		}

		@Override
		public void onUtteranceCompleted(String utteranceId) {
			_textToSpeechButton.setText("Start Help Text-To-Speech");
		}
		
	}

}
