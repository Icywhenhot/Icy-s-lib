package icy.lib.client;

import com.mojang.brigadier.CommandDispatcher;
import icy.lib.IcyBooks;
import icy.lib.IcysLib;
import icy.lib.client.book.BookLoader;
import icy.lib.client.book.BookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = IcysLib.MOD_ID, dist = Dist.CLIENT)
public final class IcysLibClient {

    public IcysLibClient(IEventBus bus) {
        bus.addListener(RegisterClientReloadListenersEvent.class, e -> e.registerReloadListener(BookLoader.INSTANCE));
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, e -> commands(e.getDispatcher()));
        IcyBooks.bindOpener(BookScreen::open);
    }

    private static void commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(IcysLib.MOD_ID)
                .then(Commands.literal("book")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggestResource(BookLoader.ids(), b))
                                .executes(ctx -> {
                                    BookScreen.open(ResourceLocationArgument.getId(ctx, "id"));
                                    return 1;
                                })))
                .then(Commands.literal("debug")
                        .executes(ctx -> {
                            BookScreen.debug = !BookScreen.debug;
                            Minecraft.getInstance().gui.getChat().addMessage(
                                    Component.translatable(BookScreen.debug ? "icys_lib.debug.on" : "icys_lib.debug.off"));
                            return 1;
                        })));
    }
}
