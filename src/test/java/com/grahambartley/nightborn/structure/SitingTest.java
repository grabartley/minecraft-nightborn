package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        "cave | true | 32 | nightborn:lair_entrance",
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

  @ParameterizedTest(name = "{0} threshold at {1},{2},{3} facing {4} is stood on at {5},{6},{7}")
  @CsvSource({
    "footing, 3, 64, 9, SOUTH, 3, 65, 9",
    "footing, -120, 71, 450, WEST, -120, 72, 450",
    "summit, 77, 180, -5, NORTH, 77, 181, -5",
    "cave, 3, -23, 9, SOUTH, 3, -22, 10",
    "cave, 3, -23, 9, NORTH, 3, -22, 8",
    "cave, -40, -50, 12, EAST, -39, -49, 12",
    "cave, -40, -50, 12, WEST, -41, -49, 12"
  })
  void standsOnTheDoorstepOrOnTheCaveSillOutside(
      final String ground,
      final int x,
      final int y,
      final int z,
      final Direction out,
      final int feetX,
      final int feetY,
      final int feetZ) {
    final String fields =
        switch (ground) {
          case "cave" -> CAVE;
          case "summit" -> FOOTING + ", " + SUMMIT;
          default -> FOOTING;
        };
    assertEquals(
        new BlockPos(feetX, feetY, feetZ),
        decode(fields).orElseThrow().standingSpot(new Threshold(new BlockPos(x, y, z), out)));
  }
}
