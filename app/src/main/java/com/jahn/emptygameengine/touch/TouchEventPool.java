package com.jahn.emptygameengine.touch;

import com.jahn.emptygameengine.core.Pool;


public class TouchEventPool extends Pool<TouchEvent>
{
    @Override
    protected TouchEvent newItem()
    {
        return new TouchEvent();
    }
}
