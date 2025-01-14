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

        for(String s : Resources.listResources("audio")){
            soundProvider.loadSound(s);
        }

        for(String s : Resources.listResources("textures")){
            System.out.println("Loading " + s);
            textureProvider.loadTexture(s);
        }

        PostProcessingShader vignettePostProcess = new PostProcessingShader("shader/post/vignette.frag");
        PostProcessingShader colorPostProcess = new PostProcessingShader("shader/post/colourize.frag");
        PostProcessingShader noisePostProcess = new PostProcessingShader("shader/post/noise.frag");

        Animation.init();

        /*AnimationFrame[] animationFrames = new AnimationFrame[22];
        for (int i = 0; i < 22; i++) {
            float f = (float) i / animationFrames.length;
            animationFrames[i] = new AnimationFrame(textureProvider.get("textures.test.jpg"), Color.WHITE, -f,-1,f*2,2);
        }*/

        Animation animation0 = new Animation(2000,new AnimationFrame(textureProvider.get("textures.test.jpg"), Color.WHITE, 0,-1,0,2),new AnimationFrame(textureProvider.get("textures.test2.jpg"), Color.WHITE, -1,-1,2,2));
        animation0.setReverse(false);
        animation0.setRepeating(true);
        //bonnieJump = new Animation(32,animationFrames);

        addKeyPressedEvent(new KeyPressedEvent() {
            @Override
            public void keyPressedEvent(int key, int action, int mods) {
                if(key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_RELEASE){
                    animation0.play();
                }
                if(key == GLFW.GLFW_KEY_S && action == GLFW.GLFW_RELEASE){
                    animation0.stop();
                }
            }
        });


        addRenderer(new RenderI() {
            @Override
            public void render(Renderer renderer, TextRenderer textRenderer) {
                colorPostProcess.setUniform4f("color",1,0,1,1);
                noisePostProcess.setUniform1f("seed",0.5f);
                noisePostProcess.setUniform1f("strength",0.5f);
                noisePostProcess.setUniform1i("pixelsX",2000);
                noisePostProcess.setUniform1i("pixelsY",2000);

                for (int i = 0; i < 1; i++) {
                    renderer.renderTexture(-1,-1,1,1,textureProvider.get("textures.test2.jpg"),Color.WHITE);
                }

                renderer.setPostProcessingShaders(vignettePostProcess);

                renderer.setPostProcessingShaders();

                if(animation0.isRunning()) animation0.render(renderer);

                renderer.setPostProcessingShaders();

                textRenderer.renderText(getFps() + " FPS",0,0,40, Color.RED);
                textRenderer.renderText(GLFW.glfwGetTime() + "",0,80,40, Color.RED);
            }
        });

        loop();
    }

    @Override
    public void update(float deltaTime) {

    }

    public static void main(String[] args) {
        new TestMain(0,0,1280,720);
    }
}
