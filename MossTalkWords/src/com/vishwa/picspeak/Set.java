package com.vishwa.picspeak;

public class Set {

		public int highscore;
		public int numcompleted;
		public int total;
		public String score;
		public String category;
		public String difficulty;
		public int star1;
		public int star2;
		public int star3;
		public int color; 
		public boolean locked;
	
		
		public Set(String sc, String cat, String diff, Boolean unlocked){
			score = sc;
			category = cat;
			difficulty = diff;
			star1 = R.drawable.emptystar;
			star2 = R.drawable.emptystar;
			star3 = R.drawable.emptystar;
			color = 0xFF37719D;
			if(sc.equals("6/10") || sc.equals("7/10")){
				star1 = R.drawable.star;
			}
			if(sc.equals("8/10") || sc.equals("9/10")){
				star1 = R.drawable.star;
				star2 = R.drawable.star;
			}
			if(sc.equals("10/10")){
				star1 = R.drawable.star;
				star2 = R.drawable.star;
				star3 = R.drawable.star;
			}
			
			if(difficulty.equals("Easy")){
				color = 0xFF288C8C;
			}
			if(difficulty.equals("Medium")){
				color = 0xFF1E6767;
			}
			if(difficulty.equals("Hard")){
				color = 0xFF113E3E;
			}
			if(unlocked == false){
				
				color = 0xBB888888;
				locked = true;
			}
			
		}
		
		
	}

