package me.redstoner2019.graphics.font;

import me.redstoner2019.graphics.render.Renderer;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;

public class TextRenderer {
    private static TextRenderer INSTANCE;
    private Map<Float, FontData> fontDataMap = new HashMap<>();
    private final String fontPath = "fonts/font.ttf";
    private final int BITMAP_W = 2048;
    private final int BITMAP_H = 2048;

    private TextRenderer() {}

    public static TextRenderer getInstance() {
        if (INSTANCE == null) INSTANCE = new TextRenderer();
        return INSTANCE;
    }

    public void renderText(String text, float x, float y, float fontSize, Color color, float screenWidth, float screenHeight) {
        y+=fontSize;
        FontData fontData = fontDataMap.get(fontSize);
        if (fontData == null) {
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontData.textureID);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] xPos = {x};
            float[] yPos = {y};

            glBegin(GL_QUADS);
            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
                stbtt_GetBakedQuad(fontData.charData, BITMAP_W, BITMAP_H, c - 32, xPos, yPos, quad, true);

                glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
                glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
            }
            glEnd();
        }

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();

        glPopAttrib();
    }

    public void loadFontTexture(String fontPath, float fontSize) {
        ByteBuffer ttfBuffer;
        try {
            ttfBuffer = ioResourceToByteBuffer(fontPath, 16000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font", e);
        }

        ByteBuffer bitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H);
        STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(96);
        stbtt_BakeFontBitmap(ttfBuffer, fontSize, bitmap, BITMAP_W, BITMAP_H, 32, charData);

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        MemoryUtil.memFree(bitmap);
        fontDataMap.put(fontSize, new FontData(textureID, charData));
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        try (InputStream source = ClassLoader.getSystemResourceAsStream(resource)) {
            if (source == null) throw new IOException("Resource not found: " + resource);
            ByteBuffer buffer = MemoryUtil.memAlloc(bufferSize);
            ReadableByteChannel rbc = Channels.newChannel(source);
            while (true) {
                int bytes = rbc.read(buffer);
                if (bytes == -1) break;
                if (buffer.remaining() == 0)
                    buffer = resizeBuffer(buffer, buffer.capacity() * 2);
            }
            buffer.flip();
            return buffer;
        }
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = MemoryUtil.memAlloc(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        MemoryUtil.memFree(buffer);
        return newBuffer;
    }

    private static class FontData {
        int textureID;
        STBTTBakedChar.Buffer charData;

        FontData(int textureID, STBTTBakedChar.Buffer charData) {
            this.textureID = textureID;
            this.charData = charData;
        }
    }
}