package com.personalwaystone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-world (overworld) storage of each player's last teleport game time. */
public class CooldownState extends SavedData {
	private final Map<UUID, Long> lastTeleport = new HashMap<>();

	public static final Codec<CooldownState> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.unboundedMap(UUIDUtil.CODEC, Codec.LONG)
				.fieldOf("cooldowns")
				.forGetter(state -> state.lastTeleport)
		).apply(instance, CooldownState::new)
	);

	public static final SavedDataType<CooldownState> TYPE = new SavedDataType<>(
		PersonalWaystone.id("cooldowns"), CooldownState::new, CODEC, null);

	private CooldownState() {
	}

	private CooldownState(Map<UUID, Long> lastTeleport) {
		this.lastTeleport.putAll(lastTeleport);
	}

	public static CooldownState get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	public long get(UUID player) {
		return lastTeleport.getOrDefault(player, Long.MIN_VALUE / 2);
	}

	public void set(UUID player, long gameTime) {
		lastTeleport.put(player, gameTime);
		setDirty();
	}
}
