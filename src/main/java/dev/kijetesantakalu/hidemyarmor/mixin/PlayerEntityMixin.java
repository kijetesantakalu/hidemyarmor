package dev.kijetesantakalu.hidemyarmor.mixin;

import dev.kijetesantakalu.hidemyarmor.HideMyArmor;
import dev.kijetesantakalu.hidemyarmor.state.CosmeticState;
import dev.kijetesantakalu.hidemyarmor.util.DisguiseHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @ModifyVariable(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ItemStack replaceDroppedStack(ItemStack stack) {
        return DisguiseHelper.getPossibleUndisguisedStack(stack);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void undisguiseArmor(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((PlayerEntity)(Object)this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (CosmeticState.getPlayerState(player).hideArmor) {
                HideMyArmor.showArmor(player);
            }
        }
    }
}
