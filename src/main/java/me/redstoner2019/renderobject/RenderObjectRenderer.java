package me.redstoner2019.renderobject;

import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.render.Renderer;

public interface RenderObjectRenderer {
    void render(Renderer renderer, TextRenderer textRenderer, RenderObject renderObject);
}
