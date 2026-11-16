package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TopiaryTest {

  @ParameterizedTest(name = "a figure at {0},{1} stands {2} tall")
  @CsvSource({"3, 3, 5", "4, 2, 5"})
  void figuresStandOnMoss(final int x, final int z, final int height) {
    final Template t = new Template("vampire/test", 8, 8, 8);
    Topiary.figure(t, x, z);
    assertEquals("minecraft:moss_block", t.get(x, 0, z));
    assertEquals(Topiary.CLIPPED, t.get(x, height, z));
    assertEquals(null, t.get(x, height + 1, z));
    assertEquals(Topiary.CLIPPED, t.get(x - 1, 1, z));
    assertEquals(null, t.get(x - 1, 1, z - 1));
  }

  @ParameterizedTest(name = "a cone at {0},{1} tapers")
  @CsvSource({"3, 3", "2, 4"})
  void conesTaper(final int x, final int z) {
    final Template t = new Template("vampire/test", 8, 8, 8);
    Topiary.cone(t, x, z);
    assertEquals(Topiary.CLIPPED, t.get(x - 1, 1, z - 1));
    assertEquals(null, t.get(x - 1, 2, z));
    assertEquals(Topiary.CLIPPED, t.get(x, 3, z));
  }
}
