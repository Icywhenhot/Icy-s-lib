package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import icy.lib.client.draw.Gfx;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.GsonHelper;

final class Scrollbar {
    private final int[] area;
    private final int track;
    private final int thumb;

    private Scrollbar(int[] area, int track, int thumb) {
        this.area = area;
        this.track = track;
        this.thumb = thumb;
    }

    static Scrollbar read(JsonObject o) {
        JsonObject bar = GsonHelper.getAsJsonObject(o, "scrollbar", null);
        if (bar == null) {
            return null;
        }
        int[] area = Json.ints(bar, "area", 4, null);
        Integer track = Json.color(bar, "track");
        Integer thumb = Json.color(bar, "thumb");
        if (area == null || track == null || thumb == null) {
            throw new JsonParseException("scrollbar needs an area, track and thumb");
        }
        return new Scrollbar(area, track, thumb);
    }

    void render(GuiGraphicsExtractor g, int first, int shown, int total) {
        if (total <= shown) {
            return;
        }
        int h = Math.max(3, area[3] * shown / total);
        int y = area[1] + (area[3] - h) * first / (total - shown);
        Gfx.fill(g, area[0], area[1], area[0] + area[2], area[1] + area[3], track);
        Gfx.fill(g, area[0], y, area[0] + area[2], y + h, thumb);
    }

    boolean over(double ax, double ay) {
        return ax >= area[0] && ay >= area[1] && ax < area[0] + area[2] && ay < area[1] + area[3];
    }
}
