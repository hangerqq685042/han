package com.han.flowradiogroup;

import com.han.flowradiogroup.widget.FlowRadioGroup;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;

public class MainActivity extends Activity {
	
	private FlowRadioGroup mGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mGroup = (FlowRadioGroup) findViewById(R.id.flowRadioGroup);
		
		
	}
	
	/* add a button,this method will be invoked while the button was clicked
	 * 
	 * 
	 * */
	public void onClick(View view) {
		RadioButton radioBtn = (RadioButton) findViewById(R.id.radioButton1);
		FlowRadioGroup.LayoutParams lps = (FlowRadioGroup.LayoutParams) radioBtn.getLayoutParams();
		int mWidth = lps.width;
		int mHeidht = lps.height;
		int mMarginLeft = lps.leftMargin;
		int mMarginRight = lps.rightMargin;
		int mMarginTop = lps.topMargin;
		int mMarginBottom = lps.bottomMargin;
		
		RadioButton radio = new RadioButton(this);
		radio.setText("han");
		radio.setButtonDrawable(getResources().getDrawable(
				android.R.color.transparent));
		radio.setBackgroundResource(R.drawable.bm_rbt);

		FlowRadioGroup.LayoutParams lp = new FlowRadioGroup.LayoutParams(mWidth, mHeidht);
//		lp.leftMargin = mMarginLeft;
		
		lp.leftMargin = (int)getResources().getDimension(R.dimen.product_popup_window_dimen);
		
		lp.rightMargin = mMarginRight;
		lp.topMargin = mMarginTop;
		lp.bottomMargin = mMarginBottom;
		radio.setPadding(10, 0, 0, 0);
//		radio.setGravity(Gravity.CENTER);//未有需要的效果
		radio.setLayoutParams(lp);

		mGroup.addView(radio);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
