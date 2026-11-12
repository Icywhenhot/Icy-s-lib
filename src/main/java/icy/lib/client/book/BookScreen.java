package icy.lib.client.book;

import com.mojang.blaze3d.platform.Window;
import icy.lib.IcysLib;
import icy.lib.client.anim.Anim;
import icy.lib.client.draw.Gfx;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookScreen extends Screen {
    private static final Map<ResourceLocation, Map<String, String>> remembered = new HashMap<>();
    private static final Set<String> overflowWarned = new HashSet<>();
    private static final int OPEN_MS = 220;
    private static final float RISE = 6f;
    public static boolean debug;

    final ResourceLocation id;
    Book book;
    final Anim.Lift lift = new Anim.Lift();
    private final Map<String, String> state;
    private final Map<String, List<FormattedCharSequence>> wrapped = new HashMap<>();
    private final long openedAt = Util.getMillis();
    private int k = 1;
    private int originX;
    private int originY;
    private double ax;
    private double ay;
    private double wheel;
    private ItemStack tooltip = ItemStack.EMPTY;

    private BookScreen(Book book) {
        super(Component.empty());
        this.id = book.id;
        this.book = book;
        this.state = new HashMap<>(remembered.getOrDefault(book.id, Map.of()));
    }

    public static void open(ResourceLocation id) {
        Minecraft mc = Minecraft.getInstance();
        Book b = BookLoader.get(id);
        if (b == null) {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.translatable("icys_lib.book.missing", id.toString()), false);
            }
            return;
        }
        mc.setScreen(new BookScreen(b));
    }

    @Override
    protected void init() {
        fit();
    }

    private void fit() {
        Window w = minecraft.getWindow();
        int fbW = w.getWidth();
        int fbH = w.getHeight();
        double gs = w.getGuiScale();
        int step = book.step;
        int cw = book.width + book.bleed[0] + book.bleed[2];
        int ch = book.height + book.bleed[1] + book.bleed[3];
        int pad = (int) Math.ceil(8 * gs);
        int fitK = Math.min((fbW - 2 * pad) / cw, (fbH - 2 * pad) / ch);
        int want = (int) Math.ceil(gs * book.size - 1.0e-6);
        k = (want + step - 1) / step * step;
        if (k > fitK) {
            k = fitK / step * step;
        }
        k = Math.max(k, step);
        originX = (fbW - cw * k) / 2 + book.bleed[0] * k;
        originY = (fbH - ch * k) / 2 + book.bleed[1] * k;
    }

    void reloaded(Book fresh) {
        if (fresh == null) {
            onClose();
            return;
        }
        book = fresh;
        wrapped.clear();
        fit();
    }

    String raw(String key) {
        return state.get(key);
    }

    String get(String key) {
        return state.getOrDefault(key, "");
    }

    void set(String key, String value) {
        state.put(key, value);
    }

    int scrolled(String key) {
        String raw = state.get("scroll:" + key);
        return raw == null ? 0 : Integer.parseInt(raw);
    }

    void scrollTo(String key, int n) {
        state.put("scroll:" + key, Integer.toString(n));
    }

    String fill(String raw) {
        return Template.fill(raw, this::get);
    }

    String lang(String text, String fallback) {
        String key = fill(text);
        if (fallback != null && !I18n.exists(key)) {
            return fill(fallback);
        }
        return key;
    }

    float snap(float v) {
        return Math.round(v * k) / (float) k;
    }

    private void pointer() {
        Window w = minecraft.getWindow();
        double fx = minecraft.mouseHandler.xpos() * w.getWidth() / w.getScreenWidth();
        double fy = minecraft.mouseHandler.ypos() * w.getHeight() / w.getScreenHeight();
        ax = (fx - originX) / k;
        ay = (fy - originY) / k;
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        super.renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        pointer();
        lift.beginFrame(0.06f);
        tooltip = ItemStack.EMPTY;
        for (Layer l : book.layers) {
            if (l.shown(this)) {
                l.settle(this);
            }
        }

        float gs = (float) minecraft.getWindow().getGuiScale();
        float t = Anim.easeOutCubic((Util.getMillis() - openedAt) / (float) OPEN_MS);
        int rise = Math.round((1f - t) * RISE * k);
        Gfx.push(g);
        Gfx.move(g, originX / gs, (originY + rise) / gs);
        Gfx.scale(g, k / gs);
        for (Layer l : book.layers) {
            if (l.shown(this)) {
                l.render(this, g);
            }
        }
        if (debug) {
            for (Layer l : book.layers) {
                if (l.shown(this)) {
                    l.outline(this, g);
                }
            }
        }
        Gfx.pop(g);

        if (!tooltip.isEmpty()) {
            Gfx.tooltip(g, font, tooltip, mouseX, mouseY);
        }
        if (debug) {
            Gfx.text(g, font, Mth.floor(ax) + ", " + Mth.floor(ay) + "  k=" + k, 4, 4, 0xFFFFFFFF);
        }
    }

    void blit(GuiGraphics g, Piece p, int[] r, float x, float y) {
        blit(g, p, r, x, y, 1f);
    }

    void blit(GuiGraphics g, Piece p, int[] r, float x, float y, float scale) {
        if (r[2] <= 0 || r[3] <= 0) {
            return;
        }
        Gfx.push(g);
        Gfx.move(g, snap(x), snap(y));
        Gfx.scale(g, scale);
        Gfx.blit(g, p.texture, 0, 0, r[0], r[1], r[2], r[3], p.texW, p.texH);
        Gfx.pop(g);
    }

    float crisp(float scale) {
        return Math.max(1, (int) (k * scale + 1.0e-4f)) / (float) k;
    }

    float bend(float x, float y, float unit) {
        return Math.round(book.bend.at(x, y) / unit) * unit;
    }

    void item(GuiGraphics g, ItemStack stack, float x, float y, float size, boolean tip) {
        float drawn = 16f * crisp(size / 16f);
        float off = (size - drawn) / 2f;
        Gfx.push(g);
        Gfx.move(g, x + off, y + off);
        Gfx.scale(g, drawn / 16f);
        Gfx.item(g, stack, 0, 0);
        Gfx.pop(g);
        if (tip && ax >= x && ax < x + size && ay >= y && ay < y + size) {
            tooltip = stack;
        }
    }

    void icon(GuiGraphics g, Piece p, float x, float y, float size) {
        float sc = crisp(size / p.texW);
        float off = (size - p.texW * sc) / 2f;
        blit(g, p, new int[]{0, 0, p.texW, p.texH}, x + off, y + off, sc);
    }

    private List<FormattedCharSequence> wrap(String key, int fw) {
        return wrapped.computeIfAbsent(key + "|" + fw, kk -> font.split(Component.translatable(key), fw));
    }

    int lineCount(String key, int w, float scale) {
        float s = scale > 0 ? scale : book.textScale;
        return wrap(key, Math.round(w / s)).size();
    }

    int lineRoom(int h, float scale) {
        float s = scale > 0 ? scale : book.textScale;
        return Math.max(1, Math.round(h / s) / font.lineHeight);
    }

    void text(GuiGraphics g, String key, int x, int y, int w, int h, String align, String valign, Integer color, float scale, boolean bent) {
        int count = lineCount(key, w, scale);
        int max = lineRoom(h, scale);
        if (count > max && overflowWarned.add(book.id + " " + key)) {
            IcysLib.LOGGER.warn("text {} in book {} has {} lines, room for {}", key, book.id, count, max);
        }
        text(g, key, x, y, w, h, align, valign, color, scale, bent, 0);
    }

    void text(GuiGraphics g, String key, int x, int y, int w, int h, String align, String valign, Integer color, float scale, boolean bent, int skip) {
        float s = scale > 0 ? scale : book.textScale;
        int fw = Math.round(w / s);
        int fh = Math.round(h / s);
        List<FormattedCharSequence> lines = wrap(key, fw);
        int count = Math.min(lines.size() - skip, lineRoom(h, scale));
        int top = valign.equals("middle") ? (fh - count * font.lineHeight + 1) / 2 : 0;
        int col = color != null ? color : book.textColor;
        Gfx.push(g);
        Gfx.move(g, x, y);
        Gfx.scale(g, s);
        for (int i = 0; i < count; i++) {
            FormattedCharSequence line = lines.get(skip + i);
            int lw = font.width(line);
            int lx = switch (align) {
                case "center" -> (fw - lw) / 2;
                case "right" -> fw - lw;
                default -> 0;
            };
            int ly = top + i * font.lineHeight;
            if (!bent || !book.bend.on()) {
                Gfx.text(g, font, line, lx, ly, col, book.textShadow);
                continue;
            }
            float mid = y + (ly + font.lineHeight / 2f) * s;
            int[] cur = {lx};
            line.accept((idx, style, cp) -> {
                FormattedCharSequence glyph = FormattedCharSequence.codepoint(cp, style);
                int adv = font.width(glyph);
                int dy = Math.round(bend(x + (cur[0] + adv / 2f) * s, mid, s) / s);
                Gfx.text(g, font, glyph, cur[0], ly + dy, col, book.textShadow);
                cur[0] += adv;
                return true;
            });
        }
        Gfx.pop(g);
    }

    void box(GuiGraphics g, float[] r) {
        if (r == null) {
            return;
        }
        int x0 = Math.round(r[0] * k);
        int y0 = Math.round(r[1] * k);
        int x1 = Math.round((r[0] + r[2]) * k);
        int y1 = Math.round((r[1] + r[3]) * k);
        int col = 0xFFFF40FF;
        Gfx.push(g);
        Gfx.scale(g, 1f / k);
        Gfx.fill(g, x0, y0, x1, y0 + 1, col);
        Gfx.fill(g, x0, y1 - 1, x1, y1, col);
        Gfx.fill(g, x0, y0, x0 + 1, y1, col);
        Gfx.fill(g, x1 - 1, y0, x1, y1, col);
        Gfx.pop(g);
    }

    void click(String sound) {
        if (sound == null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            return;
        }
        SoundEvent evt = SoundEvent.createVariableRangeEvent(new ResourceLocation(sound));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(evt, 1f));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        pointer();
        for (int i = book.layers.size() - 1; i >= 0; i--) {
            Layer l = book.layers.get(i);
            if (l.shown(this) && l.click(this, ax, ay) != Layer.PASS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        wheel += scrollY;
        int notches = (int) wheel;
        wheel -= notches;
        if (notches == 0) {
            return false;
        }
        pointer();
        for (int i = book.layers.size() - 1; i >= 0; i--) {
            Layer l = book.layers.get(i);
            if (l.shown(this) && l.scroll(this, ax, ay, notches)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!FMLEnvironment.production && keyCode == GLFW.GLFW_KEY_R) {
            BookLoader.reloadNow();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        remembered.put(id, new HashMap<>(state));
    }
}
