package io.datahoard.vh.casual.iface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import iskallia.vault.research.ResearchTree;
import iskallia.vault.util.PlayerReference;
import net.minecraft.server.level.ServerPlayer;

public interface GetMissingResearch {
	public Map<String, Entry> getMissingResearch(ServerPlayer player);

	public class Entry {
		public Integer base;
		public List<PlayerReference> team;
		public List<PlayerReference> researched = new ArrayList<>();

		public Entry(Integer base, List<PlayerReference> team) {
			this.base = base;
			this.team = team;
		}

		public void addResearch() {
		}

		public Integer adjusted() {
			return (int) Math.max(1, Math.round(this.base.floatValue() * penaltyFactor(this.team.size())));
		}

		public Integer payed() {
			return (int) Math.max(1, Math.round(this.base.floatValue() * penaltyFactor(this.researched.size())));
		}

		public Integer payable() {
			return (int) Math.max(1, this.adjusted() - this.payed());
		}

		public static float penaltyFactor(Integer N) {
			return 1.0F + penaltyFor(N);
		}

		public static float penaltyFor(Integer N) {
			return ResearchTree.isPenalty ? 0.0F : N.floatValue() * 0.5F;
		}
	}
}
