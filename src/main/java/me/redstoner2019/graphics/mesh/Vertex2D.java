package me.redstoner2019.graphics.mesh;

import java.util.Objects;

public class Vertex2D {
    private float x;
    private float y;

    public Vertex2D(float x, float y) {
        this.x = x;
        this.y = y;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex2D vertex2D = (Vertex2D) o;
        return Float.compare(x, vertex2D.x) == 0 && Float.compare(y, vertex2D.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
