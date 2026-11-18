package com.grahambartley.nightborn.servant;

import java.util.UUID;

public interface Bindable extends Servant {

  void bind(UUID master, int lifetimeTicks);
}
