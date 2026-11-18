package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.journal.Journal;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GuildJournalOrderTest {

  @ParameterizedTest(name = "beside {0}")
  @EnumSource(GuildCounter.Kind.class)
  @DisplayName("asking for a new journal is one entry showing the hunter's journal")
  void entry(final GuildCounter.Kind kind) {
    final List<WheelEntry> entries = new GuildJournalOrder().entries(null, null);
    assertEquals(1, entries.size());
    final WheelEntry journal = entries.getFirst();
    assertEquals(GuildJournalOrder.ID, journal.action());
    assertEquals(Journal.HUNTER.id(), journal.icon());
    assertEquals(true, Registries.ITEM.containsId(journal.icon()));
    assertEquals(
        "nightborn.guild.journal",
        ((TranslatableTextContent) journal.label().getContent()).getKey());
    assertNotEquals(new GuildCounterOrders(kind).id(), journal.action());
    assertNotEquals(GuildOathOrder.ID, journal.action());
  }
}
