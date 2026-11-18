package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestCharge;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HouseIdentityTest {

  static Stream<Arguments> identities() {
    return Stream.of(
        Arguments.of(HouseIdentity.NONE),
        Arguments.of(new HouseIdentity(Optional.of("Valcourt"), Optional.empty())),
        Arguments.of(
            new HouseIdentity(
                Optional.of("House of Thorns"),
                Optional.of(new Crest(CrestCharge.THORN_BRANCH, DyeColor.BLACK, DyeColor.RED)))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("identities")
  @DisplayName("a house's name and crest survive a save, and either may be missing")
  void roundTrip(final HouseIdentity identity) {
    assertEquals(
        identity,
        HouseIdentity.CODEC
            .parse(
                JsonOps.INSTANCE,
                HouseIdentity.CODEC.encodeStart(JsonOps.INSTANCE, identity).getOrThrow())
            .getOrThrow());
  }
}
