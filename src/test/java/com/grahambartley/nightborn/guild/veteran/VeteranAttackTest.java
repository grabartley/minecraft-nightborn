package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranAttackTest {

  static final Path ATTACKS = Path.of("src/main/resources/data/nightborn/nightborn/veteran_attack");

  static VeteranAttack shipped(final String name) throws IOException {
    return VeteranAttack.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(Files.readString(ATTACKS.resolve(name + ".json"))))
        .getOrThrow();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "silver_chain, SILVER_CHAIN, 20, 300, 0.0, 12.0, 1, 0.0, 60, 0.0",
    "bolt_volley, BOLT_VOLLEY, 30, 240, 8.0, 20.0, 3, 0.0, 0, 0.0",
    "holy_water_arc, HOLY_WATER_ARC, 20, 400, 0.0, 10.0, 3, 1.0, 120, 0.0",
    "rallying_cry, RALLYING_CRY, 20, 900, 0.0, 0.0, 1, 16.0, 160, 6.0"
  })
  void shipsTheLockedAttack(
      final String name,
      final VeteranAttack.Kind kind,
      final int windup,
      final int cooldown,
      final double reachMin,
      final double reach,
      final int count,
      final double radius,
      final int effectTicks,
      final float heal)
      throws IOException {
    final VeteranAttack attack = shipped(name);
    assertEquals(kind, attack.kind());
    assertEquals(windup, attack.windupTicks());
    assertEquals(cooldown, attack.cooldownTicks());
    assertEquals(reachMin, attack.reachMin());
    assertEquals(reach, attack.reach());
    assertEquals(count, attack.count());
    assertEquals(radius, attack.radius());
    assertEquals(effectTicks, attack.effectTicks());
    assertEquals(heal, attack.heal());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"silver_chain", "bolt_volley", "holy_water_arc", "rallying_cry"})
  void playsASoundAndMarksWithThingsTheGameHas(final String name) throws IOException {
    final VeteranAttack attack = shipped(name);
    assertTrue(Registries.SOUND_EVENT.containsId(attack.sound()), attack.sound().toString());
    assertTrue(Registries.PARTICLE_TYPE.containsId(attack.mark()), attack.mark().toString());
    attack.held().ifPresent(item -> assertTrue(Registries.ITEM.containsId(item)));
    assertTrue(!attack.clip().isEmpty());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"rallying_cry, true", "silver_chain, false"})
  void shoutsOnlyWhenTheDataSaysSo(final String name, final boolean shout) throws IOException {
    assertEquals(shout, shipped(name).shout());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"silver_chain"})
  void fillsInDefaultsLeftOut(final String kind) {
    final VeteranAttack attack =
        VeteranAttack.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"kind\":\""
                        + kind
                        + "\",\"clip\":\"c\",\"sound\":\"a\",\"mark\":\"b\","
                        + "\"windup_ticks\":5,\"cooldown_ticks\":5}"))
            .getOrThrow();
    assertEquals(1, attack.count());
    assertEquals(0.0, attack.reach());
    assertEquals(Optional.empty(), attack.held());
    assertEquals(Identifier.ofVanilla("a"), attack.sound());
  }
}
