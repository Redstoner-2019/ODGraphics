import game.FastNoiseLite;
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
import me.redstoner2019.gui.events.MouseMovedEvent;
import me.redstoner2019.gui.window.Window;
import me.redstoner2019.threed.loader.GLBLoader;
import me.redstoner2019.threed.loader.GLTFLoader;
import me.redstoner2019.threed.loader.OBJLoader;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import me.redstoner2019.threed.model.Shapes;
import me.redstoner2019.threed.render.Camera;
import me.redstoner2019.threed.render.Renderer3D;
import me.redstoner2019.util.Resources;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
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

    private Model teapot;
    private Model suzanne;

    public TestMain(float x, float y, float width, float height) throws Exception {
        super(x, y, width, height);
        setDebugMode(true);

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

        //PostProcessingShader vignettePostProcess = new PostProcessingShader("shader/post/vignette.frag");
        //PostProcessingShader colorPostProcess = new PostProcessingShader("shader/post/colourize.frag");
        //PostProcessingShader noisePostProcess = new PostProcessingShader("shader/post/noise.frag");

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

        //teapot = OBJLoader.load("src/main/resources/models/teapot.obj");
        //suzanne = OBJLoader.load("src/main/resources/models/suzanne.obj");
        //Model freddy = OBJLoader.load("src/main/resources/models/freddy/freddy.obj");
        //Model fnaf1map = OBJLoader.load("src/main/resources/models/map/fnaf_1_map.obj");



        //Model freddy = OBJLoader.loadWithMaterials("src/main/resources/models/freddy/freddy.obj", "src/main/resources/models/freddy/freddy.mtl", "src/main/resources/models/freddy/");
        Model bonnie = OBJLoader.loadWithMaterials("src/main/resources/models/fnaf/Bonnie/Bonnie.obj", "src/main/resources/models/fnaf/Bonnie/Bonnie.mtl", "src/main/resources/models/fnaf/Bonnie/");
        //Model chica = OBJLoader.loadWithMaterials("src/main/resources/models/chica/chica.obj", "src/main/resources/models/chica/chica.mtl", "src/main/resources/models/chica/");
        //Model foxy = OBJLoader.loadWithMaterials("src/main/resources/models/foxy/foxy.obj", "src/main/resources/models/foxy/foxy.mtl", "src/main/resources/models/foxy/");
        //Model fnaf1map = OBJLoader.loadWithMaterials("src/main/resources/models/map/fnaf_1_map.obj", "src/main/resources/models/map/fnaf_1_map.mtl", "src/main/resources/models/map/");

        //fnaf1map.setPosition(new Vector3f(0,0,0));

        //freddy.setPositionOffset(new Vector3f(0.13f,.96f,-1.6f));

        //bonnie.setScale(new Vector3f(1f/128f));
        //foxy.setScale(new Vector3f(1f/128f));
        //chica.setScale(new Vector3f(5));
        //freddy.setScale(new Vector3f(1f/6f));

        //bonnie.setScale(bonnie.getScale().mul(2.25f));
        //foxy.setScale(foxy.getScale().mul(2.25f));
        //chica.setScale(chica.getScale().mul(2.25f));
        //freddy.setScale(freddy.getScale().mul(2.25f));

        //freddy.setPosition(new Vector3f(0,0,0));
        bonnie.setPosition(new Vector3f(-1,0,0));
        //chica.setPosition(new Vector3f(1,0,0));
        //foxy.setPosition(new Vector3f(0,0,0));

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/vertex.glsl");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/fragment.glsl");

        ShaderProgram shader = new ShaderProgram();
        shader.attachShader(vertexShader);
        shader.attachShader(fragmentShader);
        shader.link();

        addKeyPressedEvent(new KeyPressedEvent() {
            @Override
            public void keyPressedEvent(int key, int action, int mods) {
                if (key == GLFW_KEY_F11) toggleFullscreen();
            }
        });

        GLFW.glfwSetInputMode(getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        GLFW.glfwSetCursorPosCallback(getWindow(), new GLFWCursorPosCallback() {
            private double lastX = -1;
            private double lastY = -1;
            private boolean firstMouse = true;

            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (firstMouse) {
                    lastX = xpos;
                    lastY = ypos;
                    firstMouse = false;
                }

                double xoffset = xpos - lastX;
                double yoffset = lastY - ypos; // reversed since y-coordinates go from top to bottom

                lastX = xpos;
                lastY = ypos;

                float sensitivity = 0.1f;
                xoffset *= sensitivity;
                yoffset *= sensitivity;

                Camera camera = Camera.getInstance();

                camera.yaw += xoffset;
                camera.pitch += yoffset;

                // clamp pitch
                camera.pitch = Math.max(-89.0f, Math.min(89.0f, camera.pitch));
            }
        });

        FastNoiseLite fnl = new FastNoiseLite();
        fnl.SetFractalType(FastNoiseLite.FractalType.FBm);
        fnl.SetSeed(69);

        int size = 1000;

        Model terrain = generateTerrainModel(size, fnl, new Vector3f(0.4f, 0.7f, 0.3f));
        terrain.setPosition(new Vector3f(-((float) size /2),0,-((float) size /2)));

        addRenderer(new RenderI() {
            @Override
            public void render(Renderer renderer, Renderer3D renderer3D, TextRenderer textRenderer) {
                renderer3D.renderModels(List.of(bonnie));

                Camera camera = Camera.getInstance();

                //renderer.renderTexture(-1,-1,1,1,textureProvider.get("textures.test.jpg"));

                setTitle(getFps() + " FPS - " + renderer.getWidth() + " / " + renderer.getHeight());
                textRenderer.renderText(getFps() + " FPS",0,0,20, Color.WHITE, renderer.getWidth(), renderer.getHeight());
                textRenderer.renderText(renderer.getWidth() + " / " + renderer.getHeight(),0,20,20, Color.WHITE, renderer.getWidth(), renderer.getHeight());
                textRenderer.renderText(GLFW.glfwGetTime() + "",0,40,20, Color.WHITE, renderer.getWidth(), renderer.getHeight());
                textRenderer.renderText(camera.getPosition().x + " / " + camera.getPosition().y + " / " + camera.getPosition().z,0,60,20, Color.WHITE, renderer.getWidth(), renderer.getHeight());
            }
        });

        loop();
    }

    @Override
    public void update(float deltaTime) {
        Camera camera = Camera.getInstance();

        Vector3f front = camera.getDirection();
        Vector3f right = front.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();

        float speed = 0.05f * deltaTime;

        if (isKeyDown(GLFW_KEY_LEFT_CONTROL)) speed *= 2;

        if (isKeyDown(GLFW_KEY_W)) camera.position.add(new Vector3f(front).mul(speed));
        if (isKeyDown(GLFW_KEY_S)) camera.position.sub(new Vector3f(front).mul(speed));
        if (isKeyDown(GLFW_KEY_A)) camera.position.sub(new Vector3f(right).mul(speed));
        if (isKeyDown(GLFW_KEY_D)) camera.position.add(new Vector3f(right).mul(speed));
        if (isKeyDown(GLFW_KEY_LEFT_SHIFT)) camera.position.add(new Vector3f(0,-1,0).mul(speed));
        if (isKeyDown(GLFW_KEY_SPACE)) camera.position.add(new Vector3f(0,1,0).mul(speed));
        if (isKeyDown(GLFW_KEY_ESCAPE)) System.exit(0);

        //teapot.rotate(deltaTime * 10,0);
    }

    public static void main(String[] args) throws Exception {
        new TestMain(0,0,1280,720);
    }

    public static Model generateTerrainModel(int size, FastNoiseLite fnl, Vector3f color) {
        int vertexCount = (size + 1) * (size + 1);
        int triangleCount = size * size * 2;

        float[] positions = new float[vertexCount * 3];
        float[] normals = new float[vertexCount * 3];
        int[] indices = new int[triangleCount * 3];

        // Fill positions
        int index = 0;
        for (int z = 0; z <= size; z++) {
            for (int x = 0; x <= size; x++) {
                float y = fnl.GetNoise(x, z)*10;
                positions[index * 3] = x;
                positions[index * 3 + 1] = y;
                positions[index * 3 + 2] = z;
                index++;
            }
        }

        // Fill indices
        int idx = 0;
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                int topLeft = z * (size + 1) + x;
                int topRight = topLeft + 1;
                int bottomLeft = topLeft + (size + 1);
                int bottomRight = bottomLeft + 1;

                indices[idx++] = topLeft;
                indices[idx++] = bottomLeft;
                indices[idx++] = topRight;

                indices[idx++] = topRight;
                indices[idx++] = bottomLeft;
                indices[idx++] = bottomRight;
            }
        }

        // Calculate smooth normals
        Vector3f[] tempNormals = new Vector3f[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            tempNormals[i] = new Vector3f(0, 0, 0);
        }

        for (int i = 0; i < indices.length; i += 3) {
            int i0 = indices[i];
            int i1 = indices[i + 1];
            int i2 = indices[i + 2];

            Vector3f v0 = new Vector3f(
                    positions[i0 * 3],
                    positions[i0 * 3 + 1],
                    positions[i0 * 3 + 2]);

            Vector3f v1 = new Vector3f(
                    positions[i1 * 3],
                    positions[i1 * 3 + 1],
                    positions[i1 * 3 + 2]);

            Vector3f v2 = new Vector3f(
                    positions[i2 * 3],
                    positions[i2 * 3 + 1],
                    positions[i2 * 3 + 2]);

            Vector3f edge1 = v1.sub(v0);
            Vector3f edge2 = v2.sub(v0);
            Vector3f normal = edge1.cross(edge2).normalize();

            tempNormals[i0] = tempNormals[i0].add(normal);
            tempNormals[i1] = tempNormals[i1].add(normal);
            tempNormals[i2] = tempNormals[i2].add(normal);
        }

        // Normalize final normals
        for (int i = 0; i < vertexCount; i++) {
            Vector3f n = tempNormals[i].normalize();
            normals[i * 3] = n.x;
            normals[i * 3 + 1] = n.y;
            normals[i * 3 + 2] = n.z;
        }

        Mesh mesh = new Mesh(positions, normals, indices);
        return new Model(mesh, TextureProvider.getInstance().get("textures.Stone.ground2.png"));
    }

}
