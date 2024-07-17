package me.redstoner2019.graphics;

import me.redstoner2019.audio.SoundManager;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.TextureProvider;

public interface RenderI {
    void render(Renderer renderer, TextRenderer textRenderer, TextureProvider textureProvider, SoundManager soundManager);
}
