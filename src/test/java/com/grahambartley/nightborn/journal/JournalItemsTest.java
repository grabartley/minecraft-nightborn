package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class JournalItemsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Journal.class)
  @DisplayName("each journal is one registered item under its book's id that never stacks")
  void registered(final Journal journal) {
    final JournalItem item = assertInstanceOf(JournalItem.class, JournalItems.of(journal));
    assertEquals(journal, item.journal());
    assertEquals(journal.id(), Registries.ITEM.getId(item));
    assertEquals(1, item.getMaxCount());
  }
}
