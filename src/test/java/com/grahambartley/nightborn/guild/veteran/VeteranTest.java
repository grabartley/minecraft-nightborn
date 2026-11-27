package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranTest {

  private static final Path VETERANS =
      Path.of("src/main/resources/data/nightborn/nightborn/veteran");

  static Veteran shipped(final String name) throws IOException {
    return Veteran.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(Files.readString(VETERANS.resolve(name + ".json"))))
        .getOrThrow();
  }

  @ParameterizedTest(name = "scale {0} of {1}: {2}")
  @CsvSource({"2.0, 24.0, 48.0", "1.0, 24.0, 24.0", "1.5, 20.0, 30.0"})
  void hasItsScaleOfAHuntersHealth(final double scale, final double hunter, final double health) {
    final Veteran veteran =
        new Veteran(
            Identifier.of("nightborn", "sword"),
            Optional.empty(),
            scale,
            0.0,
            List.of(),
            Optional.empty());
    assertEquals(health, veteran.health(hunter), 1.0E-9);
  }

  @ParameterizedTest(name = "crucifix {0}")
  @CsvSource({"true", "false"})
  void carriesItsWeaponAsAMeleeKitThatIsNeverPicked(final boolean crucifix) {
    final Veteran veteran =
        new Veteran(
            Identifier.of("nightborn", "sword"),
            crucifix ? Optional.of(Identifier.of("nightborn", "cross")) : Optional.empty(),
            2.0,
            0.0,
            List.of(),
            Optional.empty());
    assertEquals(Identifier.of("nightborn", "sword"), veteran.kit().weapon());
    assertEquals(crucifix, veteran.kit().crucifix().isPresent());
    assertFalse(veteran.kit().ranged());
    assertFalse(veteran.kit().holyWater());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"veteran"})
  void shipsTwiceAHuntersHealthWithSilverAndAllFourAttacks(final String name) throws IOException {
    final Veteran veteran = shipped(name);
    assertEquals(2.0, veteran.healthScale());
    assertEquals(Identifier.of("nightborn", "silver_sword"), veteran.weapon());
    assertTrue(Registries.ITEM.containsId(veteran.weapon()));
    final Set<VeteranAttack.Kind> kinds = EnumSet.noneOf(VeteranAttack.Kind.class);
    for (final Identifier attack : veteran.attacks()) {
      kinds.add(VeteranAttackTest.shipped(attack.getPath()).kind());
    }
    assertEquals(EnumSet.allOf(VeteranAttack.Kind.class), kinds);
  }

  @ParameterizedTest(name = "{0} is a hunter of rank {1}")
  @CsvSource({"veteran, veteran"})
  void shipsAsAHunterOfItsRank(final String name, final String rank) throws IOException {
    assertEquals(Optional.of(Identifier.of("nightborn", rank)), shipped(name).rank());
  }
}
