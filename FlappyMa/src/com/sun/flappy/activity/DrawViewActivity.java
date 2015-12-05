package com.sun.flappy.activity;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

public class DrawViewActivity extends Activity {

	public static int mWidth = 0;
	public static int mHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_draw);

		View _view = findViewById(R.id.view_main);
		getScreenWH();
		DrawView _drawView = new DrawView(_view.getContext(), null);
		setContentView(_drawView);
	}

	private void getScreenWH() {
		DisplayMetrics _metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(_metrics);
		mHeight = _metrics.heightPixels;
		mWidth = _metrics.widthPixels;
	}

	private class DrawView extends View implements Runnable {

		boolean bInit, bReady, bGameOver;// 游戏的各种状态

		private Bitmap backgroundBitmap, background_guideBitmap;
		private Bitmap birdLeftBitmap, birdRightBitmap;
		private Bitmap getReadyBitmap, gameOverBitmap;
		private Bitmap pillarHeadBitmap, pillarMidBitmap, pillarMid1Bitmap,
				pillarMid2Bitmap;
		private Bitmap number0, number1, number2, number3, number4, number5,
				number6, number7, number8, number9;

		private static final int SPEED = 4; // 移动速度

		private int CRACK = 0; // 缝隙的宽度

		private int mBirdLeft = 0; // 鸟x轴坐标

		private int bLeft, b = 0, gravity = -200, birdHeight, score;

		private RefreshHandler mRedrawHandler;

		private Obstacle pillar[] = new Obstacle[2];

		public DrawView(Context context, AttributeSet attrs) {
			super(context, attrs);
			// 获得焦点
			setFocusable(true);
			bInit = false;
			bReady = false;
			bGameOver = false;

			pillar[0] = new Obstacle();
			pillar[1] = new Obstacle();

			initBitmap();

			mRedrawHandler = new RefreshHandler();
			new Thread(this).start();
		}

		@Override
		public void run() {
			while (true) {
				Message _msg = new Message();
				_msg.what = 0x101;
				mRedrawHandler.sendMessage(_msg);
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			gameDraw(canvas);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getPointerCount()) {
			case 1:
				return onSingleTouchEvent(event);
			case 2:
				return onDoubleTouchEvent(event);
			default:
				return false;
			}
		}

		// 单手指触屏处理
		private boolean onSingleTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				break;
			case MotionEvent.ACTION_UP:
				if (!bInit) {
					bLeft = 0;
					score = 0; // 初始化分数
					mBirdLeft = mWidth / 2 - 45; // 初始化鸟X轴坐标
					birdHeight = mHeight / 2 - 30; // 初始化鸟的高度
					bInit = true;
				} else if (bInit) {
					if (!bReady) {
						pillar[0].x = mWidth + mWidth / 2; // 初始化柱子X轴位置
						pillar[1].x = 2 * mWidth;
						bReady = true;
						CRACK = birdLeftBitmap.getHeight() * 2
								+ birdLeftBitmap.getHeight() / 2; // 更改缝隙为2.5倍鸟的高度
					} else if (bReady) {
						if (!bGameOver) {
							gravity = gravity - 400;
						} else if (bGameOver) {
							bInit = false;
							bReady = false;
							bGameOver = false;
						}
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:

				break;
			}
			return true;
		}

		// 双指触屏处理
		private boolean onDoubleTouchEvent(MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_UP:

				break;
			case MotionEvent.ACTION_POINTER_DOWN: {

				break;
			}
			case MotionEvent.ACTION_MOVE:
				postInvalidate();
				break;
			}
			return true;
		}

		private void gameDraw(Canvas canvas) {
			if (!bInit) {
				canvas.drawBitmap(backgroundBitmap, 0, 0, null);
				canvas.drawBitmap(getReadyBitmap,
						mWidth / 2 - getReadyBitmap.getWidth() / 2,
						mHeight / 6, null);
				canvas.drawBitmap(background_guideBitmap, mWidth / 2
						- background_guideBitmap.getWidth() / 2, mHeight / 2,
						null);
			} else if (bInit) {

				bLeft = bLeft - SPEED;

				if (!bReady) {
					drawBackground(canvas);
					drawBird(canvas);
				} else if (bReady) {
					if (!bGameOver) {
						drawBackground(canvas); // 绘制背景
						computeBirdHeight(); // 计算鸟的高度

						drawBird(canvas); // 绘制鸟
						drawPillar(canvas); // 绘制障碍

						computeCollision(); // 计算碰撞
						computeScore(); // 计算分数
						drawScore(canvas);
					} else if (bGameOver) {
						canvas.drawBitmap(gameOverBitmap, mWidth / 2
								- gameOverBitmap.getWidth() / 2, mHeight / 6,
								null);
						drawScore(canvas);
					}
				}
			}
		}

		/**
		 * 计算分数
		 */
		private void computeScore() {
			for (int i = 0; i < pillar.length; i++) {
				if (pillar[i].x == mBirdLeft - pillarHeadBitmap.getWidth() + 1) {
					score++;
				}
			}
		}

		/**
		 * 计算碰撞
		 */
		private void computeCollision() {
			int _CollisionLeftX = mBirdLeft - pillarHeadBitmap.getWidth();
			int _CollisionRightX = mBirdLeft + birdLeftBitmap.getWidth();
			int _BirdBottom = birdHeight + birdLeftBitmap.getHeight();

			for (int i = 0; i < pillar.length; i++) {
				if (pillar[i].x >= _CollisionLeftX
						&& pillar[i].x <= _CollisionRightX
						&& (_BirdBottom >= pillar[i].h || birdHeight < (pillar[i].h - CRACK))) {
					bGameOver = true;
				}
			}
			if (birdHeight >= mHeight - birdLeftBitmap.getHeight()) {
				bGameOver = true;
			}
		}

		/**
		 * 计算马强高度
		 */
		private void computeBirdHeight() {
			gravity += 9.8;
			if (gravity > 420)
				gravity = 420;
			else if (gravity < -300)
				gravity = -300;
			if (gravity >= 0)
				birdHeight += ((gravity * 5.0) / 77);
			else if (gravity < 0)
				birdHeight += ((gravity * 4.5) / 77);
			if (birdHeight < 0)
				birdHeight = 0;
			else if (birdHeight > mHeight - birdLeftBitmap.getHeight())
				birdHeight = mHeight - birdLeftBitmap.getHeight();
		}

		/**
		 * 绘制分数
		 * 
		 * @param canvas
		 */
		private void drawScore(Canvas canvas) {
			Paint p1 = new Paint();
			p1.setAntiAlias(true);
			p1.setColor(Color.WHITE);
			p1.setTextSize(40);// 设置字体大小
			canvas.drawText("Height:" + birdHeight, 171, 110, p1);
			canvas.drawText("Score:" + score, 171, 150, p1);

			int _Height = 100;
			if (bGameOver) {
				_Height = mHeight / 3 + gameOverBitmap.getHeight();
			}

			if (score < 10) {
				canvas.drawBitmap(returnNumberBitmap(score), mWidth / 2,
						_Height, null);
			} else if (score >= 10 && score < 100) {
				canvas.drawBitmap(returnNumberBitmap(score / 10), mWidth / 2
						- number0.getWidth(), _Height, null);
				canvas.drawBitmap(returnNumberBitmap(score % 10), mWidth / 2,
						_Height, null);
			} else if (score >= 100 && score < 1000) {
				canvas.drawBitmap(returnNumberBitmap(score / 100), mWidth / 2
						- number0.getWidth() * 2, _Height, null);
				canvas.drawBitmap(returnNumberBitmap((score % 100) / 10),
						mWidth / 2 - number0.getWidth(), _Height, null);
				canvas.drawBitmap(returnNumberBitmap((score % 100) % 10),
						mWidth / 2, _Height, null);
			}

		}

		private Bitmap returnNumberBitmap(int number) {
			Bitmap _Bitmap = null;
			switch (number) {
			case 0:
				_Bitmap = number0;
				break;
			case 1:
				_Bitmap = number1;
				break;
			case 2:
				_Bitmap = number2;
				break;
			case 3:
				_Bitmap = number3;
				break;
			case 4:
				_Bitmap = number4;
				break;
			case 5:
				_Bitmap = number5;
				break;
			case 6:
				_Bitmap = number6;
				break;
			case 7:
				_Bitmap = number7;
				break;
			case 8:
				_Bitmap = number8;
				break;
			case 9:
				_Bitmap = number9;
				break;
			}
			return _Bitmap;
		}

		/**
		 * 绘制背景图
		 * 
		 * @param canvas
		 */
		private void drawBackground(Canvas canvas) {
			if (bLeft < 0)
				bLeft = mWidth - 10;
			canvas.drawBitmap(backgroundBitmap, bLeft, 0, null);
			canvas.drawBitmap(backgroundBitmap, bLeft - mWidth + 10, 0, null);
		}

		/**
		 * 绘制马强跳动
		 * 
		 * @param canvas
		 */
		private void drawBird(Canvas canvas) {
			b = bLeft % 64;
			if (b >= 0 && b < 32) {
				canvas.drawBitmap(birdRightBitmap, mBirdLeft, birdHeight, null);
			}
			if (b >= 32 && b < 64) {
				canvas.drawBitmap(birdLeftBitmap, mBirdLeft, birdHeight, null);
			}
		}

		/**
		 * 计算障碍物坐标，绘制障碍物
		 * 
		 * @param canvas
		 */
		private void drawPillar(Canvas canvas) {
			// 三个柱子
			int _pillarWeight = pillarHeadBitmap.getWidth();
			for (int c = 0; c < 2; c++) {
				pillar[c].x -= SPEED;
				if (pillar[c].x <= -52)
					pillar[c].x = mWidth;
				if (pillar[c].x == mWidth) {
					pillar[c].h = (new Random()).nextInt(mHeight / 4) + mHeight
							/ 2;
				}
			}

			for (int i = 0; i < 2; i++) {
				if (pillar[i].x <= mWidth && pillar[i].x >= -_pillarWeight) {
					// 绘制下边柱子
					canvas.drawBitmap(pillarHeadBitmap, pillar[i].x,
							pillar[i].h, null);
					pillarMid1Bitmap = Bitmap.createScaledBitmap(
							pillarMidBitmap, pillarMidBitmap.getWidth(),
							mHeight / 2, false);
					canvas.drawBitmap(pillarMid1Bitmap, pillar[i].x,
							pillar[i].h + pillarHeadBitmap.getHeight(), null);
					// 绘制上边柱子
					canvas.drawBitmap(pillarHeadBitmap, pillar[i].x,
							pillar[i].h - CRACK - pillarHeadBitmap.getHeight(),
							null);
					pillarMid2Bitmap = Bitmap.createScaledBitmap(
							pillarMidBitmap, pillarMidBitmap.getWidth(),
							pillar[i].h - CRACK - pillarHeadBitmap.getHeight(),
							false);
					canvas.drawBitmap(pillarMid2Bitmap, pillar[i].x, 0, null);
				}
			}
		}

		/**
		 * 初始化图片资源
		 */
		private void initBitmap() {
			backgroundBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.background_ground);
			backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap,
					mWidth, mHeight, true);

			background_guideBitmap = BitmapFactory.decodeResource(
					getResources(), R.drawable.guide);
			getReadyBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.getready);
			gameOverBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.gameover);

			birdLeftBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.bird_left);
			birdLeftBitmap = Bitmap.createScaledBitmap(birdLeftBitmap,
					mWidth / 8, mHeight / 8, true);
			birdRightBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.bird_right);
			birdRightBitmap = Bitmap.createScaledBitmap(birdRightBitmap,
					mWidth / 8, mHeight / 8, true);

			pillarHeadBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.pillar_head);
			pillarMidBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.pillar_middle);

			number0 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n0);
			number1 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n1);
			number2 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n2);
			number3 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n3);
			number4 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n4);
			number5 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n5);
			number6 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n6);
			number7 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n7);
			number8 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n8);
			number9 = BitmapFactory.decodeResource(getResources(),
					R.drawable.n9);
		}

		@SuppressLint("HandlerLeak")
		class RefreshHandler extends Handler {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x101) {
					DrawView.this.invalidate();
				}
				super.handleMessage(msg);
			}
		}

		class Obstacle {
			int x;
			int h;

			public Obstacle() {
				this.x = 0;
				this.h = 0;
			}
		}
	}
}
