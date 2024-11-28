package io.datahoard.vh.casual.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.config.CasualSaveData;
import io.datahoard.vh.casual.iface.WithDiscount;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import iskallia.vault.world.data.InventorySnapshot;
import iskallia.vault.world.data.PlayerSpiritRecoveryData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = SpiritExtractorTileEntity.class, remap = false)
public abstract class SpiritExtractorTileEntityMixin extends BlockEntity implements WithDiscount {

	public SpiritExtractorTileEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}

	@Shadow
	private GameProfile gameProfile;

	@Shadow
	private int playerLevel;

	@Shadow
	private InventorySnapshot inventorySnapshot = null;

	@Shadow
	private SpiritExtractorTileEntity.RecoveryCost recoveryCost = new SpiritExtractorTileEntity.RecoveryCost();

	@Shadow
	private final NonNullList<ItemStack> items = NonNullList.create();

	@Shadow
	private float rescuedBonus = 0.0F;

	private Optional<Float> clientSideDiscount;

	public float getDiscount() {
		if (this.gameProfile == null) {
			return 0;
		}
		if (this.level instanceof ServerLevel serverLevel) {
			return CasualSaveData.get(serverLevel, this.gameProfile.getId());
		}
		if (this.clientSideDiscount.isPresent()) {
			return this.clientSideDiscount.get();
		}
		return 0;
	}

	@Inject(method = "addSpiritRecoveryData", at = @At("RETURN"), cancellable = true)
	private void hook_addSpiritRecoveryData(CallbackInfoReturnable<CompoundTag> ci) {
		CompoundTag t = ci.getReturnValue();
		t.putFloat("recoveryDiscount", this.getDiscount());
		ci.setReturnValue(t);
	}

	@OnlyIn(Dist.CLIENT)
	@Inject(method = "load", at = @At("HEAD"), cancellable = false, remap = true)
	public void hook_load(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("recoveryDiscount")) {
			if (this.level instanceof ClientLevel) {
				this.clientSideDiscount = Optional.of(tag.getFloat("recoveryDiscount"));
			}
		}
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	@Inject(method = "recalculateCost", at = @At("HEAD"), cancellable = true, remap = false)
	public void hook_recalculateCost(CallbackInfo ci) {
		Level data = this.level;

		if (data instanceof ServerLevel serverLevel) {
			if (this.gameProfile != null) {
				PlayerSpiritRecoveryData datax = PlayerSpiritRecoveryData.get(serverLevel);

				final float discount = CasualSaveData.get(serverLevel, this.gameProfile.getId());
				final float multiplier = datax.getSpiritRecoveryMultiplier(this.gameProfile.getId()) * (1 - discount);

				this.recoveryCost.calculate(multiplier, this.playerLevel, this.items, this.inventorySnapshot,
						datax.getHeroDiscount(this.gameProfile.getId()), this.rescuedBonus);

				VaultCasualMod.LOGGER.info("recovery cost: {}", this.recoveryCost.getTotalCost());

				ci.cancel();
			}
		}
	}
}
