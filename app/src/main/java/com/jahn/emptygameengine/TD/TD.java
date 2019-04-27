package com.jahn.emptygameengine.TD;

import com.jahn.emptygameengine.core.GameEngine;
import com.jahn.emptygameengine.core.Screen;

public
class TD  extends GameEngine {
  @Override
  public
  Screen createStartScreen() {
    return new MainScreen(this);
  }
}
