package icy.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuideBookItem extends Item {
    private final ResourceLocation book;

    public GuideBookItem(Properties properties, ResourceLocation book) {
        super(properties.stacksTo(1));
        this.book = book;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() && book != null) {
            IcyBooks.open(book);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
