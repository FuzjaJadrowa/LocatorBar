package pl.fuzjajadrowa.locatorbar.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin {
    @Inject(method = "useOn", at = @At("RETURN"))
    private void locatorbar$setWaypointMetadata(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        ItemStack stack = context.getItemInHand();
        if (stack.isEmpty()) {
            return;
        }

        WaypointData.ensureWaypointData(stack, player);
    }
}