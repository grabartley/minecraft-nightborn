package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LookDataCheckTest {

  private static final Function<Identifier, Optional<EquipmentSlot>> SLOTS =
      id ->
          switch (id.getPath()) {
            case "coat", "cape" -> Optional.of(EquipmentSlot.CHEST);
            case "shoes" -> Optional.of(EquipmentSlot.FEET);
            default -> Optional.empty();
          };
  private static final VampireBuild BUILD = new VampireBuild(1.0F, 1.6F, 1.9F, 1);

  @Test
  @DisplayName("data where every look has an outfit for each era is clean")
  void clean() {
    assertEquals(
        List.of(),
        LookDataCheck.problems(
            Map.of(id("bram"), look(VampireRig.MALE, "g")),
            Map.of(id("dress"), outfit(VampireRig.MALE, "g", "coat", "shoes")),
            Map.of(id("average"), BUILD),
            SLOTS));
  }

  @ParameterizedTest(name = "{4}")
  @CsvSource(
      delimiter = '|',
      value = {
        "MALE|v|coat|shoes|nightborn:bram has no male outfit for nightborn:g",
        "FEMALE|g|coat|shoes|nightborn:bram has no male outfit for nightborn:g",
        "MALE|g|coat|stone|nightborn:dress uses nightborn:stone, which is not wearable",
        "MALE|g|coat|cape|nightborn:dress puts two pieces in the chest slot"
      })
  @DisplayName("a look with no outfit, or an outfit that is not wearable clothing, is reported")
  void reports(
      final VampireRig rig,
      final String era,
      final String first,
      final String second,
      final String problem) {
    assertEquals(
        List.of(problem),
        LookDataCheck.problems(
            Map.of(id("bram"), look(VampireRig.MALE, "g")),
            Map.of(id("dress"), outfit(rig, era, first, second)),
            Map.of(id("average"), BUILD),
            SLOTS));
  }

  @Test
  @DisplayName("no looks, no builds, a look without eras and upside-down builds are reported")
  void reportsEmpty() {
    assertEquals(
        List.of("there are no vampire looks", "there are no vampire builds"),
        LookDataCheck.problems(Map.of(), Map.of(), Map.of(), SLOTS));
    assertEquals(
        List.of(
            "nightborn:eraless has no era",
            "nightborn:lord has a lowest height above its highest",
            "nightborn:odd has a lowest height above its highest"),
        LookDataCheck.problems(
            Map.of(
                id("eraless"),
                new VampireLook(VampireRig.MALE, id("t"), List.of(), 1, Optional.empty()),
                id("lord"),
                new VampireLook(
                    VampireRig.LORD,
                    id("t"),
                    List.of(),
                    1,
                    Optional.of(new VampireBuild(1.0F, 2.3F, 2.1F, 1)))),
            Map.of(),
            Map.of(id("odd"), new VampireBuild(1.0F, 1.9F, 1.6F, 1)),
            SLOTS));
  }

  private static VampireLook look(final VampireRig rig, final String era) {
    return new VampireLook(rig, id("t.png"), List.of(id(era)), 1, Optional.empty());
  }

  private static VampireOutfit outfit(
      final VampireRig rig, final String era, final String... items) {
    return new VampireOutfit(
        rig,
        id(era),
        List.of(items).stream()
            .map(item -> new VampireOutfit.Piece(id(item), Optional.empty()))
            .toList(),
        1);
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }
}
