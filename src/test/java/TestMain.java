import me.redstoner2019.audio.SoundProvider;
import me.redstoner2019.graphics.RenderI;
import me.redstoner2019.graphics.animation.Animation;
import me.redstoner2019.graphics.animation.AnimationFrame;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.render.*;
import me.redstoner2019.graphics.shader.PostProcessingShader;
import me.redstoner2019.graphics.texture.TextureProvider;
import me.redstoner2019.gui.events.KeyPressedEvent;
import me.redstoner2019.gui.window.Window;
import me.redstoner2019.util.Resources;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Random;

public class TestMain extends Window {

    public TestMain(float x, float y, float width, float height) {
        super(x, y, width, height);

        init();

        setTitle("Test");

        TextureProvider textureProvider = TextureProvider.getInstance();
        SoundProvider soundProvider = SoundProvider.getInstance();

        /*for(String s : Resources.listResources("audio")){
            soundProvider.loadSound(s);
        }*/

        for(String s : Resources.listResources("textures")){
            textureProvider.loadTexture(s);
        }

        PostProcessingShader vignettePostProcess = new PostProcessingShader("shader/post/vignette.frag");
        PostProcessingShader colorPostProcess = new PostProcessingShader("shader/post/colourize.frag");
        PostProcessingShader noisePostProcess = new PostProcessingShader("shader/post/noise.frag");

        AnimationFrame[] animationFrames = new AnimationFrame[22];
        for (int i = 0; i < 11; i++) {
            animationFrames[i] = new AnimationFrame(textureProvider.get("textures.bonnie.jump." + i + ".png"), Color.WHITE, -1,-1,2,2);
        }
        for (int i = 11; i < 21; i++) {
            float a = (i-11)/10f;
            System.out.println(a);
            animationFrames[i] = new AnimationFrame(textureProvider.get("textures.bonnie.jump.10.png"), new Color(1,1,1,(1-a)), -1,-1,2,2);
        }
        animationFrames[21] = new AnimationFrame(textureProvider.get("textures.bonnie.jump.10.png"), new Color(1,1,1,0), -1,-1,2,2);
        Animation bonnieJump = new Animation(32,animationFrames);

        addKeyPressedEvent(new KeyPressedEvent() {
            @Override
            public void keyPressedEvent(int key, int action, int mods) {
                if(key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_RELEASE){
                    bonnieJump.play();
                }
                if(key == GLFW.GLFW_KEY_S && action == GLFW.GLFW_RELEASE){
                    bonnieJump.stop();
                }
            }
        });


        addRenderer(new RenderI() {
            @Override
            public void render(Renderer renderer, TextRenderer textRenderer) {
                colorPostProcess.setUniform4f("color",1,0,1,1);
                noisePostProcess.setUniform1f("seed",0.5f);
                noisePostProcess.setUniform1f("strength",0.5f);
                noisePostProcess.setUniform1i("pixelsX",5);
                noisePostProcess.setUniform1i("pixelsY",5);

                renderer.setPostProcessingShaders(noisePostProcess,vignettePostProcess);

                renderer.renderTexture(-1,-1,2,2,textureProvider.get("textures.optatada.jpg"),Color.WHITE);

                renderer.setPostProcessingShaders();

                if(bonnieJump.isRunning()) bonnieJump.render(renderer);

                renderer.setPostProcessingShaders();

                textRenderer.renderText(getFps() + " FPS",0,0,40, Color.RED);
                textRenderer.renderText(GLFW.glfwGetTime() + "",0,80,40, Color.RED);
            }
        });

        loop();
    }

    public static void main(String[] args) {
        new TestMain(0,0,1280,720);
    }
}
