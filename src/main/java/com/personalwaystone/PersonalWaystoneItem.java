package com.personalwaystone;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class PersonalWaystoneItem extends Item {
	private static ModConfig config() {
		return PersonalWaystone.CONFIG;
	}

	public PersonalWaystoneItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		if (player == null || !player.isShiftKeyDown() || world.isClientSide()) {
			return InteractionResult.PASS;
		}

		// Sneak + use on a block = bind the spot above it
		ItemStack stack = context.getItemInHand();
		BlockPos pos = context.getClickedPos().above();
		stack.set(ModComponents.ANCHOR, new ModComponents.AnchorData(world.dimension(), pos, player.getYRot()));

		player.sendOverlayMessage(Component.translatable("item.personalwaystone.personal_waystone.bound",
			pos.getX(), pos.getY(), pos.getZ()));
		world.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.isShiftKeyDown()) {
			// Sneak + use in air = unbind
			if (stack.has(ModComponents.ANCHOR)) {
				stack.remove(ModComponents.ANCHOR);
				player.sendOverlayMessage(Component.translatable("item.personalwaystone.personal_waystone.unbound"));
			}
			return InteractionResult.SUCCESS;
		}

		if (world.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ModComponents.AnchorData anchor = stack.get(ModComponents.ANCHOR);
		if (anchor == null) {
			reject(player, "item.personalwaystone.personal_waystone.not_bound");
			return InteractionResult.CONSUME;
		}

		ResourceKey<Level> from = player.level().dimension();
		if (from == anchor.dimension() && player.blockPosition().distSqr(anchor.pos()) <= 4 * 4) {
			player.sendOverlayMessage(Component.translatable("item.personalwaystone.personal_waystone.already_here"));
			return InteractionResult.CONSUME;
		}

		if (from != anchor.dimension() && !config().allowCrossDimension) {
			reject(player, "item.personalwaystone.personal_waystone.no_cross_dim");
			return InteractionResult.CONSUME;
		}

		ServerLevel overworld = world.getServer().overworld();
		CooldownState cooldowns = CooldownState.get(overworld);
		long cooldownTicks = config().cooldownSeconds * 20L;
		long elapsed = world.getGameTime() - cooldowns.get(player.getUUID());
		if (!player.isCreative() && elapsed < cooldownTicks) {
			player.sendOverlayMessage(Component.translatable("item.personalwaystone.personal_waystone.cooldown",
				(cooldownTicks - elapsed + 19) / 20));
			player.playSound(SoundEvents.VILLAGER_NO);
			return InteractionResult.CONSUME;
		}

		ServerLevel targetWorld = world.getServer().getLevel(anchor.dimension());
		if (targetWorld == null) {
			reject(player, "item.personalwaystone.personal_waystone.no_cross_dim");
			return InteractionResult.CONSUME;
		}

		teleport((ServerPlayer) player, targetWorld, anchor);
		cooldowns.set(player.getUUID(), world.getGameTime());
		return InteractionResult.CONSUME;
	}

	private static void reject(Player player, String key) {
		player.sendOverlayMessage(Component.translatable(key));
		player.playSound(SoundEvents.VILLAGER_NO);
	}

	private static void teleport(ServerPlayer player, ServerLevel target, ModComponents.AnchorData anchor) {
		player.playSound(SoundEvents.ENDERMAN_TELEPORT);

		Vec3 spot = findSafeSpot(target, anchor.pos());
		player.teleportTo(target, spot.x, spot.y, spot.z, java.util.Set.of(), anchor.yaw(), player.getXRot(), false);

		target.playSound(null, BlockPos.containing(spot), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
		player.sendOverlayMessage(Component.translatable("item.personalwaystone.personal_waystone.teleported"));
	}

	/** Anchor position, nudged up/down to 2 air blocks over solid ground. */
	private static Vec3 findSafeSpot(ServerLevel world, BlockPos anchor) {
		BlockPos.MutableBlockPos cursor = anchor.mutable();
		for (int dy : new int[]{0, 1, 2, 3, -1, -2}) {
			cursor.set(anchor.getX(), anchor.getY() + dy, anchor.getZ());
			if (world.getBlockState(cursor).isSolid()
				&& world.getBlockState(cursor.above()).getCollisionShape(world, cursor).isEmpty()
				&& world.getBlockState(cursor.above(2)).getCollisionShape(world, cursor).isEmpty()) {
				return new Vec3(cursor.getX() + 0.5, cursor.getY() + 1, cursor.getZ() + 0.5);
			}
		}
		return new Vec3(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
								Consumer<Component> tooltip, TooltipFlag flag) {
		ModComponents.AnchorData anchor = stack.get(ModComponents.ANCHOR);
		if (anchor != null) {
			tooltip.accept(Component.translatable("item.personalwaystone.personal_waystone.tooltip_bound",
				anchor.pos().getX(), anchor.pos().getY(), anchor.pos().getZ()).withStyle(ChatFormatting.AQUA));
		} else {
			tooltip.accept(Component.translatable("item.personalwaystone.personal_waystone.tooltip_unbound")
				.withStyle(ChatFormatting.GRAY));
		}
	}
}
