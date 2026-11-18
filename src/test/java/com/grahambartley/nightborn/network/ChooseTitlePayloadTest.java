package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChooseTitlePayloadTest {

  @ParameterizedTest(name = "title {0}")
  @CsvSource({"nightborn:the_cabinet", "other:feat", "''"})
  void aTitleOrNoneCrossesTheNetwork(final String title) {
    final ChooseTitlePayload payload =
        new ChooseTitlePayload(Optional.of(title).filter(t -> !t.isEmpty()).map(Identifier::of));
    final ByteBuf buf = Unpooled.buffer();
    ChooseTitlePayload.CODEC.encode(buf, payload);
    assertEquals(payload, ChooseTitlePayload.CODEC.decode(buf));
  }
}
