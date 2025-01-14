package me.redstoner2019.graphics.mesh;

import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private List<Vertex2D> vertices = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    private float scale = 1f;
    private float offsetX = 0;
    private float offsetY = 0;
    private float scalingX = 1;
    private float[] bv = new float[16];
    private int[] bi = new int[16];

    public Mesh() {

        /*for (int i = 0; i < 1000; i++) {
            addTriangle(new Triangle(new Vertex2D(0, 0), new Vertex2D(1, 0), new Vertex2D(0, 1)));
        }*/
    }

    public void setScalingX(float scalingX) {
        this.scalingX = scalingX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public void addVertex(Vertex2D v){
        if(vertices.contains(v)) return;
        vertices.add(v);
    }

    public void addIndex(int index){
        indices.add(index);
    }

    public int getIndexOf(Vertex2D v){
        return vertices.indexOf(v);
    }

    public void addRectangle(Rectangle r){
        addTriangle(r.getT0());
        addTriangle(r.getT1());
    }

    public void addTriangle(Triangle t){
        addVertex(t.getV0());
        addVertex(t.getV1());
        addVertex(t.getV2());

        int i0 = getIndexOf(t.getV0());
        int i1 = getIndexOf(t.getV1());
        int i2 = getIndexOf(t.getV2());

        addIndex(i0);
        addIndex(i1);
        addIndex(i2);
    }

    public void bake(){
        bv = new float[vertices.size() * 5];
        int index = 0;
        //offsetX = 0;
        //offsetY = 0;
        for(Vertex2D v2 : vertices){
            bv[index] = (v2.getX() + offsetX) * scale;
            bv[index + 1] = ((v2.getY() + offsetY) * scale);
            bv[index + 2] = 0;  //z
            bv[index + 3] = v2.getX()*scalingX * 0.5f;  //tex X
            bv[index + 4] = v2.getY() * 0.5f;  //tex y
            index += 5;
        }

        bi = new int[indices.size()];
        for (int j = 0; j < indices.size(); j++) {
            bi[j] = indices.get(j);
        }
    }

    public float[] getVertices() {
        return bv;
    }

    public int[] getIndices() {
        return bi;
    }
}
