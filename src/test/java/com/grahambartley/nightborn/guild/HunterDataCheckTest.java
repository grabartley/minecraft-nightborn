package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HunterDataCheckTest {

  private static final Identifier STAKE = Identifier.of("nightborn", "stake");
  private static final Identifier CROSSBOW = Identifier.of("nightborn", "crossbow");
  private static final Identifier BOLT = Identifier.of("nightborn", "bolt");
  private static final Identifier GONE = Identifier.of("nightborn", "gone");
  private static final Identifier KIT = Identifier.of("nightborn", "kit");

  private static final Map<Identifier, HunterLook> EVERY_RIG =
      Map.of(
          Identifier.of("nightborn", "man"),
          new HunterLook(HunterRig.MALE, STAKE, 1),
          Identifier.of("nightborn", "woman"),
          new HunterLook(HunterRig.FEMALE, STAKE, 1),
          Identifier.of("nightborn", "master"),
          new HunterLook(HunterRig.GUILD_MASTER, STAKE, 1));

  private static final Identifier CRUCIFIX = Identifier.of("nightborn", "crucifix");

  private static HunterKit kit(final Identifier weapon, final Identifier bolt) {
    return kit(weapon, bolt, null);
  }

  private static HunterKit kit(
      final Identifier weapon, final Identifier bolt, final Identifier crucifix) {
    return new HunterKit(
        weapon, Optional.ofNullable(bolt), Optional.ofNullable(crucifix), false, 1);
  }

  static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of("good data", EVERY_RIG, Map.of(KIT, kit(STAKE, null)), List.of()),
        Arguments.of("a good crossbow kit", EVERY_RIG, Map.of(KIT, kit(CROSSBOW, BOLT)), List.of()),
        Arguments.of(
            "no looks or kits",
            Map.of(),
            Map.of(),
            List.of(
                "there is no male hunter look",
                "there is no female hunter look",
                "there is no guild_master hunter look",
                "there are no hunter kits")),
        Arguments.of(
            "a weapon that is not an item",
            EVERY_RIG,
            Map.of(KIT, kit(GONE, null)),
            List.of("nightborn:kit uses nightborn:gone, which is not an item")),
        Arguments.of(
            "a good crucifix kit", EVERY_RIG, Map.of(KIT, kit(STAKE, null, CRUCIFIX)), List.of()),
        Arguments.of(
            "a crucifix that wards nothing",
            EVERY_RIG,
            Map.of(KIT, kit(STAKE, null, BOLT)),
            List.of("nightborn:kit raises nightborn:bolt, which is not a crucifix")),
        Arguments.of(
            "a bolt with no crossbow",
            EVERY_RIG,
            Map.of(KIT, kit(STAKE, BOLT)),
            List.of("nightborn:kit has a bolt but nightborn:stake is not a crossbow")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("data")
  void raisesAProblemForEachThingADataPackGotWrong(
      final String name,
      final Map<Identifier, HunterLook> looks,
      final Map<Identifier, HunterKit> kits,
      final List<String> problems) {
    assertEquals(
        problems,
        HunterDataCheck.problems(
            looks,
            kits,
            Set.of(STAKE, CROSSBOW, BOLT, CRUCIFIX)::contains,
            CROSSBOW::equals,
            CRUCIFIX::equals));
  }
}
