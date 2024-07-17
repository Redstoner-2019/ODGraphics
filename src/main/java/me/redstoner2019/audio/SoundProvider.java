package me.redstoner2019.audio;

import org.lwjgl.openal.*;

import java.util.HashMap;
import java.util.Set;

import static org.lwjgl.openal.ALC10.*;

public class SoundProvider {

    private long audioContext;
    private long audioDevice;
    private static SoundProvider INSTANCE;
    private float volume = .5f;
    private HashMap<String, Sound> sounds = new HashMap<>();

    private SoundProvider(){
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice,attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if(!alcCapabilities.OpenALC10) {
            assert false : "Audio library not supported.";
        }
    }

    public void loadSound(String soundPath){
        Sound s = new Sound(soundPath,false);
        sounds.put(soundPath,s);
    }

    public Sound getSound(String soundPath){
        return sounds.get(soundPath);
    }

    public Set<String> getSounds(){
        return sounds.keySet();
    }

    public long getAudioDevice() {
        return audioDevice;
    }

    public long getAudioContext() {
        return audioContext;
    }

    public void destroy(){
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);
    }

    public static SoundProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SoundProvider();
        }
        return INSTANCE;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
