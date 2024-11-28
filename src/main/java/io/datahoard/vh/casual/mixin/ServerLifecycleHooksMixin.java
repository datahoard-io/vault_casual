package io.datahoard.vh.casual.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraftforge.network.ConnectionType;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mixin(value = ServerLifecycleHooks.class, remap = false)
public abstract class ServerLifecycleHooksMixin {
	@Shadow
	private static final Logger LOGGER = LogManager.getLogger();

	@Shadow
	private static final Marker SERVERHOOKS = MarkerManager.getMarker("SERVERHOOKS");

	@Inject(method = "rejectConnection", at = @At("HEAD"), cancellable = true, remap = false)
	private static void hook_rejectConnection(final Connection manager, ConnectionType type, String message,
			CallbackInfo ci) {
		manager.setProtocol(ConnectionProtocol.LOGIN);
		String ip = "local";
		if (manager.getRemoteAddress() != null) {
			ip = manager.getRemoteAddress().toString();
		}
		LOGGER.info(SERVERHOOKS, "[{}] Disconnecting {} connection attempt: {}", ip, type, message);
		TextComponent text = new TextComponent(message);
		manager.send(new ClientboundLoginDisconnectPacket(text));
		manager.disconnect(text);
		ci.cancel();
	}
}
