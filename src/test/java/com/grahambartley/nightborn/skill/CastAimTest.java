package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CastAimTest {

  private static final Vec3d HERE = new Vec3d(10, 64, 10);
  private static final Vec3d LOOKING = new Vec3d(0, 0, 1);

  private static LivingEntity at(final Class<? extends LivingEntity> kind, final Vec3d pos) {
    final LivingEntity entity = mock(kind);
    when(entity.getPos()).thenReturn(pos);
    when(entity.getRotationVector()).thenReturn(LOOKING);
    return entity;
  }

  private static CastAim aim(final String kind, final LivingEntity other) {
    return switch (kind) {
      case "own" -> CastAim.OWN_TARGET;
      case "entity" -> CastAim.at(other);
      case "spot" -> CastAim.toward(new Vec3d(10, 64, 4));
      case "self" -> null;
      default -> throw new IllegalArgumentException(kind);
    };
  }

  @ParameterizedTest(name = "mob {0}, aim {1}, has target {2}: subject {3}")
  @CsvSource({
    "true, own, true, target",
    "true, own, false, none",
    "true, entity, true, other",
    "true, spot, true, none",
    "false, own, true, none",
    "false, entity, false, other"
  })
  @DisplayName(
      "a cast aimed at no one in particular falls on a mob's target, and an aim at an entity or a"
          + " spot replaces it")
  void subject(
      final boolean mob, final String kind, final boolean hasTarget, final String subject) {
    final LivingEntity target = at(LivingEntity.class, new Vec3d(10, 64, 20));
    final LivingEntity other = at(LivingEntity.class, new Vec3d(20, 64, 10));
    final LivingEntity caster = at(mob ? MobEntity.class : LivingEntity.class, HERE);
    if (mob) {
      when(((MobEntity) caster).getTarget()).thenReturn(hasTarget ? target : null);
    }
    final LivingEntity expected =
        switch (subject) {
          case "target" -> target;
          case "other" -> other;
          default -> null;
        };
    assertSame(expected, aim(kind, other).subject(caster));
  }

  @ParameterizedTest(name = "mob {0}, aim {1}, has target {2}: heading {3} {4} {5}")
  @CsvSource({
    "true, own, true, 0, 0, 10",
    "true, own, false, 0, 0, 1",
    "true, entity, true, 10, 0, 0",
    "true, spot, true, 0, 0, -6",
    "false, spot, true, 0, 0, 1",
    "true, self, true, 0, 0, 1"
  })
  @DisplayName("a mob leaps toward what it aims at, and a player toward where it looks")
  void heading(
      final boolean mob,
      final String kind,
      final boolean hasTarget,
      final double x,
      final double y,
      final double z) {
    final LivingEntity target = at(LivingEntity.class, new Vec3d(10, 64, 20));
    final LivingEntity other = at(LivingEntity.class, new Vec3d(20, 64, 10));
    final LivingEntity caster = at(mob ? MobEntity.class : LivingEntity.class, HERE);
    if (mob) {
      when(((MobEntity) caster).getTarget()).thenReturn(hasTarget ? target : null);
    }
    final CastAim aim = "self".equals(kind) ? CastAim.at(caster) : aim(kind, other);
    assertEquals(new Vec3d(x, y, z), aim.heading(caster));
  }

  @ParameterizedTest(name = "aim {0}: helps {1}")
  @CsvSource({"own, caster", "entity, other", "spot, caster", "self, caster"})
  @DisplayName(
      "a heal or ward aimed at an ally lands on that ally, and any other aim lands on the caster")
  void helped(final String kind, final String helped) {
    final LivingEntity other = at(LivingEntity.class, new Vec3d(20, 64, 10));
    final LivingEntity caster = at(MobEntity.class, HERE);
    final CastAim aim = "self".equals(kind) ? CastAim.at(caster) : aim(kind, other);
    assertSame("other".equals(helped) ? other : caster, aim.helped(caster));
  }
}
