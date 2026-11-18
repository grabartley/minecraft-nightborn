package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FeatLogTest {

  static final Identifier CABINET = Identifier.of("nightborn", "the_cabinet");
  static final Identifier STAKED = Identifier.of("nightborn", "never_staked");

  @ParameterizedTest(name = "earned as vampire {0}: open {1}, worn shows {0}")
  @CsvSource({"true", "false"})
  void anEarnedFeatClosesAndItsTitleCanBeWorn(final boolean asVampire) {
    final FeatLog log =
        FeatLog.EMPTY.counted(CABINET, new Tally(3, List.of(), -1L)).earning(CABINET, asVampire);
    assertEquals(false, log.open(CABINET));
    assertEquals(Tally.EMPTY, log.tally(CABINET));
    final FeatLog worn = log.wearing(Optional.of(CABINET));
    assertEquals(Optional.of(CABINET), worn.title());
    assertEquals(Optional.of(asVampire), worn.worn());
    assertEquals(
        worn,
        FeatLog.CODEC
            .parse(JsonOps.INSTANCE, FeatLog.CODEC.encodeStart(JsonOps.INSTANCE, worn).getOrThrow())
            .getOrThrow());
    final ByteBuf buf = Unpooled.buffer();
    FeatLog.PACKET_CODEC.encode(buf, worn);
    assertEquals(worn, FeatLog.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "wearing {0}, never earned, is refused")
  @CsvSource({"nightborn:never_staked", "nightborn:made_up"})
  void anUnearnedTitleIsNeverWorn(final String title) {
    assertEquals(
        Optional.empty(), FeatLog.EMPTY.wearing(Optional.of(Identifier.of(title))).title());
  }

  @ParameterizedTest(name = "a spoiled feat stays shut after {0} counted")
  @CsvSource({"0", "5"})
  void aSpoiledFeatStaysShut(final int counted) {
    final FeatLog log =
        FeatLog.EMPTY.counted(STAKED, new Tally(counted, List.of(), -1L)).spoiling(STAKED);
    assertEquals(false, log.open(STAKED));
    assertEquals(Tally.EMPTY, log.tally(STAKED));
  }
}
