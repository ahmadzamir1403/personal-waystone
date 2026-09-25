package com.personalwaystone;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class PersonalWaystoneClient implements ClientModInitializer {
	private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = ResourceKey.create(
		Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("minecraft", "tools_and_utilities"));

	@Override
	public void onInitializeClient() {
		CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES)
			.register(output -> output.accept(new ItemStack(ModItems.PERSONAL_WAYSTONE)));
	}
}
