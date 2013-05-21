package com.vishwa.picspeak;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



	public class SetAdapter extends ArrayAdapter<Set>{
		Context mContext;
		int mLayoutResourceId;
		ArrayList<Set> mData = null;
		
		public SetAdapter(Context context, int layoutResourceId, ArrayList<Set> data){
			super(context, layoutResourceId, data);
			this.mLayoutResourceId = layoutResourceId;
			this.mContext = context;
			this.mData = data;
		}

		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        SetInfo setInfo = null;
	       
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
	            row = inflater.inflate(mLayoutResourceId, parent, false);
	            
	           
	            setInfo = new SetInfo();
	            setInfo.star1Icon = (ImageView)row.findViewById(R.id.star1Icon);
	            setInfo.star2Icon = (ImageView)row.findViewById(R.id.star2Icon);
	            setInfo.star3Icon = (ImageView)row.findViewById(R.id.star3Icon);
	            setInfo.category = (TextView)row.findViewById(R.id.txtCategory);
	            setInfo.difficulty =(TextView)row.findViewById(R.id.txtDifficulty);
	            setInfo.lockedIcon = (ImageView)row.findViewById(R.id.lockedicon);
	            row.setTag(setInfo);
	        }
	        else
	        {
	            setInfo = (SetInfo)row.getTag();
	        }
	        Set currentSet = mData.get(position);
	        setInfo.category.setText(currentSet.getSetCategory());
	        setInfo.difficulty.setText(currentSet.getSetDifficulty());
	        setInfo.star1Icon.setImageResource(currentSet.getStar1());
	        setInfo.star2Icon.setImageResource(currentSet.getStar2());
	        setInfo.star3Icon.setImageResource(currentSet.getStar3());
	        if(currentSet.isSetLocked()){
	        setInfo.lockedIcon.setImageResource(R.drawable.locked);
	        }
	        else{
	        	setInfo.lockedIcon.setImageResource(R.drawable.checkmark_2);
	        }
	        row.setBackgroundColor(currentSet.getColor());
	        return row;
	    }
	   
	    static class SetInfo
	    {
	        ImageView star1Icon;
	        ImageView star2Icon;
	        ImageView star3Icon;
	        TextView category;
	        TextView difficulty;
	        ImageView lockedIcon;
	    }
	
	}
	

