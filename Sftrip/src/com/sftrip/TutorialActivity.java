package com.sftrip;

import com.sftrip.R;
import com.sftrip.tutorial.FragmentAdapter;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TutorialActivity extends FragmentActivity {
    
    private FragmentAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private Button btn_tutorial_start;

    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_main); 
        
        mAdapter = new FragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator   = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        btn_tutorial_start = (Button)findViewById(R.id.btn_tutorial_start);
        btn_tutorial_start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(TutorialActivity.this, MainActivity.class));
				finish();
			}
		});
        
    }
    
    
}