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

class ChallengeRecordTest {

  @ParameterizedTest(name = "finishing {0} twice is remembered once")
  @CsvSource({"nightborn:wings", "nightborn:watchman"})
  void remembersFinishedTrialsOnce(final String id) {
    final Identifier trial = Identifier.of(id);
    final ChallengeRecord record = ChallengeRecord.EMPTY.finishing(trial).finishing(trial);
    assertEquals(List.of(trial), record.finished());
    assertEquals(true, record.everFinished(trial));
    final ChallengeRecord offered =
        record.withOffer(
            new TrialOffer(
                Optional.of(Side.VAMPIRE), 2L, List.of(OfferedTrial.fresh(trial, true))));
    assertEquals(
        offered,
        ChallengeRecord.CODEC
            .parse(
                JsonOps.INSTANCE,
                ChallengeRecord.CODEC.encodeStart(JsonOps.INSTANCE, offered).getOrThrow())
            .getOrThrow());
    final ByteBuf buf = Unpooled.buffer();
    ChallengeRecord.PACKET_CODEC.encode(buf, offered);
    assertEquals(offered, ChallengeRecord.PACKET_CODEC.decode(buf));
    assertEquals(false, offered.isEmpty());
  }
}
