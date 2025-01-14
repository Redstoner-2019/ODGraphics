package me.redstoner2019.graphics.animation;

import me.redstoner2019.graphics.render.Renderer;
import me.redstoner2019.graphics.shader.PostProcessingShader;
import me.redstoner2019.graphics.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

import java.util.Arrays;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;

public class Animation {
    private AnimationFrame[] animationFrames;
    private long start;
    private int frameDelay = 16;
    private boolean running = false;
    private boolean reverse = false;
    private boolean repeating = false;
    private boolean interpolated = true;
    private static boolean initiated = false;
    private static PostProcessingShader interpolate;

    public static void init() {
        interpolate = new PostProcessingShader("shader/post/interpolate.frag", false);
        initiated = true;
    }

    public Animation(AnimationFrame...animationFrames) {
        this.animationFrames = animationFrames;
    }

    public Animation(int frameDelay, AnimationFrame...animationFrames) {
        this.animationFrames = animationFrames;
        this.frameDelay = frameDelay;
    }

    public Animation(AnimationFrame[] animationFrames, int frameDelay) {
        this.animationFrames = animationFrames;
        this.frameDelay = frameDelay;
    }

    public void play(){
        if(running) return;
        running = true;
        start = System.currentTimeMillis();
    }

    public void render(Renderer renderer){
        if(repeating && start + (long) frameDelay * animationFrames.length - frameDelay <= System.currentTimeMillis()) {
            start = System.currentTimeMillis();
        }

        if(!running) return;

        long timeSinceStart = System.currentTimeMillis() - start;
        int currentFrame = (int) (timeSinceStart / frameDelay);

        if(reverse) {
            currentFrame = animationFrames.length - currentFrame - 1;
        }

        if(currentFrame < 0 || currentFrame >= animationFrames.length) {
            running = false;
            return;
        }

        AnimationFrame animationFrame = animationFrames[currentFrame];

        float x = animationFrame.getX();
        float y = animationFrame.getY();
        float w = animationFrame.getW();
        float h = animationFrame.getH();

        if(interpolated){
            float dt = (float) (timeSinceStart % frameDelay) / frameDelay;

            int nFrameI = currentFrame + 1;
            if(reverse) nFrameI = currentFrame - 1;

            if(nFrameI < 0) nFrameI = 0;
            if(nFrameI >= animationFrames.length) nFrameI = animationFrames.length - 1;

            AnimationFrame nFrame = animationFrames[nFrameI];

            float x0 = nFrame.getX();
            float y0 = nFrame.getY();
            float w0 = nFrame.getW();
            float h0 = nFrame.getH();

            x+=(x0-x) * dt;
            y+=(y0-y) * dt;
            w+=(w0-w) * dt;
            h+=(h0-h) * dt;

            PostProcessingShader[] preShaders = Arrays.copyOf(renderer.getPostProcessingShaders(),renderer.getPostProcessingShaders().length);

            PostProcessingShader[] shaders = new PostProcessingShader[preShaders.length + 1];

            shaders[0] = interpolate;
            System.arraycopy(preShaders, 0, shaders, 1, preShaders.length);

            GL33.glActiveTexture(GL_TEXTURE0);
            GL33.glBindTexture(GL33.GL_TEXTURE_2D,animationFrame.getTexture().getId());
            GL33.glActiveTexture(GL_TEXTURE1);
            GL33.glBindTexture(GL33.GL_TEXTURE_2D,nFrame.getTexture().getId());

            interpolate.setUniform1f("dt",dt);
            interpolate.setUniform1i("image1",0);
            interpolate.setUniform1i("image2",1);

            renderer.setPostProcessingShaders(shaders);

            renderer.renderTexture(x,y,w,h,animationFrame.getTexture(),animationFrame.getColor());

            renderer.setPostProcessingShaders();
        } else {
            renderer.renderTexture(x,y,w,h,animationFrame.getTexture(),animationFrame.getColor());
        }

        if(start + (long) frameDelay * animationFrames.length - frameDelay <= System.currentTimeMillis()) {
            running = false;
        }
    }

    public AnimationFrame[] getAnimationFrames() {
        return animationFrames;
    }

    public void setAnimationFrames(AnimationFrame[] animationFrames) {
        this.animationFrames = animationFrames;
    }

    public int getFrameDelay() {
        return frameDelay;
    }

    public void setFrameDelay(int frameDelay) {
        this.frameDelay = frameDelay;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }
    public void stop(){
        running = false;
    }
}
