package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.crypt.CryptStair;
import com.grahambartley.nightborn.tools.structures.vampire.lair.Grotto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class VampireStructuresTest {

  @Test
  void buildsEveryVampirePieceOnce() {
    assertEquals(
        List.of(
            "vampire/crypt/undercroft",
            "vampire/crypt/ossuary",
            "vampire/crypt/church_tower",
            "vampire/crypt/church_bellcote",
            "vampire/lair/den",
            "vampire/lair/grotto",
            "vampire/castle/bastions",
            "vampire/castle/ruined",
            "vampire/manor/turret",
            "vampire/manor/tower",
            "vampire/mountain_castle/eyrie",
            "vampire/mountain_castle/bastion"),
        VampireStructures.build().stream().map(Template::path).toList());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "vampire/crypt/undercroft",
        "vampire/crypt/ossuary",
        "vampire/lair/den",
        "vampire/lair/grotto",
        "vampire/castle/bastions",
        "vampire/castle/ruined",
        "vampire/manor/turret",
        "vampire/manor/tower"
      })
  void everyVampireHomeHoldsOneLoadedGramophone(final String path) {
    final Template home =
        VampireStructures.build().stream()
            .filter(t -> t.path().equals(path))
            .findFirst()
            .orElseThrow();
    final List<Pos> gramophones =
        home.cells().entrySet().stream()
            .filter(cell -> cell.getValue().startsWith(Furnishing.GRAMOPHONE + "["))
            .map(Map.Entry::getKey)
            .toList();
    assertEquals(1, gramophones.size());
    assertTrue(home.blockData().get(gramophones.getFirst()).containsKey("RecordItem"));
    final Pos below = gramophones.getFirst();
    assertNotEquals(
        "minecraft:air", home.get(below.x(), below.y() - 1, below.z()), "it stands on something");
  }

  @ParameterizedTest(name = "{0} has one {1} in its outer edge with open air above it")
  @CsvSource({
    "vampire/crypt/church_tower, nightborn:crypt_entrance",
    "vampire/crypt/church_bellcote, nightborn:crypt_entrance",
    "vampire/castle/bastions, nightborn:castle_entrance",
    "vampire/castle/ruined, nightborn:castle_entrance",
    "vampire/manor/turret, nightborn:manor_entrance",
    "vampire/manor/tower, nightborn:manor_entrance",
    "vampire/mountain_castle/eyrie, nightborn:mountain_castle_entrance",
    "vampire/mountain_castle/bastion, nightborn:mountain_castle_entrance"
  })
  void everySurfaceHomeMarksItsEntrance(final String path, final String name) {
    final Template home =
        VampireStructures.build().stream()
            .filter(t -> t.path().equals(path))
            .findFirst()
            .orElseThrow();
    final List<Pos> entrances =
        home.blockData().entrySet().stream()
            .filter(data -> name.equals(data.getValue().get("name")))
            .map(Map.Entry::getKey)
            .toList();
    assertEquals(1, entrances.size());
    final Pos floor = entrances.getFirst();
    assertEquals(
        "minecraft:jigsaw[orientation=south_up]", home.get(floor.x(), floor.y(), floor.z()));
    assertTrue(floor.z() >= home.sizeZ() - 2, "entrance at z " + floor.z());
    assertTrue(StructureWalk.open(home.get(floor.x(), floor.y() + 1, floor.z())));
    assertTrue(StructureWalk.open(home.get(floor.x(), floor.y() + 2, floor.z())));
    assertNotEquals("minecraft:air", home.blockData().get(floor).get("final_state"));
  }

  static List<Arguments> fontWalks() {
    final Pos stairFoot =
        new Pos(
            CryptStair.OFFSET_X + CryptStair.FOOT_X, 1, CryptStair.OFFSET_Z + CryptStair.STAIR_Z);
    return List.of(
        Arguments.of("vampire/crypt/undercroft", stairFoot),
        Arguments.of("vampire/crypt/ossuary", stairFoot),
        Arguments.of("vampire/crypt/church_tower", null),
        Arguments.of("vampire/crypt/church_bellcote", null),
        Arguments.of("vampire/lair/den", new Pos(5, 1, 16)),
        Arguments.of("vampire/lair/grotto", new Pos(Grotto.LEDGE - 2, 1, Grotto.SPRING_Z)),
        Arguments.of("vampire/castle/bastions", null),
        Arguments.of("vampire/castle/ruined", null),
        Arguments.of("vampire/manor/turret", null),
        Arguments.of("vampire/manor/tower", null),
        Arguments.of("vampire/mountain_castle/eyrie", null),
        Arguments.of("vampire/mountain_castle/bastion", null));
  }

  @ParameterizedTest(name = "{0} keeps one full old blood font a visitor can walk up to")
  @MethodSource("fontWalks")
  void everyVampireHomeKeepsOneOldBloodFont(final String path, final Pos start) {
    final Template home =
        VampireStructures.build().stream()
            .filter(t -> t.path().equals(path))
            .findFirst()
            .orElseThrow();
    final List<Pos> fonts =
        home.cells().entrySet().stream()
            .filter(cell -> cell.getValue().startsWith(Furnishing.OLD_BLOOD_FONT + "["))
            .map(Map.Entry::getKey)
            .toList();
    assertEquals(1, fonts.size());
    final Pos font = fonts.getFirst();
    assertEquals("nightborn:old_blood_font[level=full]", home.get(font.x(), font.y(), font.z()));
    assertTrue(
        !StructureWalk.open(home.get(font.x(), font.y() - 1, font.z())),
        "it stands on " + home.get(font.x(), font.y() - 1, font.z()));
    assertEquals("minecraft:air", home.get(font.x(), font.y() + 1, font.z()));
    final Pos from = start != null ? start : entrance(home).offset(0, 1, 0);
    assertTrue(
        StructureWalk.beside(StructureWalk.from(home, from), font.x(), font.y(), font.z()),
        "font at " + font + " from " + from);
  }

  static Stream<Arguments> walks() {
    final Pos stairFoot =
        new Pos(
            CryptStair.OFFSET_X + CryptStair.FOOT_X, 1, CryptStair.OFFSET_Z + CryptStair.STAIR_Z);
    return VampireStructures.build().stream()
        .map(
            home ->
                Arguments.of(
                    home.path(),
                    switch (home.path()) {
                      case "vampire/crypt/undercroft", "vampire/crypt/ossuary" -> stairFoot;
                      case "vampire/lair/den" -> new Pos(5, 1, 16);
                      case "vampire/lair/grotto" -> new Pos(Grotto.LEDGE - 2, 1, Grotto.SPRING_Z);
                      default -> entrance(home).offset(0, 1, 0);
                    }));
  }

  private static Template home(final String path) {
    return VampireStructures.build().stream()
        .filter(t -> t.path().equals(path))
        .findFirst()
        .orElseThrow();
  }

  @Test
  void theMobWalkFitsTheTallestShippedVampire() {
    assertEquals(2, StructureWalk.MOB.headroom());
    assertTrue(TallestVampire.height() <= StructureWalk.MOB.headroom());
    assertTrue(TallestVampire.height() > StructureWalk.MOB.headroom() - 1);
  }

  @ParameterizedTest(name = "{0}: a vampire walks to every coffin, chair, table and lectern")
  @MethodSource("walks")
  void aVampireWalksToEveryPieceItsHouseholdUses(final String path, final Pos start) {
    final Template home = home(path);
    assertEquals(
        List.of(), StructureAudit.unreachedHousehold(home, StructureWalk.mob(home, start)));
  }

  @ParameterizedTest(name = "{0}: a vampire walks everywhere a person can, with no ladder")
  @MethodSource("walks")
  void aVampireWalksEverywhereAPersonCan(final String path, final Pos start) {
    final Template home = home(path);
    assertEquals(
        List.of(),
        StructureAudit.humanOnly(
            StructureWalk.from(home, start), StructureWalk.mob(home, start), home));
  }

  @ParameterizedTest(name = "{0}: no floor under a roof is left out of reach")
  @MethodSource("walks")
  void noFloorIsSealedOff(final String path, final Pos start) {
    final Template home = home(path);
    assertEquals(List.of(), StructureAudit.sealedFloors(home, StructureWalk.from(home, start)));
  }

  @ParameterizedTest(name = "{0}: every door is walked through from both sides")
  @MethodSource("walks")
  void everyDoorLeadsSomewhere(final String path, final Pos start) {
    final Template home = home(path);
    final Set<Pos> reached = StructureWalk.from(home, start);
    assertEquals(List.of(), StructureAudit.doorsLeadingNowhere(home, reached));
  }

  @ParameterizedTest(
      name = "{0}: wall decor hangs on solid wall and floor decor stands on solid floor")
  @MethodSource("walks")
  void everyDecorationIsHeld(final String path, final Pos start) {
    assertEquals(List.of(), StructureAudit.loose(home(path)));
  }

  @ParameterizedTest(name = "{0}: blood lies in clusters on its floors and low on its walls")
  @MethodSource("walks")
  void everyHomeIsStainedWithBlood(final String path, final Pos start) {
    final Template home = home(path);
    final long floor =
        home.cells().values().stream().filter(state -> state.contains("face=floor")).count();
    final long wall =
        home.cells().values().stream().filter(state -> state.contains("face=wall")).count();
    assertTrue(floor >= BloodStainRules.forPath(path).clusters(), "floor stains " + floor);
    assertTrue(wall >= 1, "wall stains " + wall);
    assertEquals(home.cells(), home(path).cells());
  }

  private static Pos entrance(final Template home) {
    return home.blockData().entrySet().stream()
        .filter(data -> String.valueOf(data.getValue().get("name")).endsWith("_entrance"))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElseThrow();
  }
}
