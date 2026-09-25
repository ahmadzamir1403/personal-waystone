package com.personalwaystone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ModComponents {
	public record AnchorData(ResourceKey<Level> dimension, BlockPos pos, float yaw) {
		public static final Codec<AnchorData> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(AnchorData::dimension),
				BlockPos.CODEC.fieldOf("pos").forGetter(AnchorData::pos),
				Codec.FLOAT.fieldOf("yaw").forGetter(AnchorData::yaw)
			).apply(instance, AnchorData::new)
		);
	}

	public static final DataComponentType<AnchorData> ANCHOR = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		PersonalWaystone.id("anchor"),
		DataComponentType.<AnchorData>builder()
			.persistent(AnchorData.CODEC)
			.networkSynchronized(ByteBufCodecs.fromCodec(AnchorData.CODEC))
			.build()
	);

	public static void init() {
		// static fields above register themselves
	}
}
