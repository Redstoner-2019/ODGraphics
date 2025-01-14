package me.redstoner2019.graphics.shader;

import org.lwjgl.opengl.GL20;

public class PostProcessingShader extends ShaderProgram {
    private boolean overrideRenderBounds;

    public PostProcessingShader(String fragment) {
        this(fragment,true);
    }

    public PostProcessingShader(String fragment, Boolean overrideRenderBounds) {
        Shader vertexShaderNoise = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/default.vert");
        Shader fragmentShaderNoise = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, fragment);

        attachShader(vertexShaderNoise);
        attachShader(fragmentShaderNoise);
        link();
        this.overrideRenderBounds = overrideRenderBounds;
    }

    public boolean isOverrideRenderBounds() {
        return overrideRenderBounds;
    }
}
