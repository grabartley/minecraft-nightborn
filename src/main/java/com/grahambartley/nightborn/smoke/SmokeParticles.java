package com.grahambartley.nightborn.smoke;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class SmokeParticles {

  public static final SimpleParticleType VAMPIRE_SMOKE =
      Registry.register(
          Registries.PARTICLE_TYPE,
          Identifier.of(Nightborn.MOD_ID, "vampire_smoke"),
          FabricParticleTypes.simple());

  private SmokeParticles() {}

  public static void register() {}
}
