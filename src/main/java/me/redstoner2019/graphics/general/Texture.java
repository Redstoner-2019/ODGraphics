package me.redstoner2019.graphics.general;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static me.redstoner2019.util.Util.createBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture {

    private final int id;

    private int width;

    private int height;

    public Texture() {
        id = glGenTextures();
    }
    public Texture(int id) {
        this.id = id;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void setParameter(int name, int value) {
        glTexParameteri(GL_TEXTURE_2D, name, value);
    }

    public void uploadData(int width, int height, ByteBuffer data) {
        uploadData(GL_RGBA8, width, height, GL_RGBA, data);
    }

    public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data) {
        this.width = width;
        this.height = height;
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data);
    }

    public void delete() {
        glDeleteTextures(id);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width > 0) {
            this.width = width;
        }
    }
    public float getAspectRatio(){
        return (float) (getWidth() / getHeight());
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height > 0) {
            this.height = height;
        }
    }

    public static Texture createTexture(int width, int height, ByteBuffer data) {
        Texture texture = new Texture();
        texture.setWidth(width);
        texture.setHeight(height);

        texture.bind();

        texture.setParameter(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        texture.setParameter(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        texture.uploadData(GL_RGBA8, width, height, GL_RGBA, data);

        return texture;
    }

    public int getId() {
        return id;
    }

    public static Texture loadTexture(String filePath) {
        int textureID;
        Texture t;
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = STBImage.stbi_load(filePath, width, height, channels, 4);
            if (image == null) {
                System.out.println("Failed to load texture file: " + filePath);
                return null;
            }

            System.out.println("Loaded texture: " + filePath + " (Width: " + width.get(0) + ", Height: " + height.get(0) + ")");

            textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(), height.get(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
            GL30.glGenerateMipmap(GL_TEXTURE_2D);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            STBImage.stbi_image_free(image);

            t = new Texture(textureID);

            t.setWidth(width.get(0));
            t.setHeight(height.get(0));
        }

        return t;
    }

    public static Texture loadTextureFromResource(String resourcePath) {
        return loadTexture(createBuffer(resourcePath));
    }

    public static ByteBuffer getTextureData(int textureID, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        return buffer;
    }

    public static Texture loadTexture(ByteBuffer imageBuffer) {

        int textureID;

        Texture t;

        try (MemoryStack stack = stackPush()) {

            IntBuffer width = stack.mallocInt(1);

            IntBuffer height = stack.mallocInt(1);

            IntBuffer channels = stack.mallocInt(1);


            STBImage.stbi_set_flip_vertically_on_load(true);

            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);

            if (image == null) {

                System.out.println("Failed to load texture from memory");

                return null;

            }

            textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(), height.get(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
            GL30.glGenerateMipmap(GL_TEXTURE_2D);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            STBImage.stbi_image_free(image);

            t = new Texture(textureID);

            t.setWidth(width.get(0));
            t.setHeight(height.get(0));
        }

        return t;
    }
}