package me.redstoner2019.graphics.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;

public class ShaderProgram {

    public final int id;

    public ShaderProgram() {
        id = glCreateProgram();
    }

    public void attachShader(Shader shader) {
        glAttachShader(id, shader.getID());
    }

    public void bindFragmentDataLocation(int number, CharSequence name) {
        glBindFragDataLocation(id, number, name);
    }

    public void link() {
        glLinkProgram(id);

        checkStatus();
    }

    public int getAttributeLocation(CharSequence name) {
        return glGetAttribLocation(id, name);
    }

    public void enableVertexAttribute(int location) {
        glEnableVertexAttribArray(location);
    }

    public void disableVertexAttribute(int location) {
        glDisableVertexAttribArray(location);
    }

    public void pointVertexAttribute(int location, int size, int stride, int offset) {
        glVertexAttribPointer(location, size, GL_FLOAT, false, stride, offset);
    }

    public int getUniformLocation(CharSequence name) {
        return glGetUniformLocation(id, name);
    }

    public void setUniform1f(CharSequence name, float f){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform1f(location, f);
        glUseProgram(0);
    }

    public void setUniform1i(CharSequence name, int f){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform1i(location, f);
        glUseProgram(0);
    }

    public void setUniform2f(CharSequence name, float f1, float f2){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform2f(location, f1, f2);
        glUseProgram(0);
    }

    public void setUniform2i(CharSequence name, int f1, int f2){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform2i(location, f1, f2);
        glUseProgram(0);
    }

    public void setUniform3f(CharSequence name, float f1, float f2, float f3){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform3f(location, f1, f2, f3);
        glUseProgram(0);
    }

    public void setUniform3i(CharSequence name, int f1, int f2, int f3){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform3i(location, f1, f2, f3);
        glUseProgram(0);
    }

    public void setUniform4f(CharSequence name, float f1, float f2, float f3, float f4){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform4f(location, f1, f2, f3, f4);
        glUseProgram(0);
    }

    public void setUniform3f(CharSequence name, Vector3f vector3f){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform3f(location, vector3f.x, vector3f.y, vector3f.z);
        glUseProgram(0);
    }

    public void setUniform4i(CharSequence name, int f1, int f2, int f3, int f4){
        glUseProgram(id);
        int location = glGetUniformLocation(id, name);
        glUniform4i(location, f1, f2, f3, f4);
        glUseProgram(0);
    }

    public void setUniform4fv(CharSequence name, Matrix4f matrix) {
        int location = glGetUniformLocation(id, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {   // fast, no GC
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);            // writes 16 floats, advances pos to 16
            glUniformMatrix4fv(location, false, fb); // fb’s position is 0 ✓
        }
    }


    public void use() {
        glUseProgram(id);
    }

    public void checkStatus() {
        int status = glGetProgrami(id, GL_LINK_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(id));
        }
    }

    public void delete() {
        glDeleteProgram(id);
    }

    public void bind(){
        glUseProgram(id);
    }

    public void unbind(){
        glUseProgram(0);
    }
}