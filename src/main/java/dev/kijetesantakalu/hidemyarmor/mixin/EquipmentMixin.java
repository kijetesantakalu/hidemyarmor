package dev.kijetesantakalu.hidemyarmor.mixin;

import dev.kijetesantakalu.hidemyarmor.util.DisguiseHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Equipment.class)
public interface EquipmentMixin {

    @Redirect(method = "equipAndSwap", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack replaceEquippedStack(PlayerEntity instance, EquipmentSlot slot) {
        ItemStack stack = instance.getEquippedStack(slot);
        return DisguiseHelper.getPossibleUndisguisedStack(stack);
    }
}
