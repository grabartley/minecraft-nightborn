package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WornLookTest {

  @ParameterizedTest(name = "{0} {1} then {2} {3}: resolved {4} times")
  @CsvSource({
    "MALE, a, MALE, a, 1",
    "MALE, a, MALE, b, 2",
    "MALE, '', MALE, '', 1",
    "MALE, '', MALE, a, 2",
    "MALE, '', FEMALE, '', 2",
    "MALE, a, FEMALE, a, 2"
  })
  void resolvesATextureOnlyWhenTheRigOrLookChanges(
      final HunterRig firstRig,
      final String first,
      final HunterRig secondRig,
      final String second,
      final int resolved) {
    final List<String> asked = new ArrayList<>();
    final WornLook worn = new WornLook();
    final List<HunterRig> rigs = List.of(firstRig, secondRig, secondRig);
    final List<String> looks = List.of(first, second, second);
    for (int i = 0; i < looks.size(); i++) {
      final String look = looks.get(i);
      assertEquals(
          Identifier.of("nightborn", "look_" + look),
          worn.texture(
              rigs.get(i),
              look,
              id -> {
                asked.add(id);
                return Identifier.of("nightborn", "look_" + id);
              }));
    }
    assertEquals(resolved, asked.size());
  }
}
