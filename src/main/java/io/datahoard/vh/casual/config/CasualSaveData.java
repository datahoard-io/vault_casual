package io.datahoard.vh.casual.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class CasualSaveData extends SavedData {
	public static final String DATA_NAME = "CasualSaveData";
	public float casualPercent = 0;
	public Map<UUID, Float> playerCasualPercent = new HashMap<>();

	public CasualSaveData() {
	}

	@Nonnull
	public static CasualSaveData get(@Nonnull final Level world) {
		if (world.isClientSide) {
			throw new RuntimeException("Someone accessed this from the client");
		}
		DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
		return storage.computeIfAbsent(CasualSaveData::create, CasualSaveData::new, DATA_NAME);
	}

	@Nonnull
	public static float get(@Nonnull final Level world, @Nonnull final ServerPlayer player) {
		final CasualSaveData data = CasualSaveData.get(world);
		return data.playerCasualPercent.getOrDefault(player.getUUID(), data.casualPercent);
	}

	@Nonnull
	public static float get(@Nonnull final Level world, @Nonnull final UUID player) {
		final CasualSaveData data = CasualSaveData.get(world);
		return data.playerCasualPercent.getOrDefault(player, data.casualPercent);
	}

	@Nonnull
	public static CasualSaveData set(@Nonnull final Level world, @Nonnull final ServerPlayer player,
			final Float value) {
		if (world.isClientSide) {
			throw new RuntimeException("Someone accessed this from the client");
		}
		DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
		CasualSaveData current = storage.computeIfAbsent(CasualSaveData::create, CasualSaveData::new, DATA_NAME);
		current.playerCasualPercent.put(player.getUUID(), value);
		current.setDirty();
		return current;
	}

	private static CasualSaveData create(CompoundTag tag) {
		CasualSaveData data = new CasualSaveData();
		data.load(tag);
		return data;
	}

	private void load(CompoundTag tag) {
		this.casualPercent = tag.getFloat("casualPercent");
		this.playerCasualPercent.clear();
		if (!tag.contains("playerCasualPercent"))
			return;

		CompoundTag playerCasualPercent = tag.getCompound("playerCasualPercent");
		playerCasualPercent.getAllKeys().forEach(key -> {
			this.playerCasualPercent.put(UUID.fromString(key), playerCasualPercent.getFloat(key));
		});
	}

	public CompoundTag save(CompoundTag tag) {
		tag.putFloat("casualPercent", this.casualPercent);
		tag.put("playerCasualPercent", serializeData());

		return tag;
	}

	private CompoundTag serializeData() {
		CompoundTag tag = new CompoundTag();
		this.playerCasualPercent.forEach((uuid, percent) -> {
			tag.putFloat(uuid.toString(), percent);
		});

		return tag;
	}
}
