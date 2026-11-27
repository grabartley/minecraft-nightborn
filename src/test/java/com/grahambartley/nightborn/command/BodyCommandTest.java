package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BodyCommandTest {

  @ParameterizedTest(name = "{0}: complete {1}")
  @CsvSource({
    "body sever Steve left_arm, true",
    "body sever Alex right_leg, true",
    "body restore Steve, true",
    "body sever Steve, false",
    "body restore, false",
    "body heal Steve, false"
  })
  @DisplayName("sever takes targets and a part, restore takes targets")
  void parses(final String input, final boolean complete) {
    final CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
    dispatcher.register(BodyCommand.node());
    final ParseResults<ServerCommandSource> parse = dispatcher.parse(input, null);
    assertEquals(
        complete,
        parse.getReader().getRemainingLength() == 0 && parse.getContext().getCommand() != null);
  }
}
