package dev.kijetesantakalu.hidemyarmor.mixin;

import dev.kijetesantakalu.hidemyarmor.util.DisguiseHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow @Final private static Logger LOGGER;

    @Shadow private ItemStack cursorStack;

    @Inject(method = "setCursorStack", at = @At("HEAD"), cancellable = true)
    private void replaceCursorStack(ItemStack stack, CallbackInfo ci) {
        this.cursorStack = DisguiseHelper.getPossibleUndisguisedStack(stack);
        ci.cancel();
    }

    @ModifyArg(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setStack(ILnet/minecraft/item/ItemStack;)V"))
    private ItemStack replaceInventoryStackArg(ItemStack stack) {
        return DisguiseHelper.getPossibleUndisguisedStack(stack);
    }

    @Redirect(method = "insertItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 1))
    private ItemStack replaceGetStack(Slot instance) {
        ItemStack stack = instance.getStack();
        return DisguiseHelper.getPossibleUndisguisedStack(stack);
    }
}