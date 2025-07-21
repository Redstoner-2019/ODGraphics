package me.redstoner2019.threed.render;

import me.redstoner2019.graphics.shader.Shader;
import me.redstoner2019.graphics.shader.ShaderProgram;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import me.redstoner2019.threed.model.Skybox;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

public class Renderer3D {
    private static Renderer3D INSTANCE = null;

    private int width;
    private int height;
    private Vector3f lightDir = new Vector3f(1, -2, 1).normalize();
    private Skybox skybox;

    private int shadowFBO;
    private int shadowMap;
    public static final int SHADOW_WIDTH = 2048, SHADOW_HEIGHT = 2048;
    private ShaderProgram shadowShader = new ShaderProgram();
    private Matrix4f lightSpaceMatrix;
    private float FOV = 90;


    private final ShaderProgram renderShader;

    private Renderer3D(){
        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/vertex.glsl");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/fragment.glsl");

        renderShader = new ShaderProgram();
        renderShader.attachShader(vertexShader);
        renderShader.attachShader(fragmentShader);
        renderShader.link();

        Shader skyboxVertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/skybox.vert");
        Shader skyboxFragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/skybox.frag");

        ShaderProgram skyboxShader = new ShaderProgram();
        skyboxShader.attachShader(skyboxVertexShader);
        skyboxShader.attachShader(skyboxFragmentShader);
        skyboxShader.link();

        Shader shadowVertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/shadow.vert");
        Shader shadowFragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/shadow.frag");

        shadowShader.attachShader(shadowVertexShader);
        shadowShader.attachShader(shadowFragmentShader);
        shadowShader.link();

        List<String> skyboxFaces = List.of(
                "src/main/resources/skybox/right.jpg",
                "src/main/resources/skybox/left.jpg",
                "src/main/resources/skybox/top.jpg",
                "src/main/resources/skybox/bottom.jpg",
                "src/main/resources/skybox/front.jpg",
                "src/main/resources/skybox/back.jpg"
        );


        this.skybox = new Skybox(skyboxFaces, skyboxShader);

        System.out.println("3D Shader ID " + renderShader.id);
    }

    public ShaderProgram getRenderShader() {
        return renderShader;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Vector3f getLightDir() {
        return lightDir;
    }

    public void setLightDir(Vector3f lightDir) {
        this.lightDir = lightDir.normalize();
    }

    public static Renderer3D getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Renderer3D();
        }
        return INSTANCE;
    }

    public void setupCamera() {
        Camera camera = Camera.getInstance();

        Matrix4f projection = getProjectionMatrix();
        Matrix4f view = camera.getViewMatrix();

        Vector3f cameraPos = camera.getPosition();

        Matrix4f model = new Matrix4f()
                .rotateY((float) glfwGetTime() * 0)
                .rotateX((float) glfwGetTime() * 0);

        renderShader.setUniform4fv("projection", projection);
        renderShader.setUniform4fv("view", view);
        renderShader.setUniform4fv("model", model);
        renderShader.setUniform3f("lightDir", lightDir);
        renderShader.setUniform3f("lightColor", new Vector3f(1, 1, 1));
        renderShader.setUniform3f("viewPos", cameraPos);
    }

    public void renderSkybox(){
        setupCamera();
        Matrix4f projection = getProjectionMatrix();
        Camera camera = Camera.getInstance();
        skybox.render(projection,camera.getViewMatrix());
    }

    public Matrix4f getProjectionMatrix() {
        float aspect = width / (float) height;
        return new Matrix4f().perspective(
                (float)Math.toRadians(FOV),  // fov
                aspect,        // aspect ratio
                0.01f,                        // near
                1000f                         // far
        );
    }


    public void renderModels(List<Model> models){
        //renderShadows(models);

        renderShader.bind();
        setupCamera();

        for(Model model : models){
            model.render(renderShader);
        }

        renderShader.unbind();
    }

    public void initShadowMapping() {
        shadowFBO = glGenFramebuffers();

        shadowMap = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, shadowMap);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT,
                SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMap, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Shadow framebuffer is not complete!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    public void renderShadows(List<Model> models) {
        updateLightSpaceMatrix(lightDir);

        // Set the shadow map resolution
        final int SHADOW_WIDTH = 2048, SHADOW_HEIGHT = 2048;

        // Bind the framebuffer to render depth from light's perspective
        glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);
        glClear(GL_DEPTH_BUFFER_BIT);

        // Use shadow map shader
        shadowShader.bind();
        shadowShader.setUniform4fv("lightSpaceMatrix", lightSpaceMatrix);

        // Render each model into the shadow map
        for (Model model : models) {
            shadowShader.setUniform4fv("model", model.getModelMatrix());
            model.getMesh().render();
        }

        shadowShader.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // Back to default framebuffer
    }

    public void updateLightSpaceMatrix(Vector3f lightDir) {
        Matrix4f lightProjection = new Matrix4f().ortho(-50f, 50f, -50f, 50f, 1f, 100f);
        Matrix4f lightView = new Matrix4f().lookAt(
                new Vector3f(lightDir).normalize().mul(-30f), // lightPos
                new Vector3f(0f, 0f, 0f),                     // target
                new Vector3f(0f, 1f, 0f)                      // up
        );
        lightSpaceMatrix = new Matrix4f(lightProjection).mul(lightView);
    }

}
