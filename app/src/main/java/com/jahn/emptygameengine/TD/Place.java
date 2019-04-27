package com.jahn.emptygameengine.TD;

import android.graphics.Bitmap;

public
class Place {
  Bitmap skin;
  int    x;
  int    y;
  int    width;
  int    height;
  Tower tower;

  public
  Place(Bitmap skin, int x, int y) {
    this.skin = skin;
    this.x = x;
    this.y = y;
    this.width = skin.getWidth();
    this.height = skin.getHeight();

  }

  boolean towerAbove(Tower tower) {

    return (
      tower.x <= x + width &&
        tower.x + tower.width >= x &&
        tower.y <= y + height &&
        tower.y + tower.height >= y
    );
  }
  void towerSnap(Tower tower){
    if(towerAbove(tower)){
      tower.x = x;
      tower.y = y;
      this.tower = tower;
      this.tower.placed = true;
    } else{
      this.tower.placed = false;
    }
  }
}
