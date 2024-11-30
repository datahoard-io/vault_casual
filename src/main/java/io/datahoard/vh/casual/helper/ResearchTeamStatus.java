package io.datahoard.vh.casual.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;
import iskallia.vault.world.data.PlayerResearchesData;

public class ResearchTeamStatus {
	protected Map<UUID, ResearchTree> playerResearch = new HashMap<>();
	protected Map<String, ResearchStatus> byResearch = new HashMap<>();

	public ResearchTeamStatus(@Nonnull PlayerResearchesData data, @Nonnull List<PlayerReference> team) {
		this.load(data, team);
	}

	public void clear() {
		this.playerResearch.clear();
		this.byResearch.clear();
	}

	public void load(@Nonnull PlayerResearchesData data, @Nonnull List<PlayerReference> team) {
		this.clear();
		team.forEach(ref -> {
			final ResearchTree tree = data.getResearches(ref.getId());
			this.playerResearch.put(ref.getId(), tree);
			tree.getResearchesDone().forEach(name -> {
				ResearchStatus status = this.byResearch.computeIfAbsent(name, (k) -> {
					return new ResearchStatus(ModConfigs.RESEARCHES.getByName(k), team);
				});
				status.researchedBy.add(ref);
			});
		});
	}

	public Map<String, ResearchStatus> getByResearch() {
		return this.byResearch;
	}

	public class ResearchStatus {
		public Research research;
		public List<PlayerReference> team = new ArrayList<>();
		public List<PlayerReference> researchedBy = new ArrayList<>();

		public ResearchStatus(@Nonnull Research research, @Nonnull List<PlayerReference> team) {
			this.research = research;
			this.team = team;
		}

		public Integer getBaseCost() {
			return this.research.getCost();
		}

		public Integer getAdjusted() {
			return (int) Math.max(1, Math.round(this.getBaseCost().floatValue() * penaltyFactor(this.team.size())));
		}

		public Integer getSpent() {
			return (int) Math.max(1,
					Math.round(this.getBaseCost().floatValue() * penaltyFactor(this.researchedBy.size())));
		}

		public boolean isResearchedBy(UUID uuid) {
			return this.researchedBy.stream().filter(e -> e.getId().equals(uuid)).count() > 0;
		}

		public Integer getCost() {
			return (int) Math.max(1, this.getAdjusted() - this.getSpent());
		}

		public static float penaltyFactor(Integer N) {
			return 1.0F + penaltyFor(N);
		}

		public static float penaltyFor(Integer N) {
			return ResearchTree.isPenalty ? 0.0F : N.floatValue() * 0.5F;
		}
	}

}
