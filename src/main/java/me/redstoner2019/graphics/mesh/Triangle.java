package me.redstoner2019.graphics.mesh;

public class Triangle {
    private Vertex2D v0;
    private Vertex2D v1;
    private Vertex2D v2;

    public Triangle(Vertex2D v0, Vertex2D v1, Vertex2D v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    public Vertex2D getV0() {
        return v0;
    }

    public void setV0(Vertex2D v0) {
        this.v0 = v0;
    }

    public Vertex2D getV1() {
        return v1;
    }

    public void setV1(Vertex2D v1) {
        this.v1 = v1;
    }

    public Vertex2D getV2() {
        return v2;
    }

    public void setV2(Vertex2D v2) {
        this.v2 = v2;
    }
}
