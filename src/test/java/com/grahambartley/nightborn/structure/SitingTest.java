package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SitingTest {

  private static final String FOOTING =
      "\"footing\": {\"max_rise\": 4, \"entrance\": \"nightborn:crypt_entrance\"}";
  private static final String SUMMIT = "\"summit\": {\"reach\": 32}";
  private static final String CAVE =
      "\"cave\": {\"entrance\": \"nightborn:lair_entrance\", \"reach\": 24, \"rise\": 12}";

  private static Optional<Siting> decode(final String fields) {
    return Siting.MAP_CODEC
        .codec()
        .parse(JsonOps.INSTANCE, JsonParser.parseString("{" + fields + "}"))
        .result();
  }

  @ParameterizedTest(name = "{0}: decodes {1}, searches {2}, entrance {3}")
  @CsvSource(
      delimiter = '|',
      value = {
        "nothing | true | 0 | ''",
        "footing | true | 0 | nightborn:crypt_entrance",
        "summit | true | 32 | ''",
        "footing and summit | true | 32 | nightborn:crypt_entrance",
        "cave | true | 24 | nightborn:lair_entrance",
        "cave and footing | false | 0 | ''",
        "cave and summit | false | 0 | ''"
      })
  void standsOnOneKindOfGround(
      final String grounds, final boolean decodes, final int reach, final String entrance) {
    final String fields =
        String.join(
            ", ",
            Stream.of(
                    grounds.contains("footing") ? FOOTING : "",
                    grounds.contains("summit") ? SUMMIT : "",
                    grounds.contains("cave") ? CAVE : "")
                .filter(field -> !field.isEmpty())
                .toList());
    final Optional<Siting> siting = decode(fields);
    assertEquals(decodes, siting.isPresent());
    siting.ifPresent(
        found -> {
          assertEquals(reach, found.reach());
          assertEquals(
              entrance.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(entrance)),
              found.entrance());
        });
  }
}
