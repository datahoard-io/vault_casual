package io.datahoard.vh.casual.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.iface.WithDiscount;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import iskallia.vault.container.SpiritExtractorContainer;
import iskallia.vault.container.oversized.OverSizedSlotContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

@Mixin(value = SpiritExtractorContainer.class, remap = false)
public abstract class SpiritExtractorContainerMixin extends OverSizedSlotContainer implements WithDiscount {

	protected SpiritExtractorContainerMixin(MenuType<?> menuType, int id, Player player) {
		super(menuType, id, player);
	}

	@Shadow
	private final SpiritExtractorTileEntity tileEntity = null;

	public float getDiscount() {
		if (tileEntity instanceof WithDiscount wd) {
			return wd.getDiscount();
		}
		VaultCasualMod.LOGGER.warn("cannot getDiscount in SpiritExtractorContainerMixin");
		return 0;
	}
}
