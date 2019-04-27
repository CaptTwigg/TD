package com.jahn.emptygameengine.TD;

import android.graphics.Bitmap;

public
class Tower {
  Bitmap skin;
  int    x;
  int    y;
  int    width;
  int    height;

  Boolean placed = false;
  int     level  = 1;
  int     dmg    = 10;
  int     hp     = 10;

  public
  Tower(Bitmap skin, int x, int y) {
    this.skin = skin;
    this.x = x;
    this.y = y;
    this.width = skin.getWidth();
    this.height = skin.getHeight();

  }

  boolean isTouched() {
    int touchX = MainScreen.gameEngine.getTouchX(0);
    int touchY = MainScreen.gameEngine.getTouchY(0);
    return (
      touchX >= x &&
        touchX <= x + width &&
        touchY >= y &&
        touchY <= y + height
    );
  }
}
