package io.datahoard.vh.casual.iface;

import java.util.List;

import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;

public interface GetAdjustedTeamResearchCostIncreaseMultiplier {
	public float getAdjustedTeamResearchCostIncreaseMultiplier(Research research);

	public List<PlayerReference> getPlayersWithout(Research research);
}
