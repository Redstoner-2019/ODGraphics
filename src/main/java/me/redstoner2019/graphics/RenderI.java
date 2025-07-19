package me.redstoner2019.graphics;

import me.redstoner2019.audio.SoundProvider;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.render.Renderer;
import me.redstoner2019.graphics.texture.TextureProvider;
import me.redstoner2019.threed.render.Renderer3D;

public interface RenderI {
    void render(Renderer renderer, Renderer3D renderer3D, TextRenderer textRenderer);
}
