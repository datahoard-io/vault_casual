package io.datahoard.vh.casual.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.datahoard.vh.casual.iface.DumpToPlayer;
import io.datahoard.vh.casual.iface.GetMissingResearch;
import io.datahoard.vh.casual.iface.SerializeShallow;
import io.datahoard.vh.casual.iface.SetResearchData;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

@Mixin(value = PlayerResearchesData.class, remap = false)
public abstract class PlayerResearchesDataMixin extends SavedData implements DumpToPlayer, GetMissingResearch {
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
		SetResearchData setter = (SetResearchData) ci.getReturnValue();
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
			SetResearchData setter = (SetResearchData) tree;
			setter.setResearchData(this.pd());
		});
	}

//	public Map<String, GetMissingResearch.Entry> getMissingResearch(ServerPlayer player) {
//		final Map<String, GetMissingResearch.Entry> missing = new HashMap<>();
//
//		List<PlayerReference> team = this.pd().getTeamMembers(player.getUUID());
//		Integer teamSize = team.size();
//		ResearchTree tree = getResearches(player.getUUID());
//		List<String> researched = tree.getResearchesDone();
//		Map<String, Integer> researchedBy = new HashMap<>();
//
//		team.forEach(rp -> {
//			playerMap.get(rp.getId()).getResearchesDone().forEach(researchName -> {
//				if (researched.contains(researchName)) {
//					return;
//				}
//
//				researchedBy.put(researchName, 1 + researchedBy.getOrDefault(researchName, 0));
//			});
//		});
//
//		researchedBy.forEach((name, count) -> {
//			final Research research = ModConfigs.RESEARCHES.getByName(name);
//			missing.put(name, new GetMissingResearch.Entry(research.getCost(), teamSize, teamSize - count));
//		});
//
//		return missing;
//	}

	private PlayerResearchesData pd() {
		return (PlayerResearchesData) (Object) this;
	}

	public void dumpToPlayer(ServerPlayer player) {
		List<MutableComponent> text = new ArrayList<>();

		text.add(new TextComponent("--- TEAMS ---").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		IntStream.range(0, this.researchTeams.size()).forEach(N -> {
			Map<String, ResearchTree> researchedBy = new HashMap<>();
			final Integer researchers = this.researchTeams.get(N).size();
			List<ResearchTree> trees = new ArrayList<>();

			text.add(new TextComponent(String.format(" * TEAM %d", N)).withStyle(ChatFormatting.AQUA));
			text.add(new TextComponent(String.format("   * by member", N)).withStyle(ChatFormatting.AQUA));
			this.researchTeams.get(N).forEach(rp -> {
				text.add(new TextComponent("     * ")
						.append(new TextComponent(rp.getName()).withStyle(ChatFormatting.YELLOW)));
				if (!playerMap.containsKey(rp.getId())) {
					text.add(new TextComponent("       * ")
							.append(new TextComponent("no research").withStyle(ChatFormatting.RED)));
					return;
				}

				playerMap.get(rp.getId()).getResearchesDone().forEach(researchName -> {
					ResearchTree tmp = researchedBy.getOrDefault(researchName, ResearchTree.empty());
//					SetResearchData setter = (SetResearchData) tmp;
//					setter.setResearchData((PlayerResearchesData) (Object) this);
					tmp.addShare(rp);
					researchedBy.put(researchName, tmp);
					text.add(new TextComponent("       * ")
							.append(new TextComponent(researchName).withStyle(ChatFormatting.GREEN)));
					trees.add(this.getResearches(rp.getId()));
				});
			});
			if (researchedBy.isEmpty() || trees.isEmpty()) {
				return;
			}

			final ResearchTree tree = trees.get(0);

			text.add(new TextComponent("   * by research").withStyle(ChatFormatting.AQUA));
			researchedBy.forEach((name, tmpTree) -> {
				final Research research = ModConfigs.RESEARCHES.getByName(name);
				final Integer cost = research.getCost();
				final Integer adjusted = tree.getResearchCost(research);
				final Integer payed = tmpTree.getResearchCost(research);
				final Integer count = tmpTree.getResearchShares().size();

				text.add(new TextComponent("     * ".concat(name).concat(" ")).withStyle(ChatFormatting.AQUA)
						.append(new TextComponent(count.toString().concat("/").concat(researchers.toString()))
								.withStyle(ChatFormatting.GOLD)));

				text.add(new TextComponent(String.format("       cost=%d, adjusted=%d, payed=%d, diff=%d", cost,
						adjusted, payed, adjusted - payed)).withStyle(ChatFormatting.DARK_AQUA));
			});
		});

		text.forEach((msg) -> player.sendMessage(msg, player.getUUID()));
	}
}
