package com.example.scaleimageview;

import com.serenity.view.ScaleImageView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainActivity extends Activity {


	private ViewPager viewPager;
	private int[] images = {R.drawable.new_year,R.drawable.new_year2,R.drawable.new_year3};
	private ImageView[] imageViewArray = new ImageView[images.length];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		viewPager = (ViewPager)findViewById(R.id.viewPager);
		viewPager.setAdapter(new PagerAdapter() {
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ScaleImageView scaleImageView = new ScaleImageView(getApplicationContext());
				scaleImageView.setImageResource(images[position]);
				container.addView(scaleImageView);
				imageViewArray[position] = scaleImageView; 
				return scaleImageView;
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView(imageViewArray[position]);
			}
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0==arg1;
			}
			
			@Override
			public int getCount() {
				return imageViewArray.length;
			}
		});
	}
}
