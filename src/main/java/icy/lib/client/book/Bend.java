package icy.lib.client.book;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

final class Bend {
    static final Bend NONE = new Bend(new float[0], new float[0], 0, 1);

    private final float[] xs;
    private final float[] drops;
    private final float from;
    private final float to;

    private Bend(float[] xs, float[] drops, float from, float to) {
        this.xs = xs;
        this.drops = drops;
        this.from = from;
        this.to = to;
    }

    static Bend read(JsonObject book, int height) {
        JsonObject o = GsonHelper.getAsJsonObject(book, "bend", null);
        if (o == null) {
            return NONE;
        }
        JsonArray top = GsonHelper.getAsJsonArray(o, "top");
        float[] xs = new float[top.size()];
        float[] drops = new float[top.size()];
        for (int i = 0; i < top.size(); i++) {
            JsonArray pt = top.get(i).getAsJsonArray();
            xs[i] = pt.get(0).getAsFloat();
            drops[i] = pt.get(1).getAsFloat();
            if (i > 0 && xs[i] <= xs[i - 1]) {
                throw new JsonParseException("bend points must go left to right");
            }
        }
        int[] fade = Json.ints(o, "fade", 2, new int[]{0, height});
        if (fade[1] <= fade[0]) {
            throw new JsonParseException("bend fade must go top to bottom");
        }
        return new Bend(xs, drops, fade[0], fade[1]);
    }

    boolean on() {
        return xs.length > 0;
    }

    float at(float x, float y) {
        if (xs.length == 0) {
            return 0f;
        }
        float w = Mth.clamp((to - y) / (to - from), 0f, 1f);
        return w == 0f ? 0f : drop(x) * w;
    }

    private float drop(float x) {
        if (x <= xs[0]) {
            return drops[0];
        }
        for (int i = 1; i < xs.length; i++) {
            if (x <= xs[i]) {
                return Mth.lerp((x - xs[i - 1]) / (xs[i] - xs[i - 1]), drops[i - 1], drops[i]);
            }
        }
        return drops[drops.length - 1];
    }
}
