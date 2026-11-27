package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.Aim;
import com.grahambartley.nightborn.skill.CastAim;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CastAimsTest {

  private static final Vec3d END = new Vec3d(4.5, 64, 9.5);

  @ParameterizedTest(name = "{0}, route {1}: {2}")
  @CsvSource({
    "TARGET, true, own",
    "TARGET, false, own",
    "SELF, false, self",
    "SPOT, true, end",
    "SPOT, false, none",
    "ALLY, true, own",
    "ALLIES, true, own",
    "FOES, true, own"
  })
  @DisplayName(
      "a mob casts at its target, at itself, or toward the end of its route, and does not cast"
          + " at a spot when it has no route")
  void resolve(final Aim aim, final boolean route, final String cast) {
    final LivingEntity self = mock(LivingEntity.class);
    final Optional<CastAim> expected =
        switch (cast) {
          case "own" -> Optional.of(CastAim.OWN_TARGET);
          case "self" -> Optional.of(CastAim.at(self));
          case "end" -> Optional.of(CastAim.toward(END));
          default -> Optional.empty();
        };
    assertEquals(expected, CastAims.resolve(aim, self, route ? END : null));
  }
}
