import me.redstoner2019.audio.SoundProvider;
import me.redstoner2019.graphics.RenderI;
import me.redstoner2019.graphics.animation.Animation;
import me.redstoner2019.graphics.animation.AnimationFrame;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.render.*;
import me.redstoner2019.graphics.shader.PostProcessingShader;
import me.redstoner2019.graphics.shader.Shader;
import me.redstoner2019.graphics.shader.ShaderProgram;
import me.redstoner2019.graphics.texture.TextureProvider;
import me.redstoner2019.gui.events.KeyPressedEvent;
import me.redstoner2019.gui.window.Window;
import me.redstoner2019.threed.loader.OBJLoader;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.render.Renderer3D;
import me.redstoner2019.util.Resources;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;

public class TestMain extends Window {

    public TestMain(float x, float y, float width, float height) throws IOException {
        super(x, y, width, height);
        setDebugMode(true);

        init();

        setTitle("Test");

        TextureProvider textureProvider = TextureProvider.getInstance();
        SoundProvider soundProvider = SoundProvider.getInstance();

        //for(String s : Resources.listResources("audio")){
        //    soundProvider.loadSound(s);
        //}

        //for(String s : Resources.listResources("textures")){
        //    System.out.println("Loading " + s);
        //    textureProvider.loadTexture(s);
        //}

        PostProcessingShader vignettePostProcess = new PostProcessingShader("shader/post/vignette.frag");
        PostProcessingShader colorPostProcess = new PostProcessingShader("shader/post/colourize.frag");
        PostProcessingShader noisePostProcess = new PostProcessingShader("shader/post/noise.frag");

        Animation.init();

        /*AnimationFrame[] animationFrames = new AnimationFrame[22];
        for (int i = 0; i < 22; i++) {
            float f = (float) i / animationFrames.length;
            animationFrames[i] = new AnimationFrame(textureProvider.get("textures.test.jpg"), Color.WHITE, -f,-1,f*2,2);
        }*/

        //Animation animation0 = new Animation(2000,new AnimationFrame(textureProvider.get("textures.test.jpg"), Color.WHITE, 0,-1,0,2),new AnimationFrame(textureProvider.get("textures.test2.jpg"), Color.WHITE, -1,-1,2,2));
        //animation0.setReverse(false);
        //animation0.setRepeating(true);
        //bonnieJump = new Animation(32,animationFrames);

        Mesh teapot = OBJLoader.load("src/main/resources/models/teapot.obj");
        Mesh cube = Mesh.createCube();

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/vertex.glsl");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/fragment.glsl");

        ShaderProgram shader = new ShaderProgram();
        shader.attachShader(vertexShader);
        shader.attachShader(fragmentShader);
        shader.link();

        addKeyPressedEvent(new KeyPressedEvent() {
            @Override
            public void keyPressedEvent(int key, int action, int mods) {
                //if(key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_RELEASE){
                //    animation0.play();
                //}
                //if(key == GLFW.GLFW_KEY_S && action == GLFW.GLFW_RELEASE){
                //    animation0.stop();
                //}
            }
        });


        addRenderer(new RenderI() {
            @Override
            public void render(Renderer renderer, Renderer3D renderer3D, TextRenderer textRenderer) {
                //GL11.glViewport(0, 0, (int) width, (int) height);                            // REQUIRED
                //GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);         // REQUIRED
                //GL11.glEnable(GL_DEPTH_TEST);                                    // Once in init
                //GL11.glDisable(GL_CULL_FACE);                                    // For now
                renderer3D.renderMesh(cube);


                //GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                //renderer3D.setWidth((int) getWidth());
                //renderer3D.setHeight((int) getHeight());
                //renderer3D.setupCamera(); // important!
                //renderer3D.renderMesh(cube);
                setTitle(getFps() + " FPS");
                textRenderer.renderText(getFps() + " FPS",0,0,40, Color.RED);
                textRenderer.renderText(renderer.getWidth() + " / " + renderer.getHeight(),0,80,40, Color.RED);
                textRenderer.renderText(GLFW.glfwGetTime() + "",0,160,40, Color.RED);

                //System.exit(0);
            }
        });

        loop();
    }

    @Override
    public void update(float deltaTime) {

    }

    public static void main(String[] args) throws Exception {
        new TestMain(0,0,1280,720);
    }
}
