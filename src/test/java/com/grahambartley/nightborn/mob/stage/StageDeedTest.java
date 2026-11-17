package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StageDeedTest {

  static StageDeed deed(final StageDeed.Act act) {
    return new StageDeed(
        act,
        TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("nightborn", "deed/adult")),
        Text.literal("a deed"));
  }

  @ParameterizedTest(name = "{0} asked, {1} done on a listed victim {2} -> {3}")
  @CsvSource({
    "DRAIN, DRAIN, true, true",
    "DRAIN, DRAIN, false, false",
    "DRAIN, END, true, false",
    "END, END, true, true",
    "END, DRAIN, true, false",
    "END, END, false, false"
  })
  @DisplayName("a deed is done only by its own act on one of its victims")
  void doneBy(
      final StageDeed.Act asked,
      final StageDeed.Act done,
      final boolean listed,
      final boolean counts) {
    assertEquals(counts, deed(asked).doneBy(done, listed));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"drain, DRAIN", "end, END"})
  @DisplayName("a deed reads from stage data")
  void parses(final String act, final StageDeed.Act parsed) {
    final StageDeed deed =
        StageDeed.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"act\":\""
                        + act
                        + "\",\"victims\":\"nightborn:deed/adult\","
                        + "\"description\":{\"translate\":\"nightborn.age.deed.adult\"}}"))
            .getOrThrow();
    assertEquals(parsed, deed.act());
    assertEquals(Identifier.of("nightborn", "deed/adult"), deed.victims().id());
    assertEquals(Text.translatable("nightborn.age.deed.adult"), deed.description());
  }

  @Test
  @DisplayName("an act nobody knows is refused")
  void refusesUnknownActs() {
    assertTrue(
        StageDeed.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"act\":\"dance\",\"victims\":\"nightborn:deed/adult\","
                        + "\"description\":\"x\"}"))
            .isError());
    assertFalse(deed(StageDeed.Act.END).doneBy(StageDeed.Act.DRAIN, true));
  }
}
