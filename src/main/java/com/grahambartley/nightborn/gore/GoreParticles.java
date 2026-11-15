package com.grahambartley.nightborn.gore;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class GoreParticles {

  public static final SimpleParticleType DROP = particle("blood_drop");
  public static final SimpleParticleType SPLAT = particle("blood_splat");

  private GoreParticles() {}

  public static void register() {}

  private static SimpleParticleType particle(final String path) {
    return Registry.register(
        Registries.PARTICLE_TYPE,
        Identifier.of(Nightborn.MOD_ID, path),
        FabricParticleTypes.simple());
  }
}
