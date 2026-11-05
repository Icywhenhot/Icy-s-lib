package icy.lib.client.book;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;
import java.util.Set;

class ImageLayer extends Layer {
    final Sprite sprite;

    ImageLayer(JsonObject o) {
        super(o);
        this.sprite = Sprite.read(o);
    }

    @Override
    void render(BookScreen s, GuiGraphics g) {
        sprite.draw(s, g, 0, 0);
    }

    @Override
    int click(BookScreen s, double ax, double ay) {
        return sprite.hit(s, ax, ay, 0, 0) ? BLOCK : PASS;
    }

    @Override
    void names(Map<String, Set<String>> domain, Set<String> need, Set<String> maybe) {
        sprite.names(domain, need, maybe);
    }
}
