package com.grahambartley.nightborn.age;

public record AgeNeeds(int nights, int blood) {

  public boolean opened(final AgeProgress progress) {
    return progress.nights() >= nights && progress.blood() >= blood;
  }
}
