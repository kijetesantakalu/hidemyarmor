package dev.kijetesantakalu.hidemyarmor.state;

import dev.kijetesantakalu.hidemyarmor.HideMyArmor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class CosmeticState extends PersistentState {

    public HashMap<UUID, PlayerCosmeticData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            if (playerData.hat.equals(ItemStack.EMPTY)) { playerData.hat = new ItemStack(Items.AIR, 1); }
            playerNbt.put("hat", playerData.hat.encodeAllowEmpty(registryLookup));
            playerNbt.putBoolean("hideArmor", playerData.hideArmor);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static CosmeticState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        CosmeticState state = new CosmeticState();

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerCosmeticData playerData = new PlayerCosmeticData();

            playerData.hideArmor = playersNbt.getCompound(key).getBoolean("hideArmor");
            playerData.hat = ItemStack.fromNbtOrEmpty(registryLookup, playersNbt.getCompound(key).getCompound("hat"));
            if (playerData.hat.equals(ItemStack.EMPTY)) { playerData.hat = new ItemStack(Items.AIR, 1); }

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static final Type<CosmeticState> type = new Type<>(
            CosmeticState::new, // If there's no state yet create one
            CosmeticState::createFromNbt, // If there is a state NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );

    public static CosmeticState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        CosmeticState state = persistentStateManager.getOrCreate(type, HideMyArmor.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerCosmeticData getPlayerState(LivingEntity player) {
        CosmeticState serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerCosmeticData());
    }
}
