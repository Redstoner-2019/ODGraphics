package me.redstoner2019.threed.model;

import me.redstoner2019.graphics.shader.ShaderProgram;
import me.redstoner2019.graphics.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Model {
    private final Mesh mesh;
    private final Texture texture;
    private Vector3f color;

    private Vector3f position;
    private Vector3f positionOffset = new Vector3f(0, 0, 0);
    private Vector3f rotation; // in degrees
    private Vector3f scale;

    private HashMap<String, Model> children = new HashMap<>();

    public Model(Mesh mesh, Texture texture, Vector3f color) {
        this.mesh = mesh;
        this.texture = null;
        this.color = color;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
        if(this.color == null) this.color = new Vector3f(1, 1, 1);
    }

    public Model(Mesh mesh, Texture texture) {
        this.mesh = mesh;
        this.texture = texture;
        this.color = new Vector3f(1, 1, 1);
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Model(Mesh mesh, Vector3f color) {
        this.mesh = mesh;
        this.texture = null;
        this.color = color;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Matrix4f getModelMatrix(){
        return new Matrix4f()
                .translate(position.x + positionOffset.x, position.y + positionOffset.y, position.z + positionOffset.z)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale.x, scale.y, scale.z);
    }

    public void render(ShaderProgram shader) {
        Matrix4f modelMatrix = getModelMatrix();

        shader.setUniform4fv("model", modelMatrix);

        if (texture != null) {
            shader.setUniform1i("useTexture", 1);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        } else {
            shader.setUniform1i("useTexture", 0);
            shader.setUniform3f("objectColor", color);
        }

        mesh.render();

        for(Model child : children.values()){
            child.setScale(scale);
            child.setPosition(position);
            child.setRotation(rotation);
            child.setPositionOffset(positionOffset);
            child.render(shader);
        }
    }

    public void rotate(float yawDegrees, float pitchDegrees) {
        float yaw = (float) Math.toRadians(yawDegrees);
        float pitch = (float) Math.toRadians(pitchDegrees);

        // Add to existing rotation
        rotation.y += yaw;
        rotation.x += pitch;

        // Optional: clamp pitch to avoid flipping (e.g., for camera-like behavior)
        rotation.x = Math.max(- (float)Math.PI / 2f, Math.min((float)Math.PI / 2f, rotation.x));
    }

    public void addChild(String name, Model model) {
        children.put(name, model);
    }

    public Model getChild(String name) {
        return children.get(name);
    }

    public List<Model> getChildren() {
        return new ArrayList<>(children.values());
    }

    // Getters and setters

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector3f getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(Vector3f positionOffset) {
        this.positionOffset = positionOffset;
    }

    public void setChildren(HashMap<String, Model> children) {
        this.children = children;
    }

    public void print(){
        System.out.println(color.x + " " + color.y + " " + color.z + " " + children.keySet());
        for(Model m : children.values()){
            m.print();
        }
    }
}
