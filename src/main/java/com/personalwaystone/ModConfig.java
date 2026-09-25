package com.personalwaystone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public int cooldownSeconds = 60;
	public boolean allowCrossDimension = true;
	public int channelTicks = 0;

	public static ModConfig load() {
		Path path = Path.of("config", PersonalWaystone.MOD_ID + ".json");
		try {
			if (Files.exists(path)) {
				return GSON.fromJson(Files.readString(path), ModConfig.class);
			}
			ModConfig config = new ModConfig();
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(config));
			return config;
		} catch (IOException e) {
			PersonalWaystone.LOGGER.warn("Failed to load config, using defaults", e);
			return new ModConfig();
		}
	}
}
