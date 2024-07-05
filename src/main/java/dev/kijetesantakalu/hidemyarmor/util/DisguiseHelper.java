package dev.kijetesantakalu.hidemyarmor.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class DisguiseHelper {
    public static RegistryWrapper.WrapperLookup lookup;

    public static ItemStack getPossibleUndisguisedStack(ItemStack stack) {
        return isStackDisguised(stack) ? getUndisguisedStack(stack) : stack;
    }

    public static boolean isStackDisguised(ItemStack stack) {
        if (stack.isEmpty()) return false;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component != null) {
            return component.contains("disguised_item");
        }
        return false;
    }

    public static ItemStack getUndisguisedStack(ItemStack stack) {
        ItemStack result = ItemStack.EMPTY;
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component != null) {
            result = ItemStack.fromNbtOrEmpty(lookup, component.copyNbt().getCompound("disguised_item"));
        }
        return result;
    }

    public static ItemStack getDisguisedStack(ItemStack stack, ItemStack disguiseStack) {
        CustomModelDataComponent dataComponent = disguiseStack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        return getDisguisedStack(stack, disguiseStack.getItem(), dataComponent == null ? -1 : dataComponent.value());
    }

    public static ItemStack getDisguisedStack(ItemStack stack, Item disguise) {
        return getDisguisedStack(stack, disguise, -1);
    }

    public static ItemStack getDisguisedStack(ItemStack stack, Item disguise, int customModelData) {
        if (isStackDisguised(stack)) stack = getUndisguisedStack(stack);
        if (stack.getItem() == Items.ELYTRA) return stack;
        ItemStack disguiseStack = stack.isEmpty() ? new ItemStack(disguise, 1) : stack.copyComponentsToNewStack(disguise, 1);
        disguiseStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Disguised Item").formatted(Formatting.RED));
        disguiseStack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(" "),stack.getName().copy().formatted(Formatting.RED))));
        if (customModelData != -1) {
            disguiseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));
            disguiseStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        }

        NbtCompound nbt = new NbtCompound();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ArmorItem) disguiseStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, ((ArmorItem) stack.getItem()).getAttributeModifiers());
            NbtComponent previousData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (previousData != null) {
                nbt = previousData.copyNbt();
            }
        }
        nbt.put("disguised_item", stack.encodeAllowEmpty(lookup));
        disguiseStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return disguiseStack;
    }
}
