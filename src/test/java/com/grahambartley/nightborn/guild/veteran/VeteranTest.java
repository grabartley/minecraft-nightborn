package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.guild.build.HunterBuild;
import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.grahambartley.nightborn.mob.combat.Presence;
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
import org.junit.jupiter.api.DisplayName;
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
            Optional.empty(),
            0.0F,
            Optional.empty(),
            Optional.empty(),
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
            Optional.empty(),
            0.0F,
            Optional.empty(),
            Optional.empty(),
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

  @ParameterizedTest(name = "{0} adds {1} to each bolt it looses")
  @CsvSource({"veteran, 4.0"})
  @DisplayName("a veteran ships the damage it adds to each bolt it looses at a vampire")
  void shipsItsOwnBoltDamage(final String name, final float damage) throws IOException {
    assertEquals(damage, shipped(name).boltDamage());
  }

  private static final Path BUILD =
      Path.of("src/main/resources/data/nightborn/nightborn/hunter_build/hunter.json");

  @ParameterizedTest(name = "{0} over a {1} hunter")
  @CsvSource({"veteran, master", "veteran, veteran", "veteran, hunter"})
  @DisplayName(
      "the shipped veteran thinks faster, sees further and reads more of the fight than any"
          + " ranked hunter, and chains one attack into the next")
  void outthinksEveryRank(final String name, final String rank) throws IOException {
    final Mind ranked =
        HunterBuild.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(BUILD)))
            .getOrThrow()
            .mind(Optional.of(Identifier.of("nightborn", rank)), Optional.empty());
    final Mind mind = shipped(name).mind(ranked);
    assertTrue(mind.thinkTicks() < ranked.thinkTicks());
    assertTrue(mind.sharpness().foresight() > ranked.sharpness().foresight());
    assertTrue(mind.sharpness().blur() <= ranked.sharpness().blur());
    assertTrue(mind.sharpness().reads().containsAll(ranked.sharpness().reads()));
    assertTrue(mind.sharpness().reads(Sharpness.Reads.TACTICS));
    assertTrue(mind.chains());
    assertEquals(ranked.disposition(), mind.disposition());
  }

  @ParameterizedTest(name = "think {0}")
  @CsvSource({"45", "30"})
  @DisplayName("a veteran with no mind of its own in data thinks as its rank does")
  void ranksMindByDefault(final int think) {
    final Mind ranked = new Mind(think, Sharpness.KEEN, Disposition.EVEN);
    assertEquals(
        ranked,
        new Veteran(
                Identifier.of("nightborn", "silver_sword"),
                Optional.empty(),
                1.0,
                0.0,
                List.of(),
                Optional.empty(),
                0.0F,
                Optional.empty(),
                Optional.empty(),
                Optional.empty())
            .mind(ranked));
  }

  @ParameterizedTest(name = "{0}: {1} within {2}")
  @CsvSource({"veteran, minecraft:resistance, 16"})
  @DisplayName("hunters fighting near the shipped veteran stand firmer for it")
  void leadsByPresence(final String name, final String effect, final float radius)
      throws IOException {
    final Presence presence = shipped(name).presence().orElseThrow();
    assertEquals(Identifier.of(effect), presence.effect());
    assertEquals(radius, presence.radius());
  }
}
