package icy.lib.client.book;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Entry {
    private static final long CYCLE_MS = 1000;

    final String name;
    final String category;
    final int sort;
    final List<String> iconItems = new ArrayList<>();
    final String iconTexture;
    private final JsonObject json;
    private final Map<String, List<ItemStack>> stacks = new HashMap<>();
    private final Map<String, List<List<ItemStack>>> slots = new HashMap<>();
    private List<ItemStack> icons;

    private Entry(String name, String category, JsonObject json) {
        this.name = name;
        this.category = category;
        this.json = json;
        this.sort = GsonHelper.getAsInt(json, "sort", 0);
        JsonObject ic = GsonHelper.getAsJsonObject(json, "icon", new JsonObject());
        JsonElement it = ic.get("item");
        if (it != null && it.isJsonArray()) {
            it.getAsJsonArray().forEach(id -> iconItems.add(id.getAsString()));
        } else if (it != null) {
            iconItems.add(it.getAsString());
        }
        this.iconTexture = GsonHelper.getAsString(ic, "texture", null);
    }

    static Entry read(String path, JsonObject json) {
        int slash = path.lastIndexOf('/');
        String name = path.substring(slash + 1);
        String folder = slash < 0 ? "" : path.substring(0, path.indexOf('/'));
        return new Entry(name, GsonHelper.getAsString(json, "category", folder), json);
    }

    String field(String key) {
        JsonElement el = json.get(key);
        return el != null && el.isJsonPrimitive() ? el.getAsString() : "";
    }

    ItemStack icon() {
        if (icons == null) {
            icons = new ArrayList<>();
            for (String id : iconItems) {
                ItemStack st = stack(id);
                if (!st.isEmpty()) {
                    icons.add(st);
                }
            }
        }
        return cycle(icons);
    }

    static ItemStack cycle(List<ItemStack> opts) {
        if (opts.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return opts.get((int) (Util.getMillis() / CYCLE_MS % opts.size()));
    }

    List<ItemStack> items(String key) {
        return stacks.computeIfAbsent(key, k -> {
            List<ItemStack> out = new ArrayList<>();
            if (json.has(k) && json.get(k).isJsonArray()) {
                for (JsonElement el : json.getAsJsonArray(k)) {
                    ItemStack st = stack(el.getAsString());
                    if (!st.isEmpty()) {
                        out.add(st);
                    }
                }
            }
            return out;
        });
    }

    List<List<ItemStack>> slots(String key) {
        return slots.computeIfAbsent(key, k -> {
            List<List<ItemStack>> out = new ArrayList<>();
            if (!json.has(k) || !json.get(k).isJsonArray()) {
                return out;
            }
            for (JsonElement el : json.getAsJsonArray(k)) {
                List<ItemStack> opts = new ArrayList<>();
                if (el.isJsonArray()) {
                    for (JsonElement id : el.getAsJsonArray()) {
                        ItemStack st = stack(id.getAsString());
                        if (!st.isEmpty()) {
                            opts.add(st);
                        }
                    }
                } else {
                    ItemStack st = stack(el.getAsString());
                    if (!st.isEmpty()) {
                        opts.add(st);
                    }
                }
                out.add(opts);
            }
            return out;
        });
    }

    private static ItemStack stack(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(rl).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
