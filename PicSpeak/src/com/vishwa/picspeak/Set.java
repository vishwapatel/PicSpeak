package com.vishwa.picspeak;

public class Set {

		private int mHighScore;
		private int mNumCompleted;
		private int mTotal;
		private String mSetScore;
		private String mCategory;
		private String mDifficulty;
		private int mStar1;
		private int mStar2;
		private int mStar3;
		private int mColor; 
		private boolean mIsLocked;
	
		
		public Set(String sc, String cat, String diff, Boolean unlocked){
			mSetScore = sc;
			mCategory = cat;
			mDifficulty = diff;
			mStar1 = R.drawable.emptystar;
			mStar2 = R.drawable.emptystar;
			mStar3 = R.drawable.emptystar;
			mColor = 0xFF37719D;
			if(sc.equals("6/10") || sc.equals("7/10")){
				mStar1 = R.drawable.star;
			}
			if(sc.equals("8/10") || sc.equals("9/10")){
				mStar1 = R.drawable.star;
				mStar2 = R.drawable.star;
			}
			if(sc.equals("10/10")){
				mStar1 = R.drawable.star;
				mStar2 = R.drawable.star;
				mStar3 = R.drawable.star;
			}
			
			if(mDifficulty.equals("Easy")){
				mColor = 0xFF288C8C;
			}
			if(mDifficulty.equals("Medium")){
				mColor = 0xFF1E6767;
			}
			if(mDifficulty.equals("Hard")){
				mColor = 0xFF113E3E;
			}
			if(unlocked == false){
				
				mColor = 0xBB888888;
				mIsLocked = true;
			}
			
		}
		
		public String getSetDifficulty() {
			return mDifficulty;
		}
		
		public String getSetCategory() {
			return mCategory;
		}
		
		public int getStar1() {
			return mStar1;
		}
		
		public int getStar2() {
			return mStar2;
		}
		
		public int getStar3() {
			return mStar3;
		}
		
		public int getColor() {
			return mColor;
		}
		
		public boolean isSetLocked() {
			return mIsLocked;
		}
		
	}

