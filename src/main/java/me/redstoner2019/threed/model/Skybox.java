package me.redstoner2019.threed.model;

import me.redstoner2019.graphics.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;

import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

import me.redstoner2019.graphics.shader.Shader;
import me.redstoner2019.graphics.math.Vector3f;

public class Skybox {
    private int vao, vbo, cubemapTexture;
    private ShaderProgram shader;

    private static final float[] SKYBOX_VERTICES = {
            -1,  1, -1,  -1, -1, -1,   1, -1, -1,
            1, -1, -1,   1,  1, -1,  -1,  1, -1,
            -1, -1,  1,  -1, -1, -1,  -1,  1, -1,
            -1,  1, -1,  -1,  1,  1,  -1, -1,  1,
            1, -1, -1,   1, -1,  1,   1,  1,  1,
            1,  1,  1,   1,  1, -1,   1, -1, -1,
            -1, -1,  1,  -1,  1,  1,   1,  1,  1,
            1,  1,  1,   1, -1,  1,  -1, -1,  1,
            -1,  1, -1,   1,  1, -1,   1,  1,  1,
            1,  1,  1,  -1,  1,  1,  -1,  1, -1,
            -1, -1, -1,  -1, -1,  1,   1, -1, -1,
            1, -1, -1,  -1, -1,  1,   1, -1,  1
    };

    public Skybox(List<String> faces, ShaderProgram skyboxShader) {
        shader = skyboxShader;
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, SKYBOX_VERTICES, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);

        cubemapTexture = loadCubemap(faces);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        glDepthFunc(GL_LEQUAL);
        shader.bind();

        Matrix4f skyView = new Matrix4f(view);
        skyView.m30(0).m31(0).m32(0); // remove translation
        shader.setUniform4fv("view", skyView);
        shader.setUniform4fv("projection", projection);

        glBindVertexArray(vao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
        shader.unbind();
        glDepthFunc(GL_LESS);
    }

    private int loadCubemap(List<String> faces) {
        int texID = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, texID);

        for (int i = 0; i < faces.size(); i++) {
            int[] width = new int[1], height = new int[1], channels = new int[1];
            stbi_set_flip_vertically_on_load(false);
            FloatBuffer image = stbi_loadf(faces.get(i), width, height, channels, 4);
            if (image != null) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_FLOAT, image);
                stbi_image_free(image);
            } else {
                System.err.println("Failed to load skybox face: " + faces.get(i));
            }
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        return texID;
    }
}
