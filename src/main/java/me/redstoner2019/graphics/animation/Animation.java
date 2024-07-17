package me.redstoner2019.graphics.animation;

import me.redstoner2019.graphics.render.Renderer;

public class Animation {
    private AnimationFrame[] animationFrames;
    private int frame = 0;
    private int frameDelay = 16;
    private boolean running = false;
    private boolean reverse = false;
    private boolean repeating = false;

    public Animation(AnimationFrame...animationFrames) {
        this.animationFrames = animationFrames;
    }

    public Animation( int frameDelay, AnimationFrame...animationFrames) {
        this.animationFrames = animationFrames;
        this.frameDelay = frameDelay;
        this.frame = 0;
    }

    public Animation(AnimationFrame[] animationFrames, int frame, int frameDelay) {
        this.animationFrames = animationFrames;
        this.frame = frame;
        this.frameDelay = frameDelay;
    }

    public void play(){
        if(running) return;
        running = true;
        frame = 0;
        if(reverse) frame = animationFrames.length;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if(reverse){
                    while (frame > 0) {
                        try {
                            Thread.sleep(frameDelay);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        frame--;
                        if(!running) return;
                        if(frame == 0 && repeating){
                            frame = animationFrames.length;
                            if(!running) return;
                        }
                    }
                } else {
                    while (frame < animationFrames.length) {
                        try {
                            Thread.sleep(frameDelay);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if(!running) return;
                        frame++;
                        if(frame == animationFrames.length && repeating){
                            frame = 0;
                            if(!running) return;
                        }
                    }
                }
                running = false;
            }
        });
        t.start();
    }

    public void render(Renderer renderer){
        if(!running) return;
        int frame0 = frame;
        if(frame0 < animationFrames.length) {
            AnimationFrame animationFrame = animationFrames[frame0];
            renderer.renderTexture(animationFrame.getX(),animationFrame.getY(),animationFrame.getW(),animationFrame.getH(),animationFrame.getTexture(),animationFrame.getColor());
        }
    }
    public void render(Renderer renderer, float x, float y, float w, float h){
        if(!running) return;
        int frame0 = frame;
        if(frame0 < animationFrames.length) {
            AnimationFrame animationFrame = animationFrames[frame0];
            renderer.renderTexture(x,y,w,h,animationFrame.getTexture(),animationFrame.getColor());
        }
    }

    public AnimationFrame[] getAnimationFrames() {
        return animationFrames;
    }

    public void setAnimationFrames(AnimationFrame[] animationFrames) {
        this.animationFrames = animationFrames;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
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
