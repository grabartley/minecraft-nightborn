package com.grahambartley.nightborn.particle;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModParticles {

  private ModParticles() {}

  public static SimpleParticleType simple(final String path) {
    return Registry.register(
        Registries.PARTICLE_TYPE,
        Identifier.of(Nightborn.MOD_ID, path),
        FabricParticleTypes.simple());
  }
}
