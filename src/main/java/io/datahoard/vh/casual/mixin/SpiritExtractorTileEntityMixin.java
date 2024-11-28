package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.config.CasualSaveData;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import iskallia.vault.world.data.InventorySnapshot;
import iskallia.vault.world.data.PlayerSpiritRecoveryData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = SpiritExtractorTileEntity.class, remap = false)
public abstract class SpiritExtractorTileEntityMixin extends BlockEntity {

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

	@Inject(method = "recalculateCost", at = @At("HEAD"), cancellable = true, remap = false)
	public void hook_recalculateCost(CallbackInfo ci) {
		Level data = this.level;

		if (data instanceof ServerLevel serverLevel) {
			if (this.gameProfile != null) {

				PlayerSpiritRecoveryData datax = PlayerSpiritRecoveryData.get(serverLevel);

				float multiplier = datax.getSpiritRecoveryMultiplier(this.gameProfile.getId());

				multiplier *= (1 - CasualSaveData.get(serverLevel, this.gameProfile.getId()));


				this.recoveryCost.calculate(multiplier, this.playerLevel, this.items, this.inventorySnapshot,
						datax.getHeroDiscount(this.gameProfile.getId()), this.rescuedBonus);

				ci.cancel();
			}
		}
	}
}
