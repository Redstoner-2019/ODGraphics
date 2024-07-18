package me.redstoner2019.graphics;

import me.redstoner2019.audio.SoundProvider;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.render.Renderer;
import me.redstoner2019.graphics.texture.TextureProvider;

public interface RenderI {
    void render(Renderer renderer, TextRenderer textRenderer);
}
