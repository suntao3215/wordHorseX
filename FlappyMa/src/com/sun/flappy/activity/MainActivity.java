package com.sun.flappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		Button _startButton = (Button) findViewById(R.id.btn_start);
		_startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent _intent = new Intent(MainActivity.this,
						DrawViewActivity.class);
				startActivity(_intent);
			}
		});
	}
}
