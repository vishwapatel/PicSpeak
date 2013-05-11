# [PicSpeak](https://play.google.com/store/apps/details?id=com.vishwa.picspeak) #

[PicSpeak](https://play.google.com/store/apps/details?id=com.vishwa.picspeak) is an Android app for Tablets that has been primarily designed for [aphasia](http://en.wikipedia.org/wiki/Aphasia) patients and their rehabilitation therapists. ** However, PicSpeak can also be used by parents to teach their kids new words through association with some image of that word. ** PicSpeak is a modernized mobile gamified approach to an existing (and slightly outdated) aphasia therapy software called [MossTalk Words](http://www.mosstalkwords.com/). Most aphasia patients suffer from these symptoms:
- inability to name objects
- inability to form words
- poor enunciation

 In order to tackle these symptoms the app presents a visual stimulus (i.e an image) and then waits for the user's voice input and it processes this input through the [Google Voice Recognition API](http://developer.android.com/reference/android/speech/package-summary.html) and awards the user's points based on whether they got it right and how many hints they used. The app provides hints which can help the patient make a conncection between the object and its name.  

Here's how the app works:

When you open the app, you see a selection screen to pick the set you would like to play with. As you can see below, some levels/sets are locked. You can also see three starts next to each set. These represent your progress through that set. One star means you've completed six out of the 10 stimuli in that set, two mean you've completed 8 out of 10 and three stars mean you've aced the set! When you get at least one star in one of the sets you then unlock the more difficult version of that stimulus set.
![Selection Screen](http://i.imgur.com/6Rx8qm7.png)

Now, once you pick a set you wish to play you move on to the main game screen. On the main game screen, you can see the following things:

![Main Screen](http://i.imgur.com/0UNpsCx.png)

1. Hint buttons:
	- Phrase hint: The app reads out a funny phrase that contains the answer but doesn't include the answer itself.
	- Rhyme hint: The app reads out a word that rhymes with the answer.
	- Word hint: The app reads out the answer itself, so now, the patient simply has to enunciate the word correctly to get it right!

2. The score:
	- The score shows how many points you've gotten in the current set and it animates if you get something right.

3. The progress bar:
	- The progress bar is an indicator of how far ahead you are in the current set.

4. An image:
	- This is the object or thing that has to be named.

5. Buttons at the bottom:
	- Help button: The help button opens up a help screen where the user can choose to have the text read to them (specifically aimed at aphasia patients because another symptom of aphasia is the inability to read clearly).
	- Mic button: The mic button allows the user to say the word out and then using this input it determines whether or not it matches the actual answer and gives feedback to the user appropriately.
	- Skip button: You can choose to just skip over this image if you can't get it after trying hard enough.

Lastly, if you reach the end of the set, then a page tells you how you've performed in that set, tells you how many stars you've earned, if you've reached a new high score for that set and also shows you your total score in the game.

![Help Screen](http://i.imgur.com/PXrbxqT.png) ![End Screen](http://i.imgur.com/t3WmZ16.png)



