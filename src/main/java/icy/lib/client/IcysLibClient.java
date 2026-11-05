package icy.lib.client;

import com.mojang.brigadier.CommandDispatcher;
import icy.lib.IcyBooks;
import icy.lib.IcysLib;
import icy.lib.client.book.BookLoader;
import icy.lib.client.book.BookScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class IcysLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(Identifier.fromNamespaceAndPath(IcysLib.MOD_ID, "books"), BookLoader.INSTANCE);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx) -> commands(dispatcher));
        IcyBooks.bindOpener(BookScreen::open);
    }

    private static void commands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal(IcysLib.MOD_ID)
                .then(ClientCommands.literal("book")
                        .then(ClientCommands.argument("id", IdentifierArgument.id())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggestResource(BookLoader.ids(), b))
                                .executes(ctx -> {
                                    Identifier id = ctx.getArgument("id", Identifier.class);
                                    Minecraft.getInstance().schedule(() -> BookScreen.open(id));
                                    return 1;
                                })))
                .then(ClientCommands.literal("debug")
                        .executes(ctx -> {
                            BookScreen.debug = !BookScreen.debug;
                            ctx.getSource().sendFeedback(
                                    Component.translatable(BookScreen.debug ? "icys_lib.debug.on" : "icys_lib.debug.off"));
                            return 1;
                        })));
    }
}
