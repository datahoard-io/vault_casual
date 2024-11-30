package io.datahoard.vh.casual.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.datahoard.vh.casual.iface.AdjustedTeamResearch;
import iskallia.vault.client.gui.screen.player.ResearchesElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.ResearchDialog;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractDialog;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.util.PlayerReference;

@Mixin(value = ResearchDialog.class, remap = false)
public abstract class ResearchDialogMixin extends AbstractDialog<ResearchesElementContainerScreen> {
	@Shadow
	private String researchName = null;

	protected ResearchDialogMixin(ResearchesElementContainerScreen skillTreeScreen) {
		super(skillTreeScreen);
		throw new AssertionError();
	}

	@Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getTeamResearchCostIncreaseMultiplier()F"), remap = false)
	public float hook_update_getTeamResearchCostIncreaseMultiplier(ResearchTree tree) {
		Research research = ModConfigs.RESEARCHES.getByName(this.researchName);
		if (tree instanceof AdjustedTeamResearch getter) {
			return getter.getAdjustedTeamResearchCostIncreaseMultiplier(research);
		}
		return tree.getTeamResearchCostIncreaseMultiplier();
	}

	@Redirect(method = "lambda$update$2", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getResearchShares()Ljava/util/List;"), remap = false, require = 2)
	public List<PlayerReference> hook_update_getResearchShares(ResearchTree tree) {
		Research research = ModConfigs.RESEARCHES.getByName(this.researchName);
		if (tree instanceof AdjustedTeamResearch getter) {
			return getter.getPlayersWithout(research);
		}
		return tree.getResearchShares();
	}
}
