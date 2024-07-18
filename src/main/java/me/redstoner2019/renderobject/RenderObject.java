package me.redstoner2019.renderobject;

import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.render.Renderer;

public class RenderObject {
    private float x0;
    private float y0;
    private float x1;
    private float y1;
    private RenderObjectRenderer renderer;

    public RenderObject(float x0, float y0, float x1, float y1, RenderObjectRenderer renderer) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.renderer = renderer;
    }

    public void render(Renderer renderer, TextRenderer textRenderer){
        this.renderer.render(renderer, textRenderer, this);
    }

    public boolean isOnObject(float x, float y){
        return x <= x1 && x>= x0 && y <= y1 && y >= y0;
    }

    public float getX0() {
        return x0;
    }

    public void setX0(float x0) {
        this.x0 = x0;
    }

    public float getY0() {
        return y0;
    }

    public void setY0(float y0) {
        this.y0 = y0;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public RenderObjectRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(RenderObjectRenderer renderer) {
        this.renderer = renderer;
    }
}
