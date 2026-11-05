package icy.lib;

import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public final class IcyBooks {
    private static Consumer<Identifier> opener = id -> {};

    private IcyBooks() {}

    public static void open(Identifier book) {
        opener.accept(book);
    }

    public static void bindOpener(Consumer<Identifier> fn) {
        opener = fn;
    }
}
