package me.redstoner2019.graphics.general;

import org.lwjgl.opengl.*;

import java.awt.*;
import java.util.Random;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private static Renderer INSTANCE = null;

    private int width;
    private int height;

    public int vao;
    private final ShaderProgram renderShader;
    private ShaderProgram[] postProcessingShaders;


    private Renderer(){
        vao = createVertexArray();

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/default.vert");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/default.frag");

        renderShader = new ShaderProgram();
        renderShader.attachShader(vertexShader);
        renderShader.attachShader(fragmentShader);
        renderShader.link();
    }

    public static Renderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Renderer();
        }
        return INSTANCE;
    }

    public float getAspectRatio(){
        return (float) width / height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ShaderProgram[] getPostProcessingShaders() {
        return postProcessingShaders;
    }

    public void setPostProcessingShaders(ShaderProgram...postProcessingShaders) {
        this.postProcessingShaders = postProcessingShaders;
    }

    public void renderTexture(float x, float y, float w, float h, float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, Color color){
        if(texture == null) {
            System.err.println("Texture is null.");
            throw new RuntimeException(new NullPointerException("Texture is null."));
        }
        renderTexture(x,y,w,h,sectionX,sectionY,sectionW,sectionH,texture.getId(),color);
    }

    public void renderTexture(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, int texture, Color color){
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glEnable(GL_BLEND);

        glUseProgram(renderShader.id);

        int textureUniformLocation = GL20.glGetUniformLocation(renderShader.id, "textureSampler");
        GL20.glUniform1i(textureUniformLocation, 0);

        int texOffsetLocation = GL20.glGetUniformLocation(renderShader.id, "texOffset");
        GL20.glUniform2f(texOffsetLocation, sectionX, sectionY);

        int texScaleLocation = GL20.glGetUniformLocation(renderShader.id, "texScale");
        GL20.glUniform2f(texScaleLocation, sectionW, sectionH);

        int offsetLocation = GL20.glGetUniformLocation(renderShader.id, "offset");
        GL20.glUniform2f(offsetLocation, x + (w/2), y + (h/2));

        int offsetScaleLocation = GL20.glGetUniformLocation(renderShader.id, "offsetScale");
        GL20.glUniform2f(offsetScaleLocation, w, h);

        glBindVertexArray(vao);
        glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        if(postProcessingShaders != null){
            for(ShaderProgram post : postProcessingShaders){
                glUseProgram(post.id);

                textureUniformLocation = GL20.glGetUniformLocation(post.id, "textureSampler");
                GL20.glUniform1i(textureUniformLocation, 0);

                texOffsetLocation = GL20.glGetUniformLocation(post.id, "texOffset");
                GL20.glUniform2f(texOffsetLocation, sectionX, sectionY);

                texScaleLocation = GL20.glGetUniformLocation(post.id, "texScale");
                GL20.glUniform2f(texScaleLocation, sectionW, sectionH);

                offsetLocation = GL20.glGetUniformLocation(post.id, "offset");
                GL20.glUniform2f(offsetLocation, x + (w/2), y + (h/2));

                offsetScaleLocation = GL20.glGetUniformLocation(post.id, "offsetScale");
                GL20.glUniform2f(offsetScaleLocation, w, h);

                System.out.println(post.id);

                glBindVertexArray(vao);
                glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
                glBindVertexArray(0);
            }
        }

        /*glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);*/

        GL20.glUseProgram(0);
    }

    /**
     * Screenspace Rendering
     */

    public void renderTexture(float x, float y, float w, float h, Texture texture){
        renderTexture(x,y,w,h,0,0,1,1, texture, Color.WHITE);
    }
    public void renderTexture(float x, float y, float w, float h, Texture texture, Color c){
        renderTexture(x,y,w,h,0,0,1,1, texture, c);
    }

    public void renderTexture(float x, float y, float w, float h, int texture){
        renderTexture(x,y,w,h,0,0,1,1, texture, Color.WHITE);
    }
    public void renderTexture(float x, float y, float w, float h, int texture, Color c){
        renderTexture(x,y,w,h,0,0,1,1, texture, c);
    }

    public void renderTextureBounds(float x0, float y0, float x1, float y1, Texture texture){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture, Color.WHITE);
    }
    public void renderTextureBounds(float x0, float y0, float x1, float y1, Texture texture, Color c){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture, c);
    }

    public void renderTextureBounds(float x0, float y0, float x1, float y1, int texture){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture, Color.WHITE);
    }
    public void renderTextureBounds(float x0, float y0, float x1, float y1, int texture, Color c){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture, c);
    }

    /**
     * Coordinate Rendering
     */

    public void renderTextureCoordinates(float x, float y, float w, float h, Texture texture){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture, Color.WHITE);
    }

    public void renderTextureCoordinates(float x, float y, float w, float h, Texture texture, Color c){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture, c);
    }

    public void renderTextureCoordinates(float x, float y, float w, float h, int texture){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture, Color.WHITE);
    }

    public void renderTextureCoordinates(float x, float y, float w, float h, int texture, Color c){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture, c);
    }

    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, Texture texture){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture);
    }
    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, Texture texture, Color c){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture, c);
    }

    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, int texture){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture);
    }
    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, int texture, Color c){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture, c);
    }

    private float toNegativeRange(float f){
        return (f * 2) - 1;
    }

    private int createVertexArray() {
        float[] vertices = {
                -0.5f,  0.5f, 0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, 0.0f,  0.0f, 0.0f,
                0.5f, -0.5f, 0.0f,  1.0f, 0.0f,
                0.5f,  0.5f, 0.0f,  1.0f, 1.0f
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        int ebo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);

        return vao;
    }
}
