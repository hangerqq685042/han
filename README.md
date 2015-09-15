FlowRadioGroup by han
========
situation:
1.while we don't know how much RadioButton we need to add to RadioGroup

2.we want to add other widget to RadioGroup

3.we need the RadioButton we add can automatically add after the last one we add previously,
and the RadioButton will add to a new line,while the lastrow doesn't have enough room to add
this RadioButton


so the characters of FlowRadioGroup:
1.the new RadioButton will add after the last one we add,if FlowRadioGroup have no more room
to add RadioButton,the RadioButton will be added to FlowRadioGroup in a new line.

2.we can add other widgets to FlowRadioGroup,and this won't effect RadioButton's android:checked



sum up,
if you can't confirm how much RadioButton you need to add,use FlowRadioGroup!
if you want to add RadioButton automatically,use FlowRadioGroup!
if you want to add not only RadioButton,but something else,use FlowRadioGroup!

=========

I have declare some attributions:
<declare-styleable name="FlowRadioGroup">
        <attr name="android:orientation" />
        <attr name="radio_direction" format="enum">
            <enum name="ltr" value="0" />
            <enum name="rtl" value="1" />
        </attr>
        <attr name="debug_draw" format="boolean" />
        <attr name="weight_default" format="float" />
        <attr name="android:gravity" />
    </declare-styleable>
    <declare-styleable name="FlowRadioGroup_LayoutParams">
        <attr name="radio_new_line" format="boolean" />
        <attr name="radio_weight" format="float" />
        <attr name="android:layout_gravity" />
    </declare-styleable>
	
you can choose some one to use.

=========

how to use:

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
//		radio.setGravity(Gravity.CENTER);
		radio.setLayoutParams(lp);

		mGroup.addView(radio);
		
	}

note:this is the demo which add RadioButton in automatical way.







