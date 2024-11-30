package io.datahoard.vh.casual;

import org.slf4j.Logger;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;

import io.datahoard.vh.casual.command.CasualCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("vault_casual")
public class VaultCasualMod {
	// Directly reference a slf4j logger
	public static final Logger LOGGER = LogUtils.getLogger();
	public static CasualCommand CASUAL_COMMAND;

	public VaultCasualMod() {
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, this::onCommandRegister);
	}

	private void onCommandRegister(RegisterCommandsEvent event) {
		CASUAL_COMMAND = new CasualCommand();

		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("casual");
		CASUAL_COMMAND.build(builder);
		event.getDispatcher().register(builder);
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		// seems customary to leave this in
		LOGGER.info("HELLO from server starting");
	}
}
