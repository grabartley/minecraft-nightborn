package com.grahambartley.nightborn.tools.structures.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShippedDiffTest {

  @TempDir Path generated;
  @TempDir Path shipped;

  @ParameterizedTest(name = "shipped {0}, stray {1}: {2}")
  @CsvSource({
    "same, '', ''",
    "changed, '', differs: structure/t.nbt",
    "missing, '', not shipped: structure/t.nbt",
    "same, structure/stray.nbt, not generated: structure/stray.nbt",
    "same, loot/x.json, ''"
  })
  void reportsEveryTemplateThatDoesNotMatch(
      final String state, final String stray, final String problem) throws IOException {
    final OutputTree ours = new OutputTree(generated);
    ours.nbt("structure/t.nbt", Nbt.compound("x", 1));
    if (!state.equals("missing")) {
      new OutputTree(shipped)
          .nbt("structure/t.nbt", Nbt.compound("x", state.equals("same") ? 1 : 2));
    }
    if (!stray.isEmpty()) {
      Files.createDirectories(shipped.resolve(stray).getParent());
      Files.writeString(shipped.resolve(stray), "");
    }
    assertEquals(
        problem.isEmpty() ? List.of() : List.of(problem),
        ShippedDiff.differences(
            generated, ours.written(), shipped, List.of("structure", "missing")));
  }
}
