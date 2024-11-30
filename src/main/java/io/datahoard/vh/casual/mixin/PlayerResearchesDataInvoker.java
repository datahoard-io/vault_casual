package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.nbt.CompoundTag;

@Mixin(value = PlayerResearchesData.class, remap = false)
public interface PlayerResearchesDataInvoker {

	@Invoker("create")
	public static PlayerResearchesData create(CompoundTag tag) {
		throw new AssertionError();
	}

}
