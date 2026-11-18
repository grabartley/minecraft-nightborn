package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisMarkTest {

  static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @ParameterizedTest(name = "token {0}, growths {1}, given {2}")
  @CsvSource({"0, 0, Agnes", "4, 2, Silas", "1, 3, ''"})
  @DisplayName("a nemesis keeps its quarry, its growth and its given name through a save")
  void survivesSave(final int token, final int growths, final String given) {
    final NemesisMark mark = new NemesisMark(QUARRY, token, growths, given);
    assertEquals(
        mark,
        NemesisMark.CODEC
            .parse(
                JsonOps.INSTANCE,
                NemesisMark.CODEC.encodeStart(JsonOps.INSTANCE, mark).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "growths {0} grows to {1}, token {2} becomes {3}")
  @CsvSource({"0, 1, 0, 1", "2, 3, 5, 9", "-3, 1, 0, 0"})
  @DisplayName("growing counts up and a new visit carries a new token")
  void grows(final int growths, final int grown, final int token, final int next) {
    final NemesisMark mark = new NemesisMark(QUARRY, token, growths, "Agnes");
    assertEquals(grown, mark.grown().growths());
    assertEquals(next, mark.withToken(next).token());
    assertEquals(mark.growths(), mark.withToken(next).growths());
  }
}
