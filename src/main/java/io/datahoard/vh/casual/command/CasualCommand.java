package io.datahoard.vh.casual.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.datahoard.vh.casual.VaultCasualMod;
import io.datahoard.vh.casual.config.CasualSaveData;
import iskallia.vault.entity.entity.SpiritEntity;
import iskallia.vault.world.data.InventorySnapshot;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity.RemovalReason;

public class CasualCommand {
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands.literal("revive").executes(this::revive));
		builder.then(Commands.literal("discount").then(Commands.literal("get").executes(this::getPercent)));
		builder.then(Commands.literal("discount").then(Commands.literal("set")
				.then(Commands.argument("percent", IntegerArgumentType.integer(0, 100)).executes(this::setPercent))));
	}

	private int revive(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerLevel level = ctx.getSource().getLevel();
		ServerPlayer player = ctx.getSource().getPlayerOrException();

		VaultCasualMod.LOGGER.info("casual revive {}", player.getName());

		if (player.getPassengers().get(0) instanceof SpiritEntity spirit) {
			if (level.isClientSide()) {
				return 0;
			}

			VaultCasualMod.LOGGER.info("processing spirit (server side)");

			GameProfile profile = spirit.getGameProfile().get();

			VaultCasualMod.LOGGER.info("spirit profile {}", profile.getId());

			if (!profile.getId().equals(player.getUUID())) {
				player.sendMessage(new TextComponent("This is not your spirit.").withStyle(ChatFormatting.RED),
						player.getUUID());
				return 0;
			}

			player.sendMessage(new TextComponent("Reviving spirit.").withStyle(ChatFormatting.GREEN), player.getUUID());

			VaultCasualMod.LOGGER.info("reviving spirit {}", spirit);

			InventorySnapshot inventory = spirit.getInventorySnapshot();

			if (inventory == null) {
				player.sendMessage(
						new TextComponent("This spirit does not contain items.").withStyle(ChatFormatting.RED),
						player.getUUID());
				return 0;
			}

			VaultCasualMod.LOGGER.info("applying inventory {} {}", inventory.getClass(), inventory);

			inventory.apply(player);

			VaultCasualMod.LOGGER.info("removing spirit");

			spirit.remove(RemovalReason.DISCARDED);
			return 0;
		}

		player.sendMessage(new TextComponent("You need to hold your spirit.").withStyle(ChatFormatting.RED),
				player.getUUID());

		return 0;
	}

	private int getPercent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerLevel level = ctx.getSource().getLevel();
		if (level.isClientSide()) {
			return 0;
		}

		ServerPlayer player = ctx.getSource().getPlayerOrException();

		final Float percent = CasualSaveData.get(level, player) * 100;

		player.sendMessage(new TextComponent("Your current discount percentage is ").withStyle(ChatFormatting.GREEN)
				.append(new TextComponent(Integer.valueOf(percent.intValue()).toString())
						.withStyle(ChatFormatting.AQUA))
				.append(new TextComponent("%").withStyle(ChatFormatting.GREEN)), player.getUUID());

		return 0;
	}

	private int setPercent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerLevel level = ctx.getSource().getLevel();
		if (level.isClientSide()) {
			return 0;
		}

		final Integer percent = IntegerArgumentType.getInteger(ctx, "percent");
		ServerPlayer player = ctx.getSource().getPlayerOrException();

		CasualSaveData.set(level, player, percent.floatValue() / 100);

		return 0;
	}
}
