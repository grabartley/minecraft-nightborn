package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterStockTest {

  private static List<TradeOffer> wares(final int count) {
    return java.util.stream.IntStream.range(0, count)
        .mapToObj(
            index ->
                new TradeOffer(
                    new TradedItem(Items.EMERALD, index + 1),
                    new ItemStack(Items.ARROW, 8),
                    4,
                    0,
                    0.0F))
        .toList();
  }

  @ParameterizedTest(name = "stocked day {0} with {1} wares, opened day {2} -> restocked {3}")
  @CsvSource({
    "3, 2, 3, false",
    "3, 2, 4, true",
    "3, 0, 3, true",
    "5, 2, 4, true",
    "0, 1, 0, false"
  })
  @DisplayName("a hunter restocks on a new day, or whenever it has nothing on offer")
  void restock(final long stocked, final int count, final long today, final boolean restocked) {
    final HunterStock stock = new HunterStock();
    stock.restock(stocked, () -> wares(count));
    final AtomicInteger asked = new AtomicInteger();
    assertEquals(
        restocked,
        stock.restock(
            today,
            () -> {
              asked.incrementAndGet();
              return wares(1);
            }));
    assertEquals(restocked ? 1 : 0, asked.get());
    assertEquals(restocked ? 1 : count, stock.offers().size());
  }

  @ParameterizedTest(name = "{0} wares, {1} used, day {2}")
  @CsvSource({"1, 0, 0", "2, 3, 7", "3, 4, 120"})
  @DisplayName("what a hunter has sold today is saved with it, so a reload does not restock it")
  void savedAndLoaded(final int count, final int used, final long day) {
    final HunterStock stock = new HunterStock();
    stock.restock(day, () -> wares(count));
    for (int sale = 0; sale < used; sale++) {
      stock.offers().getFirst().use();
    }
    final NbtCompound nbt = new NbtCompound();
    stock.write(nbt, NbtOps.INSTANCE);
    final HunterStock loaded = new HunterStock();
    loaded.read(nbt, NbtOps.INSTANCE);
    assertEquals(count, loaded.offers().size());
    assertEquals(used, loaded.offers().getFirst().getUses());
    assertEquals(false, loaded.restock(day, () -> wares(9)));
  }
}
