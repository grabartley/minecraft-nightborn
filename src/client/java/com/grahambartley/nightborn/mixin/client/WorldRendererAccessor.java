package com.grahambartley.nightborn.mixin.client;

import java.util.Map;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

  @Accessor("playingSongs")
  Map<BlockPos, SoundInstance> nightborn$playingSongs();
}
