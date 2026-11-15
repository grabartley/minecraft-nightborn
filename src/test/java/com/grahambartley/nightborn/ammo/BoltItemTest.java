package com.grahambartley.nightborn.ammo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BoltItemTest {

  static Stream<Arguments> bolts() {
    return Stream.of(
        Arguments.of(AmmoItems.STAKE_BOLT, List.of()),
        Arguments.of(AmmoItems.SILVER_BOLT, List.of()),
        Arguments.of(AmmoItems.HOLY_WATER_BOLT, List.of("effect.nightborn.blessing")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bolts")
  @DisplayName("a bolt that carries a potion names what it carries")
  void tooltip(final Item bolt, final List<String> keys) {
    final List<Text> lines = new ArrayList<>();
    bolt.appendTooltip(new ItemStack(bolt), Item.TooltipContext.DEFAULT, lines, TooltipType.BASIC);
    assertEquals(
        keys,
        lines.stream()
            .map(Text::getContent)
            .map(content -> ((TranslatableTextContent) content).getKey())
            .toList());
  }
}
