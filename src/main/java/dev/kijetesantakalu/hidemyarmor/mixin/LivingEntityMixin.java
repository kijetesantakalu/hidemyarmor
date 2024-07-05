package dev.kijetesantakalu.hidemyarmor.mixin;

import dev.kijetesantakalu.hidemyarmor.HideMyArmor;
import dev.kijetesantakalu.hidemyarmor.state.CosmeticState;
import dev.kijetesantakalu.hidemyarmor.state.PlayerCosmeticData;
import dev.kijetesantakalu.hidemyarmor.util.DisguiseHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public int hurtTime;

    @Redirect(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack replaceNewEquipment(LivingEntity instance, EquipmentSlot equipmentSlot) {
        ItemStack stack = instance.getEquippedStack(equipmentSlot);
        if (equipmentSlot == EquipmentSlot.OFFHAND) {
            if (DisguiseHelper.isStackDisguised(stack)) {
                stack = DisguiseHelper.getUndisguisedStack(stack);
                ((ServerPlayerEntity) instance).getInventory().offHand.set(0, stack);
            }
        }
        else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                && instance instanceof ServerPlayerEntity
                && !DisguiseHelper.isStackDisguised(stack))
        {
            if (instance.hurtTime > 0) return stack;
            PlayerCosmeticData state = CosmeticState.getPlayerState(instance);
            ItemStack disguise = ItemStack.EMPTY;
            if (equipmentSlot == EquipmentSlot.HEAD && !state.hat.isEmpty()) {
                disguise = state.hat;
            }
            else if (!stack.isEmpty() && state.hideArmor) {
                disguise = new ItemStack(equipmentSlot == EquipmentSlot.HEAD ? Items.CONDUIT : Items.BARRIER, 1);
            }

            if (!disguise.isEmpty()) {
                stack = DisguiseHelper.getDisguisedStack(stack, disguise);
                ((ServerPlayerEntity) instance).getInventory().armor.set(equipmentSlot.getEntitySlotId(), stack);
            }
        }
        return stack;
    }

    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void showHiddenArmorOnDamage(DamageSource damageSource, CallbackInfo ci) {
        if (((LivingEntity)(Object)this) instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            PlayerCosmeticData state = CosmeticState.getPlayerState(player);
            if (state.hideArmor) {
                HideMyArmor.showArmor(player);
            }
        }
    }

    @Inject(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;hurtTime:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void hideArmorOnNoHurtTime(CallbackInfo ci) {
        if (((LivingEntity)(Object)this instanceof ServerPlayerEntity) && this.hurtTime == 0) {
            HideMyArmor.hideArmor((ServerPlayerEntity)(Object)this);
        }
    }
}
