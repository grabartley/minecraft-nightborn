package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContractProgressTest {

  private static final Identifier CONTRACT = Identifier.of("nightborn", "stake_three");

  @ParameterizedTest(name = "{0} done becomes {1}")
  @CsvSource({"0, 1", "2, 3", "63, 64", "64, 64"})
  void advancesOneStepUpToTheMost(final int done, final int after) {
    assertEquals(after, new ContractProgress(CONTRACT, done).advanced().done());
  }

  @ParameterizedTest(name = "{0} is kept as {1}")
  @CsvSource({"-4, 0", "0, 0", "64, 64", "200, 64"})
  void clampsWhatIsDone(final int done, final int kept) {
    assertEquals(kept, new ContractProgress(CONTRACT, done).done());
  }

  @Test
  void aNewContractFromDataStartsWithNothingDone() {
    final ContractProgress progress =
        ContractProgress.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"contract\":\"nightborn:x\"}"))
            .getOrThrow();
    assertEquals(new ContractProgress(Identifier.of("nightborn", "x"), 0), progress);
  }

  @ParameterizedTest(name = "{0} done survives the network")
  @CsvSource({"0", "5", "64"})
  void survivesTheNetwork(final int done) {
    final ByteBuf buffer = Unpooled.buffer();
    final ContractProgress sent = new ContractProgress(CONTRACT, done);
    ContractProgress.PACKET_CODEC.encode(buffer, sent);
    assertEquals(sent, ContractProgress.PACKET_CODEC.decode(buffer));
  }
}
