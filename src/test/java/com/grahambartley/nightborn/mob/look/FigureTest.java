package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.HunterLook;
import com.grahambartley.nightborn.guild.HunterLooking;
import com.grahambartley.nightborn.guild.HunterRig;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FigureTest {

  static Stream<Arguments> figures() {
    final HunterLooking guildMaster =
        new HunterLooking() {
          @Override
          public HunterRig rig() {
            return HunterRig.GUILD_MASTER;
          }

          @Override
          public Identifier texture() {
            return HunterLook.MISSING;
          }
        };
    return Stream.of(
        Arguments.of("a plain figure", new Figure() {}),
        Arguments.of("a guild master", guildMaster));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("figures")
  @DisplayName("a figure that rolls no bust of its own is flat")
  void defaultsFlat(final String who, final Figure figure) {
    assertEquals(Figure.FLAT, figure.bust(), who);
  }
}
