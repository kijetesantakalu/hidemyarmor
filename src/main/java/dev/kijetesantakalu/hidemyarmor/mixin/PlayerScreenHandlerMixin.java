package dev.kijetesantakalu.hidemyarmor.mixin;

import dev.kijetesantakalu.hidemyarmor.util.DisguiseHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    @Redirect(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack replaceInventoryStack(Slot instance) {
        ItemStack stack = instance.getStack();
        return DisguiseHelper.getPossibleUndisguisedStack(stack);
    }

    @Redirect(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;hasStack()Z", ordinal = 1))
    private boolean replaceHasStackCheck(Slot instance) {
        ItemStack stack = instance.getStack();
        return !DisguiseHelper.getPossibleUndisguisedStack(stack).isEmpty();
    }
}
