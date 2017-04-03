package com.sftrip.library;

import com.sftrip.R;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String> implements
		SpinnerAdapter {

	private Integer[] listedColor;
	private Activity ac;
	private String[] colorTexts;
	
	public CustomSpinnerAdapter(Context context, int textViewResourceId,
			Integer[] colors, String[] colorTexts, Activity ac) {
		super(context, textViewResourceId, colorTexts);
		// TODO Auto-generated constructor stub
		this.listedColor = colors;
		this.ac = ac;
		this.colorTexts = colorTexts;
	}	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View rowView = convertView;
		LayoutInflater inflater = ac.getLayoutInflater();
		rowView = inflater.inflate(R.layout.spinner_row, null);
		TextView textView = (TextView)rowView.findViewById(R.id.spinner_row_text);
		rowView.setBackgroundResource(listedColor[position]);
		textView.setText(colorTexts[position]);
		return rowView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View rowView = convertView;
		LayoutInflater inflater = ac.getLayoutInflater();
		rowView = inflater.inflate(R.layout.spinner_lists, null);
		TextView textView = (TextView)rowView.findViewById(R.id.spinner_list_text);
		rowView.setBackgroundResource(listedColor[position]);
		textView.setText(colorTexts[position]);
		return rowView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return colorTexts.length;
	}

	@Override
	public String getItem(int arg0) {
		// TODO Auto-generated method stub
		return colorTexts[arg0];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return listedColor[position];
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

}
