import me.redstoner2019.audio.SoundManager;
import me.redstoner2019.graphics.RenderI;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.Shader;
import me.redstoner2019.graphics.general.ShaderProgram;
import me.redstoner2019.graphics.general.TextureProvider;
import me.redstoner2019.gui.window.Window;
import me.redstoner2019.util.Resources;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.Random;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;

public class TestMain extends Window {

    private ShaderProgram noisePostProcess;

    public TestMain(float x, float y, float width, float height) {
        super(x, y, width, height);

        init();

        setTitle("Test");

        TextureProvider textureProvider = TextureProvider.getInstance();
        SoundManager soundManager = SoundManager.getInstance();

        for(String s : Resources.listResources("audio")){
            soundManager.loadSound(s);
        }

        for(String s : Resources.listResources("textures")){
            textureProvider.loadTexture(s);
        }

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/default.vert");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/noise.frag");

        noisePostProcess = new ShaderProgram();
        noisePostProcess.attachShader(vertexShader);
        noisePostProcess.attachShader(fragmentShader);
        noisePostProcess.link();

        addRenderer(new RenderI() {
            @Override
            public void render(Renderer renderer, TextRenderer textRenderer, TextureProvider textureProvider, SoundManager soundManager) {
                int seedLocation = glGetUniformLocation(noisePostProcess.id, "seed");
                glUniform1f(seedLocation, new Random().nextFloat());

                renderer.setPostProcessingShaders(noisePostProcess);

                renderer.renderTexture(-1,-1,2,2,textureProvider.get("textures\\test.jpg"),new Color(1,1,1,(float) (1 - (GLFW.glfwGetTime() / 20f))));

                renderer.setPostProcessingShaders();

                textRenderer.renderText(getFps() + "",0,0,40, Color.RED);
                textRenderer.renderText(GLFW.glfwGetTime() + "",0,80,40, Color.RED);
            }
        });

        loop();
    }

    public static void main(String[] args) {
        new TestMain(0,0,1280,720);
    }
}
