package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;

class ItemsLayer extends Layer {
    private final int[] area;
    private final int cell;
    private final int size;
    private final String category;
    private final String entry;
    private final String field;
    private final boolean center;
    private final boolean bend;

    ItemsLayer(JsonObject o) {
        super(o);
        this.area = Json.ints(o, "area", 4, null);
        if (area == null) {
            throw new JsonParseException("items needs an area");
        }
        this.cell = GsonHelper.getAsInt(o, "cell", 10);
        this.size = GsonHelper.getAsInt(o, "size", 8);
        this.category = GsonHelper.getAsString(o, "category");
        this.entry = GsonHelper.getAsString(o, "entry");
        this.field = GsonHelper.getAsString(o, "field");
        this.center = GsonHelper.getAsString(o, "align", "left").equals("center");
        this.bend = GsonHelper.getAsBoolean(o, "bend", true);
    }

    @Override
    void render(BookScreen s, GuiGraphics g) {
        Entry e = s.book.entry(category, s.fill(entry));
        if (e == null) {
            return;
        }
        List<ItemStack> stacks = e.items(field);
        int cols = Math.max(1, area[2] / cell);
        int pad = (cell - size) / 2;
        for (int i = 0; i < stacks.size(); i++) {
            int row = i / cols;
            int left = area[0];
            if (center) {
                int n = Math.min(cols, stacks.size() - row * cols);
                left += (area[2] - n * cell) / 2;
            }
            int x = left + (i % cols) * cell + pad;
            int y = area[1] + row * cell + pad;
            if (y + size > area[1] + area[3]) {
                break;
            }
            float dy = bend ? s.bend(x + size / 2f, y + size / 2f, size / 16f) : 0f;
            s.item(g, stacks.get(i), x, y + dy, size, true);
        }
    }
}
