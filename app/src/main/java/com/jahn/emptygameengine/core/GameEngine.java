package com.jahn.emptygameengine.core;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.jahn.emptygameengine.touch.MultiTouchHandler;
import com.jahn.emptygameengine.touch.TouchEvent;
import com.jahn.emptygameengine.touch.TouchEventPool;
import com.jahn.emptygameengine.touch.TouchHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public abstract
class GameEngine extends AppCompatActivity implements Runnable, TouchHandler, SensorEventListener {

  private static final List<State> STATE_CHANGES = new ArrayList<>();

  private Thread            mainLoopThread;
  private State             state             = State.PAUSED;
  private SurfaceView       surfaceView;
  private SurfaceHolder     surfaceHolder;
  private Canvas            canvas            = null;
  private Screen            screen            = null;
  private Bitmap            offScreenSurface;
  private MultiTouchHandler touchHandler;
  private TouchEventPool    touchEventPool    = new TouchEventPool();
  private List<TouchEvent>  touchEventsBuffer = new ArrayList<>();
  private List<TouchEvent>  touchEventsCopied = new ArrayList<>();
  private float[]           accelerometer     = new float[3]; // to hold the g-forces in three dimensions, x, y, and z
  private SoundPool         soundpool         = new SoundPool.Builder().setMaxStreams(20).build();

  public int width = 1920;
  public int heigth = 1080;

  private int framesPerSecond = 0;
  long  currentTime;
  long  lastTime;
  Paint paint = new Paint();
  public Music music;

  public abstract
  Screen createStartScreen();

  public
  void setScreen(Screen screen) {

    if (screen != null) this.screen.dispose();
    this.screen = screen;
  }

  @Override
  protected
  void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    Objects.requireNonNull(getSupportActionBar()).hide(); // hides the action bar
    this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    // Prepared variables used for drawing on screen
    surfaceView = new SurfaceView(this);
    setContentView(surfaceView); // places view on the physical screen
    surfaceHolder = surfaceView.getHolder();
//        Log.d("GameEngine class", "We just finished the onCreate() method");
    screen = createStartScreen();
    int display_mode = getResources().getConfiguration().orientation;
    if (display_mode == Configuration.ORIENTATION_PORTRAIT) {
      setOffScreenSurface(heigth, width);
    } else setOffScreenSurface(width, heigth );
    touchHandler = new MultiTouchHandler(surfaceView, touchEventsBuffer, touchEventPool);
    SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
      Sensor accelerometer = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
      sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    //this.soundpool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);

  }

  public
  void setOffScreenSurface(int width, int height) {
    if (offScreenSurface != null) offScreenSurface.recycle();
    offScreenSurface = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    canvas = new Canvas(offScreenSurface);
  }

  public
  Bitmap loadBitmap(String fileName) {
    InputStream inputStream = null;
    Bitmap      bitmap      = null;
    try {
      inputStream = getAssets().open(fileName);
      bitmap = BitmapFactory.decodeStream(inputStream);
      if (bitmap == null) {
        throw new RuntimeException("Couldn't load bitmap from file " + fileName);
      }
      return bitmap;
    } catch (IOException ioe) {
      throw new RuntimeException("Couldn't load bitmap from assets folder: " + fileName);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioe) {
          throw new RuntimeException("Couldn't close the inputstream");
        }
      }
    }
  }

  public
  int getFrameBufferWidth() {
    return offScreenSurface.getWidth();
  }

  public
  int getFrameBufferHeight() {
    return offScreenSurface.getHeight();
  }

  public
  void drawBitmap(Bitmap bitmap, int x, int y) {
    if (canvas != null) canvas.drawBitmap(bitmap, x, y, null);
  }

  Rect source      = new Rect();
  Rect destination = new Rect();

  public
  void drawBitmap(Bitmap bitmap,
                  int x,
                  int y,
                  int sourceX,
                  int sourceY,
                  int sourceWidth,
                  int sourceHeight) {
    if (canvas != null) {
      source.left = sourceX;
      source.top = sourceY;
      source.right = sourceX + sourceWidth;
      source.bottom = sourceY + sourceHeight;

      destination.left = x;
      destination.top = y;
      destination.right = x + sourceWidth;
      destination.bottom = y + sourceHeight;

      canvas.drawBitmap(bitmap, source, destination, null);
    }
  }


  public
  void clearFrameBuffer(int color) {
    canvas.drawColor(color);
  }


  public
  Typeface loadFont(String filename) {
    Typeface font = Typeface.createFromAsset(getAssets(), filename);
    if (font == null) throw new RuntimeException("could't load font from: " + filename);

    return font;
  }

  public
  void drawText(Typeface font, String text, int x, int y, int color, int size) {
    paint.setColor(color);
    paint.setTypeface(font);
    paint.setTextSize(size);
    canvas.drawText(text, x, y, paint);

  }

  public
  Sound loadSound(String fileName) {
    try {

      AssetFileDescriptor assetFileDescriptor = getAssets().openFd(fileName);

      int soundId = soundpool.load(assetFileDescriptor, 0);
      return new Sound(soundpool, soundId);
    } catch (IOException e) {
      throw new RuntimeException("Could't load sound file: " + fileName);
    }
  }

  public
  Music loadMusic(String fileName) {
    try {

      AssetFileDescriptor assetFileDescriptor = getAssets().openFd(fileName);
      return new Music(assetFileDescriptor);

    } catch (IOException e) {
      throw new RuntimeException("Could't load music file: " + fileName);
    }
  }

  public
  boolean isTouchDown(int pointer) {
    return touchHandler.isTouchDown(pointer);
  }

  public
  List<TouchEvent> getTouchEvents() {
    return touchEventsCopied;
  }

  /**
   * @return int as a scaled x
   */
  public
  int getTouchX(int pointer) {
    return (int) (((float) touchHandler.getTouchX(
      pointer) * (float) offScreenSurface.getWidth()) / (float) surfaceView.getWidth());
  }

  /**
   * @return int as a scaled y
   */
  public
  int getTouchY(int pointer) {
    return (int) (((float) touchHandler.getTouchY(
      pointer) * (float) offScreenSurface.getHeight()) / (float) surfaceView.getHeight());
  }

  public
  float[] getAccelerometer() {
    return accelerometer;
  }

  @Override
  public
  void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  @Override
  public
  void onSensorChanged(SensorEvent sensorEvent) {
    System.arraycopy(sensorEvent.values, 0, accelerometer, 0, 3);
  }

  private
  void fillEvent() {
    synchronized (touchEventsBuffer) {
      touchEventsCopied.addAll(touchEventsBuffer);
      touchEventsBuffer.clear();
    }
  }

  private
  void freeEvents() {
    synchronized (touchEventsCopied) {
      int stop = touchEventsCopied.size();

      for (int i = 0; i < stop; i++) {
        touchEventPool.free(touchEventsCopied.get(i));
      }
      touchEventsCopied.clear();
    }
  }

  public
  int getFPS() {
    return framesPerSecond;
  }

  public
  void run() {
    //fps calculator
//    int frames = 0;
//    long startTime = System.nanoTime();
    while (true) {
      synchronized (STATE_CHANGES) {
        for (int i = 0; i < STATE_CHANGES.size(); i++) {
          this.state = STATE_CHANGES.get(i);
          if (this.state == State.DISPOSED) {
            //Log.d("GameEngine", "State changed to Disposed");
            return;
          }
          if (this.state == State.PAUSED) {
            //Log.d("GameEngine", "State changed to Pause");
            //this.state = State.PAUSED;
            screen.pause();
          }
          if (this.state == State.RESUMED) {
            //Log.d("GameEngine", "State changed to Resumed");
            this.state = State.RUNNING;
          }
        } // end of for loop
        STATE_CHANGES.clear();
        if (this.state == State.RUNNING) {
          //Log.d("GameEngine running", "" + surfaceHolder.getSurface().isValid());
          if (!surfaceHolder.getSurface().isValid()) {
            continue;
          }
          Canvas canvas = surfaceHolder.lockCanvas();
          // all drawing happens here
          //canvas.drawColor(Color.rgb(0, 0, 255));
          fillEvent();
          currentTime = System.nanoTime();
          if (screen != null)
            screen.update((currentTime - lastTime) / 1000_000_000.0f);
          lastTime = currentTime;
          freeEvents();
          source.left = 0;
          source.top = 0;
          source.right = offScreenSurface.getWidth() - 1;
          source.bottom = offScreenSurface.getHeight() - 1;
          destination.left = 0;
          destination.top = 0;
          destination.right = surfaceView.getWidth();
          destination.bottom = surfaceView.getHeight();
          canvas.drawBitmap(offScreenSurface, source, destination, null);
          surfaceHolder.unlockCanvasAndPost(canvas);
        }
        //fps calculator
//        frames++; // drawn a frame
//        if (System.nanoTime() - startTime > 1000_000_000) {
//          framesPerSecond = frames;
//          frames = 0;
//          startTime = System.nanoTime();
//        }

      }// end of sync
    }  // end of while(true) loop
  }

  @Override
  protected
  void onPause() {
    super.onPause();
    synchronized (STATE_CHANGES) {
      if (isFinishing()) {
        STATE_CHANGES.add(STATE_CHANGES.size(), State.DISPOSED);
      } else
        STATE_CHANGES.add(STATE_CHANGES.size(), State.PAUSED);
    }
    if (isFinishing()) {
      ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
      soundpool.release();
    }

  }

  @Override
  protected
  void onResume() {
    super.onResume();
    mainLoopThread = new Thread(this);
    mainLoopThread.start(); // starts the thread
    synchronized (STATE_CHANGES) {
      STATE_CHANGES.add(STATE_CHANGES.size(), State.RESUMED);
    }
  }
}
