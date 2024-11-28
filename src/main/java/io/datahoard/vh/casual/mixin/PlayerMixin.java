package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.iface.GetRecoveryDiscount;
import io.datahoard.vh.casual.iface.SetRecoveryDiscount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(value = Player.class)
public abstract class PlayerMixin extends LivingEntity implements GetRecoveryDiscount, SetRecoveryDiscount {
	private static EntityDataAccessor<Float> RECOVERY_DISCOUNT = SynchedEntityData.defineId(Player.class,
			EntityDataSerializers.FLOAT);
	private static String RECOVERY_DISCOUNT_TAG = "recoveryDiscount";

	protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	protected void hook_defineSynchedData(CallbackInfo ci) {
		this.player().getEntityData().define(RECOVERY_DISCOUNT, 0F);
	}

	@Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	protected void load(CompoundTag nbt, CallbackInfo ci) {
		if (nbt.contains(RECOVERY_DISCOUNT_TAG, 5)) {
			final float value = nbt.getFloat(RECOVERY_DISCOUNT_TAG);
			this.player().getEntityData().set(RECOVERY_DISCOUNT, value);
			VaultCasualMod.LOGGER.info("got {}={}", RECOVERY_DISCOUNT_TAG, value);
		} else {
			VaultCasualMod.LOGGER.info("PlayerMixin.load missing {}", RECOVERY_DISCOUNT_TAG);
		}
	}

	@Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	protected void save(CompoundTag nbt, CallbackInfo ci) {
		nbt.putFloat(RECOVERY_DISCOUNT_TAG, this.getRecoveryDiscount());
	}

	private Player player() {
		if ((Object) this instanceof Player p) {
			return p;
		}
		throw new IllegalStateException("How did we get here??");
	}

	public float getRecoveryDiscount() {
		return this.player().getEntityData().get(RECOVERY_DISCOUNT);
	}

	public void setRecoveryDiscount(float amount) {
		if (this.player() instanceof ServerPlayer serverPlayer) {
			VaultCasualMod.LOGGER.info("PlayerMixin.setRecoveryDiscount({})", amount);
			serverPlayer.getEntityData().set(RECOVERY_DISCOUNT, amount);
		} else {
			throw new IllegalStateException("Don't call this from the client!");
		}
	}
}
