package me.redstoner2019.threed.model;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Mesh {
    private int vao, vbo, nbo, ibo;
    private int vertexCount;

    public Mesh(float[] positions, float[] normals, int[] indices) {
        vertexCount = indices.length;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Positions
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // Normals
        nbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, nbo);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        // Indices
        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    public static Mesh createCube() {
        float[] positions = {
                -0.5f, -0.5f, -0.5f,   // 0
                0.5f, -0.5f, -0.5f,   // 1
                0.5f,  0.5f, -0.5f,   // 2
                -0.5f,  0.5f, -0.5f,   // 3
                -0.5f, -0.5f,  0.5f,   // 4
                0.5f, -0.5f,  0.5f,   // 5
                0.5f,  0.5f,  0.5f,   // 6
                -0.5f,  0.5f,  0.5f    // 7
        };

        float[] normals = {
                // dummy flat normals for now (later improve)
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
                0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,  1,
        };

        int[] indices = {
                0, 1, 2, 2, 3, 0, // back
                4, 5, 6, 6, 7, 4, // front
                0, 4, 7, 7, 3, 0, // left
                1, 5, 6, 6, 2, 1, // right
                3, 2, 6, 6, 7, 3, // top
                0, 1, 5, 5, 4, 0  // bottom
        };

        System.out.println("Positions: " + positions.length);
        System.out.println("Normals: " + normals.length);
        System.out.println("Indices: " + indices.length);


        return new Mesh(positions, normals, indices);
    }


    public void render() {
        glBindVertexArray(vao);
        //System.out.println("Rendering mesh with " + vertexCount + " vertices.");
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
