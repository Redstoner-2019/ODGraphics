package me.redstoner2019.audio;

import me.redstoner2019.util.Util;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static java.lang.Math.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SAMPLE_OFFSET;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound {
    private int bufferId;
    private int sourceId;
    private float volume = 1f;
    private float general_volume = 0.15f;
    private String filepath;

    private boolean isPlaying = false;

    public Sound(String filepath, boolean loops) {
        this.filepath = filepath;
        if (filepath.endsWith(".ogg") || filepath.endsWith(".ogx")) {
            loadOgg(filepath, loops);
        } else if (filepath.endsWith(".wav")) {
            loadWav(filepath, loops);
        } else if (filepath.endsWith(".mp3")) {
            loadMp3(filepath, loops);
        } else {
            System.out.println("Unsupported audio format: " + filepath);
        }
    }

    private void loadOgg(String filepath, boolean loops) {
        // Allocate space to store the return information from stb
        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        ByteBuffer b = Util.createBuffer(filepath);

        ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(b, channelsBuffer, sampleRateBuffer);

        if (rawAudioBuffer == null) {
            System.out.println("Could not load sound '" + filepath + "'");
            stackPop();
            stackPop();
            return;
        }

        int channels = channelsBuffer.get();
        int sampleRate = sampleRateBuffer.get();

        stackPop();
        stackPop();

        int format = -1;
        if (channels == 1) {
            format = AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL_FORMAT_STEREO16;
        }

        bufferId = alGenBuffers();
        alBufferData(bufferId, format, rawAudioBuffer, sampleRate);

        sourceId = alGenSources();

        alSourcei(sourceId, AL_BUFFER, bufferId);
        alSourcei(sourceId, AL_LOOPING, loops ? 1 : 0);
        alSourcei(sourceId, AL_POSITION, 0);
        alSourcef(sourceId, AL_GAIN, 0.15f);  //Volume

        free(rawAudioBuffer);

        updateGain();
    }

    private void loadWav(String filepath, boolean loops) {
        try (InputStream inputStream = Sound.class.getClassLoader().getResourceAsStream(filepath)) {
            if (inputStream == null) {
                System.out.println("File not found: " + filepath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
            AudioFormat format = audioStream.getFormat();
            int bufferSize = (int) (audioStream.getFrameLength() * format.getFrameSize());
            byte[] audioBytes = new byte[bufferSize];
            audioStream.read(audioBytes);

            ByteBuffer audioBuffer = ByteBuffer.allocateDirect(audioBytes.length);
            audioBuffer.put(audioBytes);
            audioBuffer.flip();

            int alFormat = getOpenAlFormat(format.getChannels(), format.getSampleSizeInBits());

            bufferId = alGenBuffers();
            alBufferData(bufferId, alFormat, audioBuffer, (int) format.getSampleRate());

            sourceId = alGenSources();
            alSourcei(sourceId, AL_BUFFER, bufferId);
            alSourcei(sourceId, AL_LOOPING, loops ? 1 : 0);
            alSourcef(sourceId, AL_GAIN, 0.15f);  //Volume

            updateGain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMp3(String filepath, boolean loops) {
        try (InputStream inputStream = Sound.class.getClassLoader().getResourceAsStream(filepath)) {
            if (inputStream == null) {
                System.out.println("File not found: " + filepath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
            AudioFormat baseFormat = audioStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            AudioInputStream decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = decodedAudioStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            ByteBuffer audioBuffer = ByteBuffer.allocateDirect(out.size());
            audioBuffer.put(out.toByteArray());
            audioBuffer.flip();

            int alFormat = getOpenAlFormat(decodedFormat.getChannels(), decodedFormat.getSampleSizeInBits());

            bufferId = alGenBuffers();
            alBufferData(bufferId, alFormat, audioBuffer, (int) decodedFormat.getSampleRate());

            sourceId = alGenSources();
            alSourcei(sourceId, AL_BUFFER, bufferId);
            alSourcei(sourceId, AL_LOOPING, loops ? 1 : 0);
            alSourcef(sourceId, AL_GAIN, 0.15f);  //Volume

            updateGain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getOpenAlFormat(int channels, int bitsPerSample) {
        if (channels == 1) {
            return bitsPerSample == 8 ? AL_FORMAT_MONO8 : AL_FORMAT_MONO16;
        } else {
            return bitsPerSample == 8 ? AL_FORMAT_STEREO8 : AL_FORMAT_STEREO16;
        }
    }

    public void setSourcePositionAtAngle(float angle, float distance) {
        angle /= 2;
        float radians = (float) (angle * (PI / 180.0f)); // Convert degrees to radians
        float x = (float) (distance * cos(radians));
        float y = (float) (distance * sin(radians));
        float z = 0.0f; // Assuming 2D plane for simplicity

        alSource3f(sourceId, AL_POSITION, x, y, z);
    }

    public void setAngle(float angle, float distance) {
        alSource3f(sourceId, AL_VELOCITY, 0.0f, 0.0f, 0.0f);
        alSource3f(sourceId, AL_DIRECTION, 0.0f, 0.0f, 1.0f);
        setSourcePositionAtAngle(angle, distance);
    }

    public void setAngle(float angle) {
        setAngle(angle, 1);
    }

    public void delete() {
        alDeleteBuffers(sourceId);
        alDeleteBuffers(bufferId);
    }

    public void play() {
        updateGain();
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if (state == AL_STOPPED) {
            isPlaying = false;
            alSourcei(sourceId, AL_POSITION, 0);
        }

        if (!isPlaying) {
            alSourcePlay(sourceId);
            isPlaying = true;
        }
    }

    public void stop() {
        if (isPlaying) {
            alSourceStop(sourceId);
            isPlaying = false;
        }
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public String getFilepath() {
        return filepath;
    }

    public boolean isPlaying() {
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if (state == AL_STOPPED) isPlaying = false;
        return isPlaying;
    }

    public void setRepeating(boolean repeating) {
        alSourcei(sourceId, AL_LOOPING, repeating ? 1 : 0);
    }

    public void updateGain() {
        general_volume = SoundProvider.getInstance().getVolume();
        alSourcef(sourceId, AL_GAIN, volume * general_volume);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        updateGain();
    }

    public void setCursor(int frame) {
        alSourcei(sourceId, AL_POSITION, frame);
    }

    public int getCursor() {
        return alGetSourcei(sourceId, AL_SAMPLE_OFFSET);
    }

    public int getLength() {
        int sizeInBytes = alGetBufferi(bufferId, AL_SIZE);

        int channels = alGetBufferi(bufferId, AL_CHANNELS);
        int bitsPerSample = alGetBufferi(bufferId, AL_BITS);

        int bytesPerSample = channels * (bitsPerSample / 8);

        if (bytesPerSample == 0) return 0;

        return sizeInBytes / bytesPerSample;
    }

    public int getLengthMS() {
        return getLength() / alGetBufferi(bufferId, AL_FREQUENCY);
    }

    public String getCurrentTime() {
        int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        if (frequency == 0) frequency = 44100;
        return alGetSourcei(sourceId, AL_SAMPLE_OFFSET) / frequency + "s";
    }

    public String getTotalLength() {
        int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        if (frequency == 0) return "0";
        return getLength() / frequency + "s";
    }
}
