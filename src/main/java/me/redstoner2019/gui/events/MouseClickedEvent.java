package me.redstoner2019.gui.events;

public interface MouseClickedEvent extends Event{

    /**
     * Gets Called when the Mouse clicks on the screen
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @param screenX X-Coordinate in screenspace, -1 left, 1 right
     * @param screenY Y-Coordinate in screenspace, -1 bottom, 1 top
     * @param button Button that was clicked
     * @param type Action, PRESS or RELEASE
     */
    void onMouseClickedEvent(float x, float y, float screenX, float screenY, Button button, Type type);

    enum Type{
        PRESS,RELEASE
    }
    enum Button{
        LEFT, MIDDLE, RIGHT
    }
}
