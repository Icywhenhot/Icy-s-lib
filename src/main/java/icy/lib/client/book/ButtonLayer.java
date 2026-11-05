package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.GsonHelper;

class ButtonLayer extends ImageLayer {
    private final String action;
    private final String sound;

    ButtonLayer(JsonObject o) {
        super(o);
        this.action = GsonHelper.getAsString(o, "action");
        if (!action.equals("close")) {
            throw new JsonParseException("unknown button action " + action);
        }
        this.sound = GsonHelper.getAsString(o, "sound", null);
    }

    @Override
    int click(BookScreen s, double ax, double ay) {
        if (!sprite.hit(s, ax, ay, 0, 0)) {
            return PASS;
        }
        s.click(sound);
        s.onClose();
        return DONE;
    }

    @Override
    void outline(BookScreen s, GuiGraphics g) {
        s.box(g, sprite.box(s, 0, 0));
    }
}
