package pl.fuzjajadrowa.locatorbar.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.fuzjajadrowa.locatorbar.waypoint.LodestoneWaypointData;

@Mixin(CompassItem.class)
public final class CompassItemMixin {
    @Inject(method = "useOn", at = @At("RETURN"))
    private void locatorbar$assignWaypointOnLodestoneLink(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }

        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack stack = context.getItemInHand();
        LodestoneWaypointData.assignWaypointDataIfNeeded(stack, serverPlayer);
    }
}