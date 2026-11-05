package icy.lib;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties properties, Identifier book) {
        super(properties.stacksTo(1).component(IcysLib.BOOK, book));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Identifier book = player.getItemInHand(hand).get(IcysLib.BOOK);
        if (level.isClientSide() && book != null) {
            IcyBooks.open(book);
        }
        return InteractionResult.SUCCESS;
    }
}
