package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.look.VampireRig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RestlessLookCacheTest {

  private static Optional<RestlessLook> look(final String id) {
    return Optional.of(
        new RestlessLook(
            VampireRig.MALE, Identifier.of("nightborn", "textures/" + id + ".png"), 1));
  }

  @ParameterizedTest(name = "worn in turn [{0}]: looked up {1} times")
  @CsvSource({"a, 1", "a;a;a, 1", "a;b, 2", "a;a;b;b;a, 3"})
  void looksUpOnlyWhenTheWornLookChanges(final String worn, final int lookups) {
    final RestlessLookCache cache = new RestlessLookCache();
    final List<String> asked = new ArrayList<>();
    for (final String id : Arrays.asList(worn.split(";"))) {
      final Optional<RestlessLook> resolved =
          cache.resolve(
              id,
              wanted -> {
                asked.add(wanted);
                return look(wanted);
              });
      assertEquals(look(id), resolved);
    }
    assertEquals(lookups, asked.size());
  }
}
