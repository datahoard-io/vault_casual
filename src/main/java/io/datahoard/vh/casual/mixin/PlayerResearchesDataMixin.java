package io.datahoard.vh.casual.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.datahoard.vh.casual.iface.ResearchData;
import io.datahoard.vh.casual.iface.SerializeShallow;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;

@Mixin(value = PlayerResearchesData.class, remap = false)
public abstract class PlayerResearchesDataMixin extends SavedData {
	@Shadow
	protected static final String DATA_NAME = "the_vault_PlayerResearches";
	@Shadow
	private final Map<UUID, ResearchTree> playerMap = new HashMap<>();
	@Shadow
	private final List<List<PlayerReference>> researchTeams = new ArrayList<>();
	@Shadow
	private final Map<PlayerReference, Set<PlayerReference>> invites = new HashMap<>();
	@Shadow
	private boolean AE2ResearchTree = false;
	@Shadow
	private final UUID AE2PlayerUUID = UUID.fromString("41c82c87-7afb-4024-ba57-13d2c99cae77");

	@Shadow
	public ResearchTree getResearches(UUID uuid) {
		throw new AssertionError();
	}

	@Shadow
	private static PlayerResearchesData create(CompoundTag tag) {
		throw new AssertionError();
	}

	@Inject(method = "getResearches(Ljava/util/UUID;)Liskallia/vault/research/ResearchTree;", at = @At("TAIL"), cancellable = true, remap = false)
	public void hook_getResearches(UUID uuid, CallbackInfoReturnable<ResearchTree> ci) {
		ResearchData.Setter setter = (ResearchData.Setter) ci.getReturnValue();
		setter.setResearchData(this.pd());
	}

	@Inject(method = "save", at = @At("HEAD"), cancellable = true, remap = true)
	public void hook_save(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> ci) {
		ListTag teams = new ListTag();
		this.researchTeams.forEach(teamList -> {
			ListTag teamMembers = new ListTag();
			teamList.forEach(ref -> teamMembers.add(ref.serialize()));
			teams.add(teamMembers);
		});
		nbt.put("sharedTeams", teams);
		this.playerMap.forEach((playerId, researchTree) -> {
			SerializeShallow serializer = (SerializeShallow) researchTree;
			nbt.put(playerId.toString(), serializer.serializeShallow());
		});
		ci.setReturnValue(nbt);
		ci.cancel();
	}

	@Inject(method = "load", at = @At("TAIL"), remap = false)
	private void hook_load(CompoundTag nbt, CallbackInfo ci) {
		this.playerMap.forEach((id, tree) -> {
			ResearchData.Setter setter = (ResearchData.Setter) tree;
			setter.setResearchData(this.pd());
		});
	}

	private PlayerResearchesData pd() {
		return (PlayerResearchesData) (Object) this;
	}

}
