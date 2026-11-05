package icy.lib;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public final class IcyBooks {
    private static Consumer<ResourceLocation> opener = id -> {};

    private IcyBooks() {}

    public static void open(ResourceLocation book) {
        opener.accept(book);
    }

    public static void bindOpener(Consumer<ResourceLocation> fn) {
        opener = fn;
    }
}
