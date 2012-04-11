package com.hunterdavis.skillfuldodge;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

class Panel extends SurfaceView implements SurfaceHolder.Callback {

	InventorySQLHelper scoreData = null;

	// member variables
	private CanvasThread canvasthread;
	public Boolean surfaceCreated;
	public Context mContext;
	int difficulty = 0;
	public Boolean gameOver = false;
	public Boolean gamePaused = false;
	public Boolean introScreenOver = false;
	Point player1Pos = new Point(25, 25);
	Point player1Wants = new Point(0, 0);
	public Bitmap cometBitmap = null;
	public Bitmap cometBitmapLarge = null;
	public Bitmap cometBitmapSmall = null;
	public Bitmap player1Bitmap = null;
	public Bitmap player1IconBitmap = null;
	public Uri selectedImageUri = null;
	public Uri selectedPlayerUri = null;
	String playerName = null;
	int mwidth = 0;
	int mheight = 0;
	int player1Score = 0;
	int cometSize = 0;
	int player1Lives = 0;
	int player1Health = 0;
	int enemiesKilled = 0;
	int enemiesPerLevel = 0;
	int enemiesKilledThisLevel = 0;
	int level = 1;
	int enemyDamage = 0;
	int numberOfEnemiesAllowed = 0;
	int numberOfBulletsAllowed = 0;
	int weaponLevel = 0;
	Random myrandom = new Random();
	List<Enemy> enemyList;
	int clearPosition = 0;
	int changedLast = 0;

	// tweaking for game mechanics
	int clearPositionChangeConstant = 5;
	int enemiesPerLevelConstant = 6;
	int numberOfBloodSpots = 5000;
	int maxDepthValue = 5;
	int player1Step = 2;
	int player1IconSize = 12;
	int bloodTTL = 10;
	int bulletStep = 10;
	int player1Size = 5;
	int emenyStartDamage = 4;
	int player1StartingHealth = 40;
	int boomStickHurtValue = 50;
	int playerColor = Color.rgb(204, 8, 57);
	int scoreColor = Color.rgb(0, 0, 234);
	int healthBarColor = Color.rgb(255, 35, 35);
	int enemyHealthColor = Color.rgb(255, 65, 65);
	int enemyBloodColor = Color.rgb(0, 165, 233);
	int bulletColor = Color.rgb(47, 79, 79);
	int player1LivesStarting = 3;
	int maxBulletSize = 25;
	int initialNumberOfEnemies = 10;
	int numCracks = 3;
	Boolean shootReverse = false;
	private static final float EPS = (float) 0.000001;

	public class Enemy {
		public int x;
		public int y;
		public int size;
		public int healthPoints;
		public Boolean left;
		public Boolean down;

		Enemy(int xa, int ya, int sizea, int healtha, Boolean lefta,
				Boolean downa) {
			x = xa;
			y = ya;
			size = sizea;
			healthPoints = healtha;
			left = lefta;
			down = downa;
		}

		Enemy(int xa, int ya) {
			x = xa;
			y = ya;
			size = myrandom.nextInt(3);
			healthPoints = (int) Math.pow(2, size);
			left = myrandom.nextBoolean();
			down = myrandom.nextBoolean();

		}

		Enemy(Enemy newComet) {
			x = newComet.x;
			y = newComet.y;
			size = newComet.size;
			healthPoints = newComet.healthPoints;
			left = newComet.left;
			down = newComet.down;
		}
	}

	public class bloodPoint {
		public int x;
		public int y;
		public int age;

		bloodPoint(int xa, int ya, int agea) {
			x = xa;
			y = ya;
			age = agea;
		}

		bloodPoint(bloodPoint b) {
			x = b.x;
			y = b.y;
			age = b.age;

		}

		bloodPoint() {
			x = myrandom.nextInt(mwidth);
			y = myrandom.nextInt(mheight);
			age = 0;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {

			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {

				player1Wants.x = (int) event.getX();
				player1Wants.y = (int) event.getY();

				if (gamePaused == true) {
					gamePaused = false;
				}

				return true;
			} else if (action == MotionEvent.ACTION_MOVE) {

				player1Wants.x = (int) event.getX();
				player1Wants.y = (int) event.getY();

				return true;
			} else if (action == MotionEvent.ACTION_UP) {

				return true;
			}
			return true;
		}
	}

	public void setDifficulty(int difficult) {
		difficulty = difficult;
		initialNumberOfEnemies = (difficulty + 2) * 2;
		maxBulletSize = 25 + (5 * difficult);
		reset();
	}

	public void setUri(Uri uri) {
		selectedImageUri = uri;
		cometBitmap = null;
	}

	public void setScoreData(InventorySQLHelper scoreDataB) {
		scoreData = scoreDataB;
	}

	public void changeName(String name) {
		playerName = name;
	}

	public void setPlayerUri(Uri uri) {
		selectedPlayerUri = uri;
		cometBitmap = null;
	}

	public void setShootReverse(Boolean shot) {
		shootReverse = shot;
	}

	public void reset() {
		// reset everything
		gameOver = false;
		gamePaused = true;
		introScreenOver = false;
		changedLast = 0;
		enemiesPerLevel = enemiesPerLevelConstant;
		player1Score = 0;
		player1Lives = player1LivesStarting;
		player1StartingHealth = 20;
		player1Health = player1StartingHealth;
		enemiesKilled = 0;
		enemiesKilledThisLevel = 0;
		weaponLevel = 0;
		level = 1;
		player1Pos.x = mwidth / 2;
		player1Pos.y = mheight - player1Size;
		enemyDamage = emenyStartDamage;
		numberOfEnemiesAllowed = initialNumberOfEnemies;
		numberOfBulletsAllowed = maxBulletSize;

		// clear lists
		enemyList = new ArrayList();

		cometBitmap = null;
		cometBitmapLarge = null;
		cometBitmapSmall = null;
		player1Bitmap = null;
	}

	float fdistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public Panel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		//
		surfaceCreated = false;

		reset();

		getHolder().addCallback(this);
		setFocusable(true);
	}

	public void createThread(SurfaceHolder holder) {
		canvasthread = new CanvasThread(getHolder(), this, mContext,
				new Handler());
		canvasthread.setRunning(true);
		canvasthread.start();
	}

	public void terminateThread() {
		canvasthread.setRunning(false);
		try {
			canvasthread.join();
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		reset();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//
		if (surfaceCreated == false) {
			createThread(holder);
			// Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
			// R.drawable.kangoo);
			surfaceCreated = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;

	}

	Boolean testXBounds(int x) {
		if (x < 0) {
			return true;
		}
		if (x > mwidth) {
			return true;
		}
		return false;
	}

	Boolean testYBounds(int y) {
		if (y < 0) {
			return true;
		}
		if (y > mheight) {
			return true;
		}
		return false;
	}

	public void movePlayer1Tick() {
		if (introScreenOver == false) {
			player1Pos.x = mwidth / 2;
			player1Pos.y = mheight - player1Size;
			introScreenOver = true;
		}

		int x = player1Wants.x;
		int y = player1Wants.y;

		if (x > (player1Pos.x + (player1Size / 2))) {
			if ((x - player1Pos.x) > player1Step) {
				player1Pos.x += player1Step;
			}
		} else if (x < (player1Pos.x)) {
			if (player1Pos.x > 0) {
				if ((player1Pos.x - x) >= player1Step) {
					player1Pos.x -= player1Step;
				} else {
					player1Pos.x -= (player1Pos.x - x);
				}
			}
		}

		// now for a mirror Y tick
		if (y > (player1Pos.y + (player1Size / 2))) {
			if ((y - player1Pos.y) > player1Step) {
				player1Pos.y += player1Step;
			}
		} else if (y < (player1Pos.y)) {
			if (player1Pos.y > 0) {
				if ((player1Pos.y - y) >= player1Step) {
					player1Pos.y -= player1Step;
				} else {
					player1Pos.y -= (player1Pos.y - y);
				}
			}
		}

	}

	public void moveEnemiesTick() {
		for (int i = 0; i < enemyList.size(); i++) {
			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			// we won't move the ball the first tick so we have the correct
			// screen
			// width and hieght
			if ((mwidth == mheight) && (mwidth == 0)) {
				return;
			}

			try {
				enemyList.set(i, incrementEnemyOnLine(enemy));
			} catch (Exception e) {
				//
				return;
			}

		}
	}

	public void testForCollisionAndProcess() {

		// loop through all comets
		Rect playerRect = new Rect();
		playerRect.left = player1Pos.x - player1Size / 2;
		playerRect.right = player1Pos.x + player1Size / 2;
		playerRect.top = player1Pos.y + player1Size / 2;
		playerRect.bottom = player1Pos.y - player1Size / 2;

		for (int i = enemyList.size() - 1; i >= 0; i--) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			int localEnemySize = cometSize;
			if (enemy.size == 0) {
				localEnemySize = cometSize / 2;
			} else if (enemy.size == 2) {
				localEnemySize = cometSize * 2;
			}

			Rect enemyRect = new Rect();
			enemyRect.left = enemy.x - localEnemySize / 2;
			enemyRect.right = enemy.x + localEnemySize / 2;
			enemyRect.top = enemy.y + localEnemySize / 2;
			enemyRect.bottom = enemy.y - localEnemySize / 2;

			if (enemyRect.bottom > (mheight - 2)) {
				try {
					enemyList.remove(i);
					incrementEnemiesKilled();
				} catch (Exception e) {
					return;
				}
			} else {

				// test if player hit a comet
				boolean collision = doTheyOverlap(enemyRect, playerRect);
				// CollisionTest(playerRect,cometRect);
				if (collision != false) {
					decrementHealth();
					player1Score -= 100;
					try {
						enemyList.remove(i);
					} catch (Exception e) {
						//
						return;
					}
					return;
				}
			}

		}
	}

	public boolean betweenOrOn(int a, int b, int c) {
		if (a >= b) {
			if (a <= c) {
				return true;
			}
		}
		return false;
	}

	public boolean doTheyOverlap(Rect one, Rect two) {

		// left side of one is in two
		if (betweenOrOn(one.left, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}

			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}

		}
		// right side of one is in two
		// left side of one is in two
		if (betweenOrOn(one.right, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		// one is bigger and contains two
		if (betweenOrOn(two.left, one.left, one.right)
				&& betweenOrOn(two.right, one.left, one.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		return false;
	}

	public void saveHighScore() {
		SQLiteDatabase db = scoreData.getWritableDatabase();
		ContentValues values = new ContentValues();
		String scoreString = level + " " + playerName;
		values.put(InventorySQLHelper.NAMES, scoreString);
		values.put(InventorySQLHelper.SCORES, player1Score);
		long latestRowId = db.insert(InventorySQLHelper.TABLE, null, values);
		db.close();
	}

	public void decrementHealth() {

		gamePaused = true;
		player1Lives--;
		player1Health = player1StartingHealth;
		if (player1Lives < 1) {
			gameOver = true;
			gamePaused = false;
			saveHighScore();

		}

		// move the player to the middle
		player1Pos.x = mwidth / 2;
		player1Pos.y = mheight - player1Size;

	}

	public void updateGameState() {

		if (gameOver == true) {
			return;
		}

		if (gamePaused == true) {
			return;
		}

		// make sure there's a graphics init round
		if (mwidth == 0) {
			return;
		}

		// update the score a point for being alive
		player1Score++;

		// make sure there are enough comets onscreen
		int cometDiff = numberOfEnemiesAllowed - enemyList.size();
		Boolean randomBool = myrandom.nextBoolean();
		if (cometDiff > 0) {
			// for (int i = 0; i < cometDiff; i++) {
			if (randomBool == true) {
				generateEnemy();
			}
			// }
		}

		// move player 1 a tick
		for (int i = 0; i < level; i += 2) {
			movePlayer1Tick();
		}
		movePlayer1Tick();

		// move all asteroids a tick
		moveEnemiesTick();

		// change the position that enemies cannot spawn at
		changeClearPosition();

		// test for bullet or player collision
		testForCollisionAndProcess();

	}

	public void changeClearPosition() {
		if (clearPosition == 0) {
			clearPosition = myrandom.nextInt(mwidth);
		}

		if (changedLast < clearPositionChangeConstant) {
			changedLast++;
			return;
		} else {
			changedLast = 0;
		}

		if (clearPosition + (player1Size / 2) > (mwidth - (player1Size / 2) - 3)) {
			clearPosition = myrandom.nextInt(mwidth);
		} else {
			clearPosition++;
		}

	}

	public void generateEnemy() {

		int x = myrandom.nextInt(mwidth);
		int y = 0;

		int size = myrandom.nextInt(3);
		int enemySize = cometSize;
		if (size == 0) {
			enemySize = cometSize / 2;
		} else if (size == 2) {
			enemySize = cometSize * 2;
		}

		int xleft = x - enemySize / 2;
		int xright = x + enemySize / 2;

		for (int i = xleft; i < xright; i++) {
			if (i > (clearPosition - player1Size - 3)
					&& (i < (clearPosition + player1Size + 3))) {
				if ((clearPosition - enemySize) > 0) {
					x = clearPosition - player1Size / 2 - 3 - enemySize / 2;
				} else {
					x = clearPosition + player1Size / 2 + 3 + enemySize / 2;
				}
			}
		}

		int health = 1;
		boolean left = true;
		boolean down = true;

		Enemy myComet = new Enemy(x, y, size, health, left, down);
		enemyList.add(myComet);

	}

	@Override
	public void onDraw(Canvas canvas) {

		mwidth = canvas.getWidth();
		mheight = canvas.getHeight();

		Paint paint = new Paint();

		// our player sizes should be a function both of difficulty and of
		// screen size
		int visualDivisor = mwidth;
		if (mheight < mwidth) {
			visualDivisor = mheight;
		}
		player1Size = visualDivisor / 8;
		cometSize = visualDivisor / 12;
		int cometSizeSmall = cometSize / 2;
		int cometSizeLarge = cometSize * 2;

		// draw player 1
		if (introScreenOver == true) {

			if (player1Bitmap == null) {
				// cometSize = mwidth / 5;
				// if we can't load somebody else's bitmap
				if (selectedPlayerUri == null) {
					Bitmap _scratch = BitmapFactory.decodeResource(
							getResources(), R.drawable.trollface);

					if (_scratch == null) {
						Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
								.show();
					}

					// now scale the bitmap using the scale value
					player1Bitmap = Bitmap.createScaledBitmap(_scratch,
							player1Size, player1Size, false);
					player1IconBitmap = Bitmap.createScaledBitmap(_scratch,
							player1IconSize, player1IconSize, false);
				} else {
					// THIS IS WHERE YOU LOAD FILE URIS AT
					InputStream photoStream = null;

					Context context = getContext();
					try {
						photoStream = context.getContentResolver()
								.openInputStream(selectedPlayerUri);
					} catch (FileNotFoundException e) {
						//
						e.printStackTrace();
					}
					int scaleSize = decodeFile(photoStream, player1Size,
							player1Size);

					try {
						photoStream = context.getContentResolver()
								.openInputStream(selectedPlayerUri);
					} catch (FileNotFoundException e) {
						//
						e.printStackTrace();
					}
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scaleSize;

					Bitmap photoBitmap = BitmapFactory.decodeStream(
							photoStream, null, o);
					player1Bitmap = Bitmap.createScaledBitmap(photoBitmap,
							cometSize, cometSize, true);
					player1IconBitmap = Bitmap.createScaledBitmap(photoBitmap,
							player1IconSize, player1IconSize, false);
					photoBitmap.recycle();

				}

			}
			canvas.drawBitmap(player1Bitmap, player1Pos.x - (player1Size / 2),
					player1Pos.y - (player1Size / 2), paint);

		}// introscreenover = true

		// draw comets
		if (cometBitmap == null) {
			// cometSize = mwidth / 5;
			// if we can't load somebody else's bitmap
			if (selectedImageUri == null) {
				Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
						R.drawable.megusta);

				if (_scratch == null) {
					Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
							.show();
				}

				// now scale the bitmap using the scale value
				cometBitmap = Bitmap.createScaledBitmap(_scratch, cometSize,
						cometSize, false);
				cometBitmapLarge = Bitmap.createScaledBitmap(_scratch,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(_scratch,
						cometSizeSmall, cometSizeSmall, false);
			} else {
				//
				// THIS IS WHERE YOU LOAD FILE URIS AT
				InputStream photoStream = null;

				Context context = getContext();
				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					//
					e.printStackTrace();
				}
				int scaleSize = decodeFile(photoStream, cometSize, cometSize);

				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inSampleSize = scaleSize;

				Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream,
						null, o);
				cometBitmap = Bitmap.createScaledBitmap(photoBitmap, cometSize,
						cometSize, true);
				cometBitmapLarge = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeSmall, cometSizeSmall, false);
				photoBitmap.recycle();

			}

		}

		// draw the comet bitmaps all over
		// for each comet
		for (int i = enemyList.size() - 1; i >= 0; i--) {
			int enemyListSize = enemyList.size();
			if (enemyListSize > i) {

				Enemy myComet;
				try {
					myComet = enemyList.get(i);
				} catch (Exception e) {
					//
					return;
				}

				switch (myComet.size) {
				case 0:
					canvas.drawBitmap(cometBitmapSmall, myComet.x
							- (cometSizeSmall / 2), myComet.y
							- (cometSizeSmall / 2), paint);

					break;
				case 1:

					canvas.drawBitmap(cometBitmap, myComet.x - (cometSize / 2),
							myComet.y - (cometSize / 2), paint);

					break;
				case 2:

					canvas.drawBitmap(cometBitmapLarge, myComet.x
							- (cometSizeLarge / 2), myComet.y
							- (cometSizeLarge / 2), paint);

					break;
				default:
					Toast.makeText(getContext(), "Error in Enemy Rendering",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
		// - draw comet in its position
		// - draw comet with its level of asplosion
		// - after drawing a comet that's fully asploded
		// - - remove it from the list
		// - - - generate a new comet
		// generateComet();
		if (introScreenOver == true) {
			// draw score 1
			paint.setColor(scoreColor);
			// draw score 2
			String nameString;
			nameString = playerName;
			canvas.drawText(nameString, 0, 22, paint);
			nameString = "Score:" + String.valueOf(player1Score);
			canvas.drawText(nameString, 0, 9, paint);

			// for each live left draw a tiny life bitmap
			for (int i = 0; i < player1Lives; i++) {
				Paint painter = new Paint();
				canvas.drawBitmap(player1IconBitmap,
						((3 + player1IconSize) * i), 24, painter);
			}

			// draw the level number
			String levelString = "Level " + level;
			canvas.drawText(levelString, mwidth - 50, 9, paint);

		}
		// draw game over if game over
		if (gameOver == true) {

			paint.setTextSize(20);
			canvas.drawText("Game Over", (mwidth / 2) - 50, mheight / 4, paint);

		}

		if (gamePaused == true) {
			paint.setTextSize(25);
			paint.setColor(enemyBloodColor);
			canvas.drawText("Touch To Continue", (mwidth / 2) - 110, mheight
					- (mheight / 5), paint);
		}

	}

	public void incrementEnemiesKilled() {

		enemiesKilledThisLevel++;
		enemiesKilled++;
		if (enemiesKilledThisLevel >= enemiesPerLevel) {
			level++;
			// levelUpCharacter();
			enemiesKilledThisLevel = 0;
			numberOfEnemiesAllowed++;
			// numberOfBulletsAllowed += 5;
			enemiesPerLevel += enemiesPerLevelConstant;
		}
	}

	public void levelUpCharacter() {
		player1StartingHealth += level * 2;
		player1Health += level;

		if (level == 3) {
			weaponLevel++;
		}
		if (level == 6) {
			weaponLevel++;
		}
		if (level == 12) {
			weaponLevel++;
		}
		if (level == 20) {
			weaponLevel++;
		}

	}

	private void drawCracks(Canvas canvas, int left, int right, int top,
			int bottom) {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		int xa, xb, ya, yb;
		int width = right - left;
		int height = top - bottom;
		for (int i = 0; i < numCracks; i++) {
			xa = myrandom.nextInt(width) + left;
			xb = myrandom.nextInt(width) + left;
			ya = myrandom.nextInt(height) + bottom;
			yb = myrandom.nextInt(height) + bottom;
			canvas.drawLine(xa, ya, xb, yb, paint);
		}
	}

	private void drawExplosion(Canvas canvas, int left, int right, int top,
			int bottom) {
		int x = (left + right) / 2;
		int y = (top + bottom) / 2;
		int radius = (right - left) / 2;

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawCircle(x, y, radius, paint);

		canvas.drawLine(x - radius / 2, y + radius / 2, x + radius / 2, y
				+ radius / 2, paint);

		int eyestop = y - radius / 4;
		int eyesbottom = y - radius / 2;
		int lefteyeleft = x - radius / 2;
		int lefteyeright = x - radius / 4;
		int righteyeleft = x + radius / 4;
		int righteyeright = x + radius / 2;

		// left eye x
		canvas.drawLine(lefteyeleft, eyestop, lefteyeright, eyesbottom, paint);
		canvas.drawLine(lefteyeright, eyestop, lefteyeleft, eyesbottom, paint);

		// right eye x
		canvas.drawLine(righteyeleft, eyestop, righteyeright, eyesbottom, paint);
		canvas.drawLine(righteyeright, eyestop, righteyeleft, eyesbottom, paint);

	}

	// decodes image and scales it to reduce memory consumption
	private int decodeFile(InputStream photostream, int h, int w) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(photostream, null, o);

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < w || height_tmp / 2 < h)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		return scale;
	}

	public Enemy incrementEnemyOnLine(Enemy enemy) {
		Enemy returnEnemy = new Enemy(enemy);
		int enemyIncrement = 1 + (level);
		returnEnemy.y += enemyIncrement;
		return returnEnemy;
	}

} // end class