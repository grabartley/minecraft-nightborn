package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NightbornCommandTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "nightborn.command.help.header",
        "nightborn.command.help.skills",
        "nightborn.command.help.rank",
        "nightborn.command.help.swear",
        "nightborn.command.help.thrall",
        "nightborn.command.help.thrall_free"
      })
  @DisplayName("the help lists every command, the thrall ones included")
  void helpLists(final String key) {
    final List<String> keys =
        NightbornCommand.helpLines().stream()
            .map(line -> ((TranslatableTextContent) line.getContent()).getKey())
            .toList();
    assertTrue(keys.contains(key), key);
  }
}
