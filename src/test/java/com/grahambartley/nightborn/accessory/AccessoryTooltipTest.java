package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.GiftModifier;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.faction.Faction;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryTooltipTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "VAMPIRE, tooltip.nightborn.accessory.works_for.vampire",
    "HUMAN, tooltip.nightborn.accessory.works_for.human"
  })
  @DisplayName("the tooltip says who a piece works for")
  void key(final Faction wearer, final String key) {
    assertEquals(key, AccessoryTooltip.key(wearer));
  }

  static Stream<Arguments> pieces() {
    final GiftModifier slower =
        new GiftModifier(
            WornAttributes.BLOOD_DRAIN, -0.1, EntityAttributeModifier.Operation.ADD_VALUE);
    return Stream.of(
        Arguments.of(
            "onyx_ring",
            new Accessory(Optional.empty(), List.of(), List.of(), Accessory.UNDYED),
            List.of()),
        Arguments.of(
            "garnet_signet",
            new Accessory(
                Optional.of(Faction.VAMPIRE), List.of(slower), List.of(), Accessory.UNDYED),
            List.of(
                "item.nightborn.garnet_signet.gift",
                "tooltip.nightborn.accessory.works_for.vampire")),
        Arguments.of(
            "blood_vial_pendant",
            new Accessory(
                Optional.of(Faction.VAMPIRE),
                List.of(),
                List.of(new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1)),
                Accessory.UNDYED),
            List.of(
                "item.nightborn.blood_vial_pendant.gift",
                "tooltip.nightborn.accessory.holds",
                "tooltip.nightborn.accessory.works_for.vampire")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pieces")
  @DisplayName("a piece's tooltip names its gift, what it holds and who it works for")
  void lines(final String piece, final Accessory accessory, final List<String> keys) {
    assertEquals(
        keys,
        AccessoryTooltip.lines(
                AccessoryItems.of(piece), accessory, new ItemStack(AccessoryItems.of(piece)))
            .stream()
            .map(Text::getContent)
            .map(content -> ((TranslatableTextContent) content).getKey())
            .toList());
  }

  @ParameterizedTest(name = "{0} in pockets of {1} and {2}: holds {3} of {4}")
  @CsvSource({"0, 128, 4, 0, 132", "5, 6, 0, 5, 6"})
  @DisplayName("a piece with pockets says how much it holds of everything its pockets take")
  void holdsAcrossPockets(
      final int bolts, final int first, final int second, final int held, final int capacity) {
    final List<AccessoryHold> holds =
        second == 0
            ? List.of(new AccessoryHold(List.of(AmmoItems.STAKE_BOLT), first))
            : List.of(
                new AccessoryHold(List.of(AmmoItems.STAKE_BOLT), first),
                new AccessoryHold(List.of(Items.SPLASH_POTION), second));
    final ItemStack belt = new ItemStack(AccessoryItems.of("bandolier"));
    AccessoryStore.put(holds, belt, new ItemStack(AmmoItems.STAKE_BOLT, bolts));
    final Text line =
        AccessoryTooltip.lines(
                AccessoryItems.of("bandolier"),
                new Accessory(Optional.of(Faction.HUMAN), List.of(), holds, Accessory.UNDYED),
                belt)
            .get(1);
    assertEquals(
        List.of(held, capacity), List.of(((TranslatableTextContent) line.getContent()).getArgs()));
  }
}
