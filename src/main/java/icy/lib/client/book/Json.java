package icy.lib.client.book;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class Json {
    private Json() {}

    static int[] ints(JsonObject o, String key, int n, int[] def) {
        if (!o.has(key)) {
            return def;
        }
        JsonArray arr = GsonHelper.getAsJsonArray(o, key);
        if (arr.size() != n) {
            throw new JsonParseException(key + " needs " + n + " numbers");
        }
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = arr.get(i).getAsInt();
        }
        return out;
    }

    static float[] floats(JsonObject o, String key, float[] def) {
        if (!o.has(key)) {
            return def;
        }
        JsonArray arr = GsonHelper.getAsJsonArray(o, key);
        return new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat()};
    }

    static Integer color(JsonObject o, String key) {
        if (!o.has(key)) {
            return null;
        }
        String raw = GsonHelper.getAsString(o, key);
        String hex = raw.startsWith("#") ? raw.substring(1) : raw;
        long v = Long.parseLong(hex, 16);
        return hex.length() <= 6 ? (int) (0xFF000000L | v) : (int) v;
    }

    static Map<String, Set<String>> when(JsonObject o, String key) {
        if (!o.has(key)) {
            return Map.of();
        }
        Map<String, Set<String>> out = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : GsonHelper.getAsJsonObject(o, key).entrySet()) {
            Set<String> vals = new HashSet<>();
            if (e.getValue().isJsonArray()) {
                e.getValue().getAsJsonArray().forEach(v -> vals.add(v.getAsString()));
            } else {
                vals.add(e.getValue().getAsString());
            }
            out.put(e.getKey(), vals);
        }
        return out;
    }

    static Map<String, String> where(JsonObject o) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : GsonHelper.getAsJsonObject(o, "where", new JsonObject()).entrySet()) {
            out.put(e.getKey(), e.getValue().getAsString());
        }
        return out;
    }
}
