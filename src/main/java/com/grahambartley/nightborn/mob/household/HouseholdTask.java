package com.grahambartley.nightborn.mob.household;

public interface HouseholdTask {

  boolean tick();

  default void stop() {}
}
