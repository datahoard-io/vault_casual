package io.datahoard.vh.casual.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.iface.GetAdjustedTeamResearchCostIncreaseMultiplier;
import io.datahoard.vh.casual.iface.SerializeShallow;
import io.datahoard.vh.casual.iface.SetResearchData;
import iskallia.vault.config.ResearchGroupConfig;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.group.ResearchGroup;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.nbt.CompoundTag;

@Mixin(value = ResearchTree.class, remap = false)
public abstract class ResearchTreeMixin
		implements SetResearchData, SerializeShallow, GetAdjustedTeamResearchCostIncreaseMultiplier {
	@Shadow
	protected final List<String> researchesDone = new ArrayList<>();
	@Shadow
	protected final List<PlayerReference> researchShares = new ArrayList<>();

	@Shadow
	public List<String> getResearchesDone() {
		return null;
	}

	private Optional<PlayerResearchesData> researchData = Optional.empty();

	@Inject(method = "getResearchCost", at = @At("HEAD"), cancellable = true, remap = false)
	public void hook_getResearchCost(Research research, CallbackInfoReturnable<Integer> ci) {
		if (this.researchData.isEmpty()) {
			VaultCasualMod.LOGGER.warn("ResearchTreeMixin.getResearchCost({}) without researchData",
					research.getName());
			return;
		}

		float cost = (float) research.getCost();
		ResearchGroupConfig config = ModConfigs.RESEARCH_GROUPS;
		ResearchGroup thisGroup = config.getResearchGroup(research);
		String thisGroupId = config.getResearchGroupId(thisGroup);

		for (String doneResearch : this.getResearchesDone()) {
			ResearchGroup otherGroup = config.getResearchGroup(doneResearch);
			if (otherGroup != null) {
				cost += otherGroup.getGroupIncreasedResearchCost(thisGroupId);
			}
		}

		cost *= 1.0F + this.getAdjustedTeamResearchCostIncreaseMultiplier(research);
		ci.setReturnValue(Math.max(1, Math.round(cost)));
		ci.cancel();
	}

	public List<PlayerReference> getPlayersWithout(Research research) {
		VaultCasualMod.LOGGER.warn("ResearchTreeMixin.getPlayersWithout({})", research.getName());
		List<PlayerReference> missing = new ArrayList<>();
		this.researchShares.forEach(player -> {
			final boolean has = this.researchData.get().getResearches(player.getId()).isResearched(research);
			VaultCasualMod.LOGGER.warn("  player={} researched={}", player.getName(), has);
			if (!has) {
				missing.add(player);
			}
		});
		return missing;
	}

	public float getAdjustedTeamResearchCostIncreaseMultiplier(Research research) {
		// naming is wrong, should be isPenaltyDisabled
		if (ResearchTree.isPenalty) {
			return 0.0F;
		}
		return (float) this.researchShares.stream().filter(player -> {
			return !this.researchData.get().getResearches(player.getId()).isResearched(research);
		}).count() * 0.5F;
	}

	public void setResearchData(@Nonnull PlayerResearchesData data) {
		this.researchData = Optional.ofNullable(data);
	}

	private boolean shallow = false;

	@Shadow
	public CompoundTag serializeNBT() {
		throw new AssertionError();
	}

	public CompoundTag serializeShallow() {
		CompoundTag tag;
		try {
			this.shallow = true;
			tag = this.serializeNBT();
		} finally {
			this.shallow = false;
		}
		return tag;
	}

	@Inject(method = "serializeNBT", at = @At("TAIL"), remap = false)
	private void hook_serializeNBT(CallbackInfoReturnable<CompoundTag> ci) {
		if (this.shallow) {
			return;
		}
		if (this.researchData.isEmpty()) {
			VaultCasualMod.LOGGER.warn("ResearchTreeMixin.serializeNBT without researchData");
			return;
		}
		CompoundTag tag = ci.getReturnValue();
		tag.put("playerResearchData", this.researchData.get().save(new CompoundTag()));
	}

	@Inject(method = "deserializeNBT", at = @At("TAIL"), remap = false)
	void hook_deserializeNBT(CompoundTag nbt, CallbackInfo ci) {
		if (!nbt.contains("playerResearchData", 10)) {
			VaultCasualMod.LOGGER.warn("ResearchTreeMixin.deserializeNBT without researchData");
			return;
		}
		this.researchData = Optional.of(PlayerResearchesDataInvoker.create(nbt.getCompound("playerResearchData")));
	}
}
