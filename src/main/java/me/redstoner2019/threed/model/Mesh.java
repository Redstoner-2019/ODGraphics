package me.redstoner2019.threed.model;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Mesh {
    private int vao, vbo, nbo, tbo, ibo;
    private int vertexCount;
    
    // Store the original data for potential modifications
    private float[] positions;
    private float[] normals;
    private float[] texCoords;
    private int[] indices;

    public Mesh(float[] positions, float[] normals, int[] indices) {
        this(positions, normals, null, indices);
    }

    public Mesh(float[] positions, float[] normals, float[] texCoords, int[] indices) {
        // Store copies of the data
        this.positions = positions.clone();
        this.normals = normals.clone();
        this.indices = indices.clone();
        
        // If no texture coordinates provided, create default ones (0,0 for each vertex)
        if (texCoords == null) {
            int vertexCount = positions.length / 3;
            this.texCoords = new float[vertexCount * 2];
            // All texture coordinates default to (0,0)
            for (int i = 0; i < this.texCoords.length; i++) {
                this.texCoords[i] = 0.0f;
            }
            System.out.println("Generated default texture coordinates for " + vertexCount + " vertices");
        } else {
            this.texCoords = texCoords.clone();
        }
        
        vertexCount = indices.length;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Positions (location = 0)
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        // Normals (location = 1)
        nbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, nbo);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        // Texture coordinates (location = 2) - ALWAYS create this buffer
        tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, this.texCoords, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);

        // Indices
        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // Getters for mesh data (needed for normalization)
    public float[] getPositions() {
        return positions != null ? positions.clone() : null;
    }

    public float[] getNormals() {
        return normals != null ? normals.clone() : null;
    }

    public float[] getTexCoords() {
        return texCoords != null ? texCoords.clone() : null;
    }

    public int[] getIndices() {
        return indices != null ? indices.clone() : null;
    }
}