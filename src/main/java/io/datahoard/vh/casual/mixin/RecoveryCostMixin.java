package io.datahoard.vh.casual.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.datahoard.vh.casual.iface.GetRecoveryDiscount;
import io.datahoard.vh.casual.iface.SetRecoveryDiscount;
import iskallia.vault.block.entity.SpiritExtractorTileEntity.RecoveryCost;
import iskallia.vault.config.SpiritConfig.LevelCost;
import iskallia.vault.container.oversized.OverSizedItemStack;
import iskallia.vault.world.data.InventorySnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

@Mixin(value = RecoveryCost.class, remap = false)
public abstract class RecoveryCostMixin implements SetRecoveryDiscount, GetRecoveryDiscount {
	@Shadow
	private OverSizedItemStack totalCost = OverSizedItemStack.EMPTY;

	@Shadow
	private float baseCount = 0.0F;

	@Shadow
	private List<Tuple<ItemStack, Integer>> stackCost = new ArrayList<>();

	@Shadow
	private Optional<LevelCost> getLevelCost(int vaultLevel) {
		return Optional.empty();
	}

	@Shadow
	private int getItemsCost(LevelCost cost, List<ItemStack> items, @Nullable InventorySnapshot inventorySnapshot) {
		return 0;
	}

	private Optional<Float> recoveryDiscount = Optional.empty();

	public float getRecoveryDiscount() {
		if (this.recoveryDiscount.isEmpty()) {
			return 0;
		}
		return this.recoveryDiscount.get();
	}

	public void setRecoveryDiscount(float value) {
		this.recoveryDiscount = Optional.of(value);
	}

	@Inject(method = "serialize", at = @At("TAIL"), cancellable = true, remap = false)
	public void hook_serialize(CallbackInfoReturnable<CompoundTag> ci) {
		this.recoveryDiscount.ifPresent((value) -> {
			CompoundTag tag = ci.getReturnValue();
			tag.putFloat("recoveryDiscount", value);
			ci.setReturnValue(tag);
		});
	}

	@Inject(method = "deserialize", at = @At("HEAD"), remap = false)
	public void hook_deserialize(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("recoveryDiscount")) {
			this.recoveryDiscount = Optional.of(tag.getFloat("recoveryDiscount"));
		}
	}

	@Inject(method = "calculate", at = @At("HEAD"), cancellable = true, remap = false)
	public void hook_calculate(float multiplier, int vaultLevel, List<ItemStack> items,
			@Nullable InventorySnapshot inventorySnapshot, float heroDiscount, float rescuedBonus, CallbackInfo ci) {
		if (this.recoveryDiscount.isEmpty()) {
			return;
		}

		final float discount = this.recoveryDiscount.get().floatValue();

		this.getLevelCost(vaultLevel).ifPresent(cost -> {
			this.baseCount = cost.count;
			int totalCost = (int) Math.ceil((double) (cost.count * (float) Math.max(1, vaultLevel) + 1.0F));
			this.stackCost.clear();
			totalCost += this.getItemsCost(cost, items, inventorySnapshot);
			totalCost = (int) ((float) totalCost * multiplier * (1.0F - heroDiscount) * (1.0F - rescuedBonus));
			totalCost *= (1 - discount);
			totalCost = Math.max(1, totalCost);
			this.totalCost = new OverSizedItemStack(new ItemStack(cost.item, totalCost), totalCost);
		});

		ci.cancel();
	}
}
