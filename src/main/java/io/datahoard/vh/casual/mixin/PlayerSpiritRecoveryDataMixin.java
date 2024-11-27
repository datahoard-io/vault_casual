package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;

import iskallia.vault.world.data.PlayerSpiritRecoveryData;

@Mixin(value = PlayerSpiritRecoveryData.class, remap = false)
public class PlayerSpiritRecoveryDataMixin extends PlayerSpiritRecoveryData {
//	@Shadow
//	private final Map<UUID, Float> spiritRecoveryMultipliers = new HashMap();
//
//	public void setSpiritRecoveryMultiplierOverride(UUID playerId, float multiplier) {
//		this.spiritRecoveryMultipliers.put(playerId, multiplier);
//		this.setDirty();
//	}
}
