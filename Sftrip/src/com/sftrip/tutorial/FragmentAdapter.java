package com.sftrip.tutorial;

import com.viewpagerindicator.IconPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.widget.Button;

public class FragmentAdapter extends FragmentStatePagerAdapter implements
		IconPagerAdapter {

	public FragmentAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	public int getIconResId(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		Fragment fragment = new FirstActivity();
		switch (position) {
		case 0:
			fragment = new FirstActivity();
			break;
		case 1:
			fragment = new SecondActivity();
			break;
		case 2:
			fragment = new ThirdActivity();
			break;
		case 3:
			fragment = new FourthActivity();
			break;
		case 4:
			fragment = new FifthActivity();
			break;
		case 5:
			fragment = new SixthActivity();
			break;
		case 6:
			fragment = new SeventhActivity();
			break;
		case 7:
			fragment = new EighthActivity();
			break;
		case 8:
			fragment = new NinethActivity();
			break;
		case 9:
			fragment = new TenthActivity();
			break;
		case 10:
			fragment = new ElevenActivity();
			break;
		}
		return fragment;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return 11;
	}


}
