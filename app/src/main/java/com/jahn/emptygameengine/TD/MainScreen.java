package com.jahn.emptygameengine.TD;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.jahn.emptygameengine.core.GameEngine;
import com.jahn.emptygameengine.core.Screen;

import java.util.ArrayList;
import java.util.List;


public
class MainScreen extends Screen {
  static GameEngine gameEngine;

  protected
  MainScreen(GameEngine gameEngine) {
    super(gameEngine);
    this.gameEngine = gameEngine;
    bob = gameEngine.loadBitmap("bob.png");
    sand = gameEngine.loadBitmap("sand.jpg");
    background = gameEngine.loadBitmap("grid.png");
    background = Bitmap.createScaledBitmap(background, gameEngine.heigth, gameEngine.width, false);
    //towers.add(new Tower(bob, 500, 500));
    //towers.add(new Tower(bob, 800, 500));
    places.add(new Place(sand, 200, 500));
    places.add(new Place(sand, 200, 700));


  }

  Bitmap      bob;
  Bitmap      sand;
  Bitmap      background;
  List<Tower> towers            = new ArrayList<>();
  List<Place> places            = new ArrayList<>();
  List<Tower> towersToBeRemoved = new ArrayList<>();

  @Override
  public
  void update(float deltaTime) {
    staticDraw();
    checkTowers();

    if (gameEngine.isTouchDown(0)) {
      int x = gameEngine.getTouchX(0);
      int y = gameEngine.getTouchY(0);
      if (x >= 0 && x <= 128 && y >= 0 && y <= 128) {
        towers.add(new Tower(bob, 0, 0));
      }
    }

    for (Tower t : towers) {
      System.out.printf("pos: %s, %s, %s \n",t.x, t.y,t.placed);
      System.out.println(towers.size());
    }

    removeUnSetTowers();
  }

  void staticDraw() {
    gameEngine.clearFrameBuffer(Color.GREEN);
    gameEngine.drawBitmap(bob, 0, 0);
    for (Place p : places) {
      gameEngine.drawBitmap(p.skin, p.x, p.y);
    }
    for (Tower t : towers) {
      gameEngine.drawBitmap(t.skin, t.x, t.y);
    }
  }

  void checkTowers() {
    if (gameEngine.isTouchDown(0)) {
      for (Tower t : towers) {
        if (t.isTouched()) {
          t.x = gameEngine.getTouchX(0) - t.width / 2;
          t.y = gameEngine.getTouchY(0) - t.height / 2;
        }
      }
    } else {
      for (Tower t : towers) {
        if (t.isTouched()) {
          for (Place p : places) {
            if (p.towerAbove(t)) {
              p.towerSnap(t);
            }
          }
        }
      }
    }
  }

  void removeUnSetTowers(){
    if (!gameEngine.isTouchDown(0)) {
      for (Tower t : towers) {
        if (!t.placed) {
          System.out.println("no tower about");
          towersToBeRemoved.add(t);
        }
      }
      for (Tower t : towersToBeRemoved) {
        towers.remove(t);
      }
    }
  }

  @Override
  public
  void pause() {

  }

  @Override
  public
  void resume() {

  }

  @Override
  public
  void dispose() {

  }
}
