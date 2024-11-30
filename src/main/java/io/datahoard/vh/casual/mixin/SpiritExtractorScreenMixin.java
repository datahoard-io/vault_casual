package io.datahoard.vh.casual.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.iface.RecoveryDiscount;
import iskallia.vault.block.entity.SpiritExtractorTileEntity.RecoveryCost;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.screen.block.SpiritExtractorScreen;
import iskallia.vault.container.SpiritExtractorContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Mixin(value = SpiritExtractorScreen.class, remap = false)
public abstract class SpiritExtractorScreenMixin extends AbstractElementContainerScreen<SpiritExtractorContainer> {
	public SpiritExtractorScreenMixin(SpiritExtractorContainer container, Inventory inventory, Component title,
			IElementRenderer elementRenderer,
			ITooltipRendererFactory<AbstractElementContainerScreen<SpiritExtractorContainer>> tooltipRendererFactory) {
		super(container, inventory, title, elementRenderer, tooltipRendererFactory);
	}

	@OnlyIn(Dist.CLIENT)
	@Inject(method = "getPurchaseButtonTooltipLines", at = @At("RETURN"), cancellable = true)
	protected void hook_getPurchaseButtonTooltipLines(CallbackInfoReturnable<List<Component>> ci) {
		final RecoveryCost recoveryCost = this.getMenu().getRecoveryCost();
		if (recoveryCost instanceof RecoveryDiscount.Getter getter) {
			ArrayList<Component> rv = new ArrayList<Component>(ci.getReturnValue());
			rv.add(TextComponent.EMPTY);
			rv.add(new TextComponent("Casual discount ").withStyle(ChatFormatting.GREEN)
					.append(new TextComponent(String.format("%.0f", 100 * getter.getRecoveryDiscount()))
							.withStyle(ChatFormatting.AQUA))
					.append(new TextComponent("%").withStyle(ChatFormatting.GREEN)));
			ci.setReturnValue(rv);
		} else {
			VaultCasualMod.LOGGER.warn("cannot getRecoveryDiscount in SpiritExtractorScreenMixin");
		}
	}
}
