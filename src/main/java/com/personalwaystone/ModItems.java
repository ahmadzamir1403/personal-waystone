package com.personalwaystone;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ModItems {
	private static final ResourceKey<Item> PERSONAL_WAYSTONE_KEY =
		ResourceKey.create(Registries.ITEM, PersonalWaystone.id("personal_waystone"));

	public static final Item PERSONAL_WAYSTONE = Registry.register(
		BuiltInRegistries.ITEM,
		PERSONAL_WAYSTONE_KEY,
		new PersonalWaystoneItem(new Item.Properties()
			.stacksTo(1)
			.rarity(Rarity.EPIC)
			.setId(PERSONAL_WAYSTONE_KEY))
	);

	public static void init() {
		// static fields above register themselves
	}
}
