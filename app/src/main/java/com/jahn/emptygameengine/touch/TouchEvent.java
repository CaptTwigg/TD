package com.jahn.emptygameengine.touch;

/**
 * Class with responsibilities a touch needs
 */
public
class TouchEvent {
  public TouchEventType type; //The type of the event
  public int            x; //The x-coordinate of the event
  public int            y; //The y-coordinate of the event
  public int            pointer; //The pointer id (from the Android system)

  /***
   * Enum of touch event types
   */
  public
  enum TouchEventType {
    DOWN,
    UP,
    DRAGGED
  }
}
