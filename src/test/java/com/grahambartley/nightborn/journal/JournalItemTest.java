package com.grahambartley.nightborn.journal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class JournalItemTest {

  @ParameterizedTest(name = "{0} in the {1}")
  @CsvSource({"HUNTER, MAIN_HAND", "HUNTER, OFF_HAND", "VAMPIRE, MAIN_HAND", "VAMPIRE, OFF_HAND"})
  @DisplayName("using a journal takes the action and keeps the book in hand")
  void useKeepsTheBook(final Journal journal, final Hand hand) {
    final ItemStack stack = new ItemStack(JournalItems.of(journal));
    final World world = mock(World.class);
    final PlayerEntity reader = mock(PlayerEntity.class);
    when(reader.getStackInHand(hand)).thenReturn(stack);
    final TypedActionResult<ItemStack> result = JournalItems.of(journal).use(world, reader, hand);
    assertTrue(result.getResult().isAccepted());
    assertSame(stack, result.getValue());
  }
}
