package io.datahoard.vh.casual.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.datahoard.vh.casual.iface.GetAdjustedTeamResearchCostIncreaseMultiplier;
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

//	@Shadow
//	private final ResearchTree researchTree;
//
//	private List<PlayerReference> missing = new ArrayList<>();

	protected ResearchDialogMixin(ResearchesElementContainerScreen skillTreeScreen) {
		super(skillTreeScreen);
		throw new AssertionError();
	}

//	@Inject(method = "update()V", at = @At(value = "HEAD"), remap = false)
//	public void hook_update_head(CallbackInfo ci) {
//		if (this.researchName == null) {
//			return;
//		}
//		if (this.researchTree == null) {
//			VaultCasualMod.LOGGER.warn("ResearchDialogMixin.hook_update_head() without researchTree");
//			return;
//		}
//		Research research = ModConfigs.RESEARCHES.getByName(this.researchName);
//		GetAdjustedTeamResearchCostIncreaseMultiplier getter = (GetAdjustedTeamResearchCostIncreaseMultiplier) this.researchTree;
//		this.missing = getter.getPlayersWithout(research);
//	}

	@Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getTeamResearchCostIncreaseMultiplier()F"), remap = false)
	public float hook_update_getTeamResearchCostIncreaseMultiplier(ResearchTree tree) {
		Research research = ModConfigs.RESEARCHES.getByName(this.researchName);
		GetAdjustedTeamResearchCostIncreaseMultiplier getter = (GetAdjustedTeamResearchCostIncreaseMultiplier) tree;
		return getter.getAdjustedTeamResearchCostIncreaseMultiplier(research);
	}

	@Redirect(method = "lambda$update$2", at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;getResearchShares()Ljava/util/List;"), remap = false, require = 2)
	public List<PlayerReference> hook_update_getResearchShares(ResearchTree tree) {
		Research research = ModConfigs.RESEARCHES.getByName(this.researchName);
		GetAdjustedTeamResearchCostIncreaseMultiplier getter = (GetAdjustedTeamResearchCostIncreaseMultiplier) tree;
		return getter.getPlayersWithout(research);
	}
}
