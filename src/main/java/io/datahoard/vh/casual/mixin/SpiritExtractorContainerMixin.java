package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.config.CasualSaveData;
import io.datahoard.vh.casual.iface.GetRecoveryDiscount;
import io.datahoard.vh.casual.iface.SetRecoveryDiscount;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import iskallia.vault.block.entity.SpiritExtractorTileEntity.RecoveryCost;
import iskallia.vault.container.SpiritExtractorContainer;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

@Mixin(value = SpiritExtractorContainer.class, remap = false)
public abstract class SpiritExtractorContainerMixin extends OverSizedSlotContainer {

	protected SpiritExtractorContainerMixin(MenuType<?> menuType, int id, Player player) {
		super(menuType, id, player);
	}

	@Shadow
	private final SpiritExtractorTileEntity tileEntity = null;

	@Shadow
	private RecoveryCost currentPlayersRecoveryCost = new RecoveryCost();

	@Shadow
	private void calculateCostForCurrentPlayer() {
	}

	private void updateRecoveryCost() {
		VaultCasualMod.LOGGER.info("SpiritExtractorContainerMixin.updateRecoveryCost");
		if (this.currentPlayersRecoveryCost instanceof SetRecoveryDiscount setter) {
			if (this.player instanceof ServerPlayer player && player.level instanceof ServerLevel serverLevel) {
				final float discount = CasualSaveData.get(serverLevel, player);
				setter.setRecoveryDiscount(discount);
			} else if (this.player instanceof GetRecoveryDiscount getter) {
				final float discount = getter.getRecoveryDiscount();
				setter.setRecoveryDiscount(discount);
			} else {
				VaultCasualMod.LOGGER.warn("not ServerPlayer in SpiritExtractorContainerMixin.updateRecoveryCost");
			}
		} else {
			VaultCasualMod.LOGGER.warn("not SetRecoveryDiscount in SpiritExtractorContainerMixin.updateRecoveryCost");
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	public void hook_SpiritExtractorContainer(CallbackInfo ci) {
		this.updateRecoveryCost();
	}

	@Inject(method = "getRecoveryCost", at = @At("HEAD"), remap = false, cancellable = true)
	public void hook_getRecoveryCost(CallbackInfoReturnable<RecoveryCost> ci) {
		if (this.tileEntity.getGameProfile().isEmpty()) {
			this.updateRecoveryCost();
			this.calculateCostForCurrentPlayer();
			ci.setReturnValue(currentPlayersRecoveryCost);
		} else {
			ci.setReturnValue(this.tileEntity.getRecoveryCost());
		}

		ci.cancel();
	}
}
