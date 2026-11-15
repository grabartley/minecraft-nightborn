package com.grahambartley.nightborn.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gore.GoreParticles;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import com.grahambartley.nightborn.vomit.VomitEffects;
import java.util.stream.Stream;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModParticlesTest {

  static Stream<Arguments> particles() {
    return Stream.of(
        Arguments.of(GoreParticles.DROP, "blood_drop"),
        Arguments.of(GoreParticles.SPLAT, "blood_splat"),
        Arguments.of(VomitEffects.DROP, "vomit"),
        Arguments.of(VomitEffects.SPLASH, "vomit_splash"),
        Arguments.of(SmokeParticles.VAMPIRE_SMOKE, "vampire_smoke"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("particles")
  @DisplayName("each mod particle is registered under the mod's namespace and its own name")
  void registered(final SimpleParticleType type, final String path) {
    assertEquals(Identifier.of("nightborn", path), Registries.PARTICLE_TYPE.getId(type));
  }
}
