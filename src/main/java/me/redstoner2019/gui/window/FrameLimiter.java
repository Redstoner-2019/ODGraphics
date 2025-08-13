package me.redstoner2019.gui.window;

import org.lwjgl.glfw.GLFW;

public class FrameLimiter {
    public enum Mode {
        VSYNC,
        LIMITED,
        UNLIMITED
    }

    private Mode mode = Mode.VSYNC;
    private int targetFps = 60;
    private long lastFrameTime = System.nanoTime();

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.VSYNC) {
            GLFW.glfwSwapInterval(1); // VSync an
        } else {
            GLFW.glfwSwapInterval(0); // VSync aus
        }
    }

    public void setTargetFps(int fps) {
        this.targetFps = fps;
    }

    public void sync() {
        if (mode == Mode.LIMITED) {
            long now = System.nanoTime();
            long frameDuration = 1_000_000_000L / targetFps;
            long sleepTime = frameDuration - (now - lastFrameTime);

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000L, (int) (sleepTime % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            lastFrameTime = System.nanoTime();
        } else if (mode == Mode.UNLIMITED) {
            Thread.onSpinWait();
            lastFrameTime = System.nanoTime();
        }
    }
}

