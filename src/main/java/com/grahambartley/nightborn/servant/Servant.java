package com.grahambartley.nightborn.servant;

import java.util.Optional;
import java.util.UUID;

public interface Servant {

  Optional<UUID> owner();
}
