package icy.lib.client.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icy.lib.IcysLib;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class BookLoader extends SimplePreparableReloadListener<Map<ResourceLocation, Book>> {
    public static final BookLoader INSTANCE = new BookLoader();
    private static final String DIR = "icys_lib/books";

    private static volatile Map<ResourceLocation, Book> books = Map.of();

    private BookLoader() {}

    public static Set<ResourceLocation> ids() {
        return books.keySet();
    }

    static Book get(ResourceLocation id) {
        return books.get(id);
    }

    @Override
    protected Map<ResourceLocation, Book> prepare(ResourceManager rm, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonObject> defs = new HashMap<>();
        Map<ResourceLocation, Map<String, JsonObject>> entries = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> f : rm.listResources(DIR, rl -> rl.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation file = f.getKey();
            String rest = file.getPath().substring(DIR.length() + 1, file.getPath().length() - 5);
            int slash = rest.indexOf('/');
            if (slash < 0) {
                continue;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), rest.substring(0, slash));
            String inner = rest.substring(slash + 1);
            JsonObject json;
            try (Reader r = f.getValue().openAsReader()) {
                json = JsonParser.parseReader(r).getAsJsonObject();
            } catch (IOException | RuntimeException e) {
                IcysLib.LOGGER.error("could not read {}: {}", file, e.getMessage());
                continue;
            }
            if (inner.equals("book")) {
                defs.put(id, json);
            } else if (inner.startsWith("entries/")) {
                entries.computeIfAbsent(id, k -> new TreeMap<>()).put(inner.substring(8), json);
            }
        }
        Map<ResourceLocation, Book> out = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonObject> d : defs.entrySet()) {
            try {
                out.put(d.getKey(), new Book(d.getKey(), d.getValue(), entries.getOrDefault(d.getKey(), Map.of()), rm));
            } catch (IOException | RuntimeException e) {
                IcysLib.LOGGER.error("book {} skipped: {}", d.getKey(), e.getMessage());
            }
        }
        return out;
    }

    @Override
    protected void apply(Map<ResourceLocation, Book> loaded, ResourceManager rm, ProfilerFiller profiler) {
        books = Map.copyOf(loaded);
        if (Minecraft.getInstance().screen instanceof BookScreen screen) {
            screen.reloaded(books.get(screen.id));
        }
    }

    static void reloadNow() {
        Minecraft mc = Minecraft.getInstance();
        ResourceManager rm = mc.getResourceManager();
        Map<ResourceLocation, Book> loaded = INSTANCE.prepare(rm, InactiveProfiler.INSTANCE);
        for (Book b : loaded.values()) {
            for (Piece p : b.pieces()) {
                mc.getTextureManager().release(p.texture);
            }
        }
        INSTANCE.apply(loaded, rm, InactiveProfiler.INSTANCE);
    }
}
