package dev.kijetesantakalu.hidemyarmor;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import dev.kijetesantakalu.hidemyarmor.state.CosmeticState;
import dev.kijetesantakalu.hidemyarmor.state.PlayerCosmeticData;
import dev.kijetesantakalu.hidemyarmor.util.DisguiseHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;

import static net.minecraft.server.command.CommandManager.*;

public class HideMyArmor implements ModInitializer {

    public static final String MOD_ID = "hidemyarmor";

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DisguiseHelper.lookup = server.getRegistryManager();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("hat")
                .then(literal("item")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player != null) {
                                        final ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
                                        setHat(player, stack);
                                        context.getSource().sendFeedback(() -> Text.literal("Hat set to ").append(stack.getItem().getName()), false);
                                        return 1;
                                    }
                                    context.getSource().sendError(Text.literal("Command must be run by a player"));
                                    return 0;
                                })
                        )
                )
                .then(literal("custommodel")
                        .then(argument("custom_model_data", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player != null) {
                                        int customModelData = IntegerArgumentType.getInteger(context, "custom_model_data");
                                        final ItemStack stack = new ItemStack(Items.CONDUIT);
                                        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));
                                        setHat(player, stack);
                                        context.getSource().sendFeedback(() -> Text.literal("Hat set to Custom Model #").append(Text.literal(String.valueOf(customModelData))), false);
                                        return 1;
                                    }
                                    context.getSource().sendError(Text.literal("Command must be run by a player"));
                                    return 0;
                                })
                        )
                )
                .then(literal("remove")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                removeHat(player);
                                context.getSource().sendFeedback(() -> Text.literal("Hat removed"), false);
                                return 1;
                            }
                            context.getSource().sendError(Text.literal("Command must be run by a player"));
                            return 0;
                        })
                )
        ));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("hidearmor")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        PlayerCosmeticData playerState = CosmeticState.getPlayerState(player);
                        playerState.hideArmor = !playerState.hideArmor;
                        if (playerState.hideArmor && player.hurtTime == 0) hideArmor(player);
                        else showArmor(player);
                        context.getSource().sendFeedback(() -> Text.literal("Toggled hidden armor"), false);
                        return 1;
                    }
                    context.getSource().sendError(Text.literal("Command must be run by a player"));
                    return 0;
                })
        ));
    }

    public static void setHat(ServerPlayerEntity player, ItemStack hat) {
        PlayerCosmeticData state = CosmeticState.getPlayerState(player);
        state.hat = hat;

        ItemStack armor = player.getInventory().armor.get(3);
        ItemStack hatStack = DisguiseHelper.getDisguisedStack(armor, state.hat);
        player.getInventory().armor.set(3, hatStack);

        player.currentScreenHandler.sendContentUpdates();
    }

    public static void removeHat(ServerPlayerEntity player) {
        PlayerCosmeticData playerState = CosmeticState.getPlayerState(player);
        playerState.hat = ItemStack.EMPTY;

        ItemStack hatStack = player.getInventory().armor.get(3);
        player.getInventory().armor.set(3, DisguiseHelper.getUndisguisedStack(hatStack));

        player.currentScreenHandler.sendContentUpdates();
    }

    public static void hideArmor(ServerPlayerEntity player) {
        DefaultedList<ItemStack> armor = player.getInventory().armor;
        PlayerCosmeticData state = CosmeticState.getPlayerState(player);
        for (int i = 0; i < armor.size(); i++) {
            ItemStack currentArmor = armor.get(i);
            ItemStack disguise = ItemStack.EMPTY;
            if (i == 3 && !state.hat.isEmpty()) { disguise = state.hat; }
            else if (!currentArmor.isEmpty()) { disguise = new ItemStack(i == 3 ? Items.CONDUIT : Items.BARRIER, 1); }

            if (!disguise.isEmpty()) {
                ItemStack stack = DisguiseHelper.getDisguisedStack(currentArmor, disguise);
                player.getInventory().armor.set(i, stack);
            }
        }
        player.currentScreenHandler.sendContentUpdates();
    }


    public static void showArmor(ServerPlayerEntity player) {
        DefaultedList<ItemStack> armor = player.getInventory().armor;
        for (int i = 0; i < armor.size(); i++) {
                ItemStack currentArmor = armor.get(i);
                ItemStack undisguised = DisguiseHelper.getPossibleUndisguisedStack(currentArmor);
                player.getInventory().armor.set(i, undisguised);
            }
        player.currentScreenHandler.sendContentUpdates();
    }

}
