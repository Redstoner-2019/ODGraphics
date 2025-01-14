package me.redstoner2019.graphics.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class PostProcessor {

    private int quadVAO;
    private int quadVBO;

    public PostProcessor() {
        setupQuad();
    }

    // Call this method to render a shader as a post-process effect
    public void renderPostProcess(int shaderId, int textureId) {
        // Bind the shader
        GL20.glUseProgram(shaderId);

        // Bind the texture to texture unit 0
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Pass the texture unit as a uniform to the shader
        int textureLocation = GL20.glGetUniformLocation(shaderId, "screenTexture");
        GL20.glUniform1i(textureLocation, 0);

        // Bind and draw the full-screen quad
        GL30.glBindVertexArray(quadVAO);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);

        // Unbind the texture and shader
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL20.glUseProgram(0);
    }

    private void setupQuad() {
        float[] quadVertices = {
                // Positions   // Texture Coords
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };

        quadVAO = GL30.glGenVertexArrays();
        quadVBO = GL30.glGenBuffers();

        GL30.glBindVertexArray(quadVAO);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, quadVBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, quadVertices, GL30.GL_STATIC_DRAW);

        // Position attribute
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Texture coordinate attribute
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    // Clean up resources
    public void cleanup() {
        GL30.glDeleteVertexArrays(quadVAO);
        GL30.glDeleteBuffers(quadVBO);
    }
}
