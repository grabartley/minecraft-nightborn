package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MindTiesTest {

  static Stream<Arguments> ties() {
    return Stream.of(
        Arguments.of(MindTies.MESMERISED_BY, "nightborn:mesmerised_by"),
        Arguments.of(MindTies.CHARMED_BY, "nightborn:charmed_by"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("ties")
  @DisplayName("who mesmerised or charmed an entity is saved with it")
  void persistent(final AttachmentType<UUID> tie, final String id) {
    assertEquals(id, tie.identifier().toString());
    assertEquals(true, tie.isPersistent());
  }
}
