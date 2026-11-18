package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GuildOathOrderTest {

  @ParameterizedTest(name = "beside {0}")
  @EnumSource(GuildCounter.Kind.class)
  @DisplayName("the oath is one crucifix entry on the wheel, apart from each counter")
  void entry(final GuildCounter.Kind kind) {
    final List<WheelEntry> entries = new GuildOathOrder().entries(null, null);
    assertEquals(1, entries.size());
    final WheelEntry oath = entries.getFirst();
    assertEquals(GuildOathOrder.ID, oath.action());
    assertEquals(Registries.ITEM.getId(GearItems.GOLD_CRUCIFIX), oath.icon());
    assertEquals(
        "nightborn.guild.oath", ((TranslatableTextContent) oath.label().getContent()).getKey());
    assertNotEquals(new GuildCounterOrders(kind).id(), oath.action());
  }
}
