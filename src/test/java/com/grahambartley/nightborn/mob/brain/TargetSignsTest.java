package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TargetSignsTest {

  @ParameterizedTest(name = "target at {0},{1} moved {2},{3}: running {4}")
  @CsvSource({
    "0, 10, 0, 0.2, true",
    "0, 10, 0, 0.15, true",
    "0, 10, 0, 0.1, false",
    "0, 10, 0, -0.3, false",
    "0, 10, 0.3, 0, false",
    "10, 10, 0.2, 0.2, true",
    "0, 0, 0.3, 0, false"
  })
  @DisplayName("a target is running when it moves away from the mob at a run")
  void running(
      final double x, final double z, final double dx, final double dz, final boolean running) {
    assertEquals(
        running,
        TargetSigns.running(new Vec3d(0, 64, 0), new Vec3d(x, 64, z), new Vec3d(dx, 0.0, dz)));
  }

  @ParameterizedTest(name = "ground {0}, fluid {1}, climbing {2}, riding {3}: airborne {4}")
  @CsvSource({
    "false, false, false, false, true",
    "true, false, false, false, false",
    "false, true, false, false, false",
    "false, false, true, false, false",
    "false, false, false, true, false"
  })
  @DisplayName("a target is in the air when it stands, swims, climbs and rides on nothing")
  void airborne(
      final boolean ground,
      final boolean fluid,
      final boolean climbing,
      final boolean riding,
      final boolean airborne) {
    assertEquals(airborne, TargetSigns.airborne(ground, fluid, climbing, riding));
  }

  @ParameterizedTest(name = "hunts the mob {0}, struck by it {1}, struck it {2}: aware {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true",
    "true, true, true, true"
  })
  @DisplayName("prey is unaware of the mob until it turns on it, is struck by it or strikes it")
  void aware(
      final boolean hunts, final boolean struckBy, final boolean struck, final boolean aware) {
    assertEquals(aware, TargetSigns.aware(hunts, struckBy, struck));
  }

  @ParameterizedTest(name = "mending {0}, regenerating {1}, taking a cure {2}: healing {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true",
    "true, true, true, true"
  })
  @DisplayName("a foe heals while its blood mends it, it regenerates, or it eats or drinks")
  void healing(
      final boolean mending,
      final boolean regenerating,
      final boolean cure,
      final boolean healing) {
    assertEquals(healing, TargetSigns.healing(mending, regenerating, cure));
  }

  @ParameterizedTest(name = "slowed {0}, mesmerised {1}: held {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a body is held while it is slowed, pinned, staggered or mesmerised")
  void held(final boolean slowed, final boolean mesmerised, final boolean held) {
    assertEquals(held, TargetSigns.held(slowed, mesmerised));
  }

  @ParameterizedTest(name = "{0}: mends {1}")
  @CsvSource({
    "minecraft:cooked_beef, true",
    "minecraft:golden_apple, true",
    "minecraft:potion, true",
    "minecraft:milk_bucket, true",
    "minecraft:bow, false",
    "minecraft:shield, false",
    "minecraft:stone, false"
  })
  @DisplayName("eating food or drinking counts as taking a cure, and drawing or blocking does not")
  void mends(final String item, final boolean mends) {
    assertEquals(mends, TargetSigns.mends(new ItemStack(Registries.ITEM.get(Identifier.of(item)))));
  }

  @ParameterizedTest(name = "{0}: ranged {1}")
  @CsvSource({
    "minecraft:bow, true",
    "minecraft:crossbow, true",
    "minecraft:trident, true",
    "minecraft:iron_sword, false",
    "minecraft:air, false"
  })
  @DisplayName("a foe holding a bow, crossbow or trident fights at range")
  void ranged(final String item, final boolean ranged) {
    assertEquals(
        ranged, TargetSigns.ranged(new ItemStack(Registries.ITEM.get(Identifier.of(item)))));
  }
}
