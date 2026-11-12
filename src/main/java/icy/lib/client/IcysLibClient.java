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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod.EventBusSubscriber(modid = IcysLib.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IcysLibClient {

    @SubscribeEvent
    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(BookLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener((RegisterClientCommandsEvent e) -> commands(e.getDispatcher()));
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
