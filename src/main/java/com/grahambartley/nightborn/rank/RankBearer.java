package com.grahambartley.nightborn.rank;

import java.util.Optional;
import net.minecraft.util.Identifier;

public interface RankBearer {

  Optional<Identifier> shownRank();
}
