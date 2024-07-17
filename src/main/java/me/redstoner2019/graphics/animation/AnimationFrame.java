package me.redstoner2019.graphics.animation;

import me.redstoner2019.graphics.texture.Texture;

import java.awt.*;

public class AnimationFrame {
    private Texture texture;
    private Color color;
    private float x;
    private float y;
    private float w;
    private float h;

    public AnimationFrame(Texture texture, Color color) {
        this.texture = texture;
        this.color = color;
        this.x = -1;
        this.y = -1;
        this.w = 2;
        this.h = 2;
    }

    public AnimationFrame(Texture texture, Color color, float x, float y, float w, float h) {
        this.texture = texture;
        this.color = color;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }
}
