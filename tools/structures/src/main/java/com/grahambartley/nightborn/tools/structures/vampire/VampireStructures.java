package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.castle.Castle;
import com.grahambartley.nightborn.tools.structures.vampire.crypt.Church;
import com.grahambartley.nightborn.tools.structures.vampire.crypt.Churchyard;
import com.grahambartley.nightborn.tools.structures.vampire.crypt.Ossuary;
import com.grahambartley.nightborn.tools.structures.vampire.crypt.Undercroft;
import com.grahambartley.nightborn.tools.structures.vampire.lair.BloodPoolDen;
import com.grahambartley.nightborn.tools.structures.vampire.lair.Grotto;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Manor;
import com.grahambartley.nightborn.tools.structures.vampire.mountain.MountainCastle;
import java.util.List;

public final class VampireStructures {

  private VampireStructures() {}

  public static List<Template> build() {
    return List.of(
        Churchyard.bank(Undercroft.build()),
        Churchyard.bank(Ossuary.build()),
        Church.build(Church.TOWER),
        Church.build(Church.BELLCOTE),
        BloodPoolDen.build(),
        Grotto.build(),
        Castle.build(Castle.BASTIONS),
        Castle.build(Castle.RUINED),
        Manor.build(Manor.TURRET),
        Manor.build(Manor.TOWER),
        MountainCastle.build(MountainCastle.EYRIE),
        MountainCastle.build(MountainCastle.BASTION));
  }
}
