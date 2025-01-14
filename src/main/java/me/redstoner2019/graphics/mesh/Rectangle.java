package me.redstoner2019.graphics.mesh;

public class Rectangle {
    private Triangle t0;
    private Triangle t1;

    public Rectangle(Triangle t0, Triangle t1) {
        this.t0 = t0;
        this.t1 = t1;
    }

    public Triangle getT0() {
        return t0;
    }

    public Triangle getT1() {
        return t1;
    }

    public static Rectangle ofCenter(Vertex2D center, float size) {
        float offset = size/2;
        return new Rectangle(
                new Triangle(
                        new Vertex2D(center.getX()-offset,center.getY()+offset),
                        new Vertex2D(center.getX()-offset,center.getY()-offset),
                        new Vertex2D(center.getX()+offset,center.getY()+offset)
                ),
                new Triangle(
                        new Vertex2D(center.getX()+offset,center.getY()-offset),
                        new Vertex2D(center.getX()-offset,center.getY()-offset),
                        new Vertex2D(center.getX()+offset,center.getY()+offset)
                ));
    }
}
