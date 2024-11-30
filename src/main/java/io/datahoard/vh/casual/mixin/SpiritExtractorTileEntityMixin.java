package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.config.CasualSaveData;
import io.datahoard.vh.casual.iface.RecoveryDiscount;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import iskallia.vault.world.data.InventorySnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = SpiritExtractorTileEntity.class, remap = false)
public abstract class SpiritExtractorTileEntityMixin extends BlockEntity {

	public SpiritExtractorTileEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	@Shadow
	private GameProfile gameProfile;

	@Shadow
	private SpiritExtractorTileEntity.RecoveryCost recoveryCost = new SpiritExtractorTileEntity.RecoveryCost();

	@Shadow
	private InventorySnapshot inventorySnapshot = null;

	@OnlyIn(Dist.DEDICATED_SERVER)
	@Inject(method = "addSpiritRecoveryData", at = @At("HEAD"), cancellable = true)
	private void hook_addSpiritRecoveryData(CallbackInfoReturnable<CompoundTag> ci) {
		this.updateRecoveryDiscount();
	}

	private void updateRecoveryDiscount() {
		if (this.gameProfile == null) {
			VaultCasualMod.LOGGER.info("no gameProfile in SpiritExtractorTileEntity.updateRecoveryDiscount");
			return;
		}

		if (this.level instanceof ServerLevel serverLevel) {
			if (this.recoveryCost instanceof RecoveryDiscount.Setter setter) {
				setter.setRecoveryDiscount(CasualSaveData.get(serverLevel, this.gameProfile.getId()));
			}
		} else {
			VaultCasualMod.LOGGER.info("no ServerLevel in SpiritExtractorTileEntity.updateRecoveryDiscount");
		}
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	@Inject(method = "setGameProfile", at = @At("TAIL"), cancellable = true, remap = false)
	public void hook_setGameProfile(GameProfile gameProfile, CallbackInfo ci) {
		this.updateRecoveryDiscount();
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	@Inject(method = "recalculateCost", at = @At("HEAD"), cancellable = true, remap = false)
	public void hook_recalculateCost(CallbackInfo ci) {
		this.updateRecoveryDiscount();
	}
}
