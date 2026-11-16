package com.grahambartley.nightborn.tools.structures.template;

import static com.grahambartley.nightborn.tools.structures.template.BlockState.AIR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Connections {

  private static final Pattern CONNECTS =
      Pattern.compile("(_fence$|_pane$|^minecraft:iron_bars$|_wall$)");
  private static final Pattern NOT_FULL =
      Pattern.compile(
          "(slab|stairs|fence|wall|pane|iron_bars|door|trapdoor|lantern|torch|chain|sign|banner"
              + "|carpet|ladder|bell|button|lever|pressure|rail|campfire|anvil|cauldron|grindstone"
              + "|lectern|flower|sapling|snow$|grass$|fern|vine|_bed$|_rod$|candle|cobweb|hopper"
              + "|skull|head|scaffolding|path|weapon_rack|wanted_poster|trophy_mount|crucifix|portrait|gargoyle|coffin|drape|ornate_|candelabra|chandelier|chalice|blood_|garlic|jigsaw|^minecraft:air"
              + "|water|lava|sculk_vein|sculk_sensor|sculk_shrieker|composter|brewing|end_rod|cake"
              + "|bamboo|dripstone|amethyst_cluster|comparator|repeater|redstone_wire|tripwire"
              + "|moss_carpet|turtle_egg|sea_pickle|decorated_pot|flower_pot)");
  private static final List<String> SIDES = List.of("north", "south", "east", "west");

  private Connections() {}

  public static void connect(final Template template) {
    final Map<Pos, String> cells = template.cells();
    for (final Map.Entry<Pos, String> cell : new ArrayList<>(cells.entrySet())) {
      final BlockState.Parsed parsed = BlockState.parse(cell.getValue());
      final String block = parsed.block();
      if (!CONNECTS.matcher(block).find()) {
        continue;
      }
      final Map<String, String> properties = new LinkedHashMap<>(parsed.properties());
      final boolean wall = block.endsWith("_wall");
      for (final String side : SIDES) {
        final Pos step = Facing.STEP.get(side);
        final String neighbour = cells.get(cell.getKey().offset(step.x(), 0, step.z()));
        final boolean on = connects(block, neighbour);
        if (wall) {
          properties.put(side, on ? "low" : "none");
        } else {
          properties.put(side, on ? "true" : "false");
        }
      }
      if (wall) {
        properties.put("up", "true");
      }
      template.place(cell.getKey(), BlockState.of(block, properties));
    }
  }

  public static boolean connects(final String block, final String neighbour) {
    if (neighbour == null || neighbour.equals(AIR)) {
      return false;
    }
    final String other = BlockState.parse(neighbour).block();
    if (block.endsWith("_wall")) {
      return other.endsWith("_wall") || solidFull(neighbour);
    }
    if (other.equals(block)) {
      return true;
    }
    if (block.endsWith("_fence")) {
      return other.endsWith("_fence") || solidFull(neighbour) || other.endsWith("_fence_gate");
    }
    return CONNECTS.matcher(other).find() && !other.endsWith("_fence") || solidFull(neighbour);
  }

  public static boolean solidFull(final String state) {
    if (state == null) {
      return false;
    }
    return !NOT_FULL.matcher(BlockState.parse(state).block()).find();
  }
}
