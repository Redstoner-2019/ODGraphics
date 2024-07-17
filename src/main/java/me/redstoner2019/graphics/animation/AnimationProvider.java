package me.redstoner2019.graphics.animation;

import java.util.HashMap;

public class AnimationProvider {
    private static AnimationProvider INSTANCE;
    private HashMap<String,Animation> animationHashMap = new HashMap<>();
    private AnimationProvider(){

    }

    public Animation getAnimation(String animation){
        return animationHashMap.get(animation);
    }

    public void setAnimation(String name, Animation animation){
        animationHashMap.put(name,animation);
    }

    public static AnimationProvider getInstance(){
        if(INSTANCE == null) INSTANCE = new AnimationProvider();
        return INSTANCE;
    }
}
