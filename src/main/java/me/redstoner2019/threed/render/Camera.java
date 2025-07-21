package me.redstoner2019.threed.render;

import org.joml.*;

import java.lang.Math;

public class Camera {
    public Vector3f position;
    public float yaw;   // left/right (horizontal rotation)
    public float pitch; // up/down (vertical rotation)
    private static Camera INSTANCE = null;

    private Camera(Vector3f position, float yaw, float pitch) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static Camera getInstance(){
        if(INSTANCE == null) INSTANCE = new Camera(new Vector3f(0, 1, 3), 180, 0);
        return INSTANCE;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Matrix4f getViewMatrix() {
        Vector3f direction = getDirection();
        Vector3f target = new Vector3f(position).add(direction);
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    public Vector3f getDirection() {
        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        return direction.normalize();
    }

    public void move(Vector3f offset) {
        position.add(offset);
    }
}

