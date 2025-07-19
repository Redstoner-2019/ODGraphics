package me.redstoner2019.threed.render;

import me.redstoner2019.graphics.shader.Shader;
import me.redstoner2019.graphics.shader.ShaderProgram;
import me.redstoner2019.threed.model.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

public class Renderer3D {
    private static Renderer3D INSTANCE = null;

    private int width;
    private int height;

    private final ShaderProgram renderShader;

    private Renderer3D(){
        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/vertex.glsl");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/fragment.glsl");

        renderShader = new ShaderProgram();
        renderShader.attachShader(vertexShader);
        renderShader.attachShader(fragmentShader);
        renderShader.link();
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

    public static Renderer3D getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Renderer3D();
        }
        return INSTANCE;
    }

    public void setupCamera() {
        float aspect = width / (float) height;

        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), aspect, 0.1f, 1000.0f);
        Matrix4f view = new Matrix4f().lookAt(
                new Vector3f(0, 0, 3),
                new Vector3f(0, 0, 0),
                new Vector3f(0, 1, 0)
        );
        Matrix4f model = new Matrix4f().identity();

        //System.out.println("Projection:" + projection);
        //System.out.println("View:" + view);
        //System.out.println("Model:" + model.identity());


        renderShader.bind();
        renderShader.setUniform4fv("projection", projection);
        renderShader.setUniform4fv("view", view);
        renderShader.setUniform4fv("model", model);
        renderShader.setUniform3f("lightDir", new Vector3f(1, 1, -1).normalize());
        renderShader.setUniform3f("lightColor", new Vector3f(1, 1, 1));
        renderShader.setUniform3f("objectColor", new Vector3f(0.8f, 0.4f, 0.4f));
        renderShader.unbind();
    }


    public void renderMesh(Mesh mesh){
        //System.out.println("[Renderer3D] renderMesh()");
        //System.out.println("Width: " + width + ", Height: " + height);

        renderShader.bind();

        setupCamera();

        mesh.render();

        renderShader.unbind();
    }
}
