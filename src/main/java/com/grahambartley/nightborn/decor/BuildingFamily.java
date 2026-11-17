package com.grahambartley.nightborn.decor;

import java.util.List;
import java.util.Set;

public record BuildingFamily(String baseId, Material material, Set<FamilyMember> members) {

  public enum Material {
    STONE,
    PALE_STONE,
    CLAY,
    WOOD
  }

  private static final Set<FamilyMember> STONE_MEMBERS =
      Set.of(FamilyMember.BASE, FamilyMember.STAIRS, FamilyMember.SLAB, FamilyMember.WALL);
  private static final Set<FamilyMember> ROOF_MEMBERS =
      Set.of(FamilyMember.BASE, FamilyMember.STAIRS, FamilyMember.SLAB);
  private static final Set<FamilyMember> WOOD_MEMBERS =
      Set.of(FamilyMember.BASE, FamilyMember.STAIRS, FamilyMember.SLAB, FamilyMember.FENCE);

  public static final List<BuildingFamily> ALL =
      List.of(
          new BuildingFamily("gothic_stone_bricks", Material.STONE, STONE_MEMBERS),
          new BuildingFamily("cracked_gothic_stone_bricks", Material.STONE, STONE_MEMBERS),
          new BuildingFamily("mossy_gothic_stone_bricks", Material.STONE, STONE_MEMBERS),
          new BuildingFamily("carved_gothic_stone", Material.STONE, Set.of(FamilyMember.BASE)),
          new BuildingFamily("limewashed_bricks", Material.PALE_STONE, STONE_MEMBERS),
          new BuildingFamily("worn_limewashed_bricks", Material.PALE_STONE, STONE_MEMBERS),
          new BuildingFamily("plaster", Material.PALE_STONE, Set.of(FamilyMember.BASE)),
          new BuildingFamily("clay_roof_tiles", Material.CLAY, ROOF_MEMBERS),
          new BuildingFamily("ebony_planks", Material.WOOD, WOOD_MEMBERS));

  public BuildingFamily {
    members = Set.copyOf(members);
    if (!members.contains(FamilyMember.BASE)) {
      throw new IllegalArgumentException(baseId + " family has no base block");
    }
  }

  public List<String> ids() {
    return List.of(FamilyMember.values()).stream()
        .filter(this.members::contains)
        .map(member -> member.idFor(this.baseId))
        .toList();
  }
}
