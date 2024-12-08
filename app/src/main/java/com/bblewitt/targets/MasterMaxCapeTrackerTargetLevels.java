package com.bblewitt.targets;

@SuppressWarnings("SameParameterValue")
public enum MasterMaxCapeTrackerTargetLevels {
    ATTACK(120),
    STRENGTH(120),
    DEFENCE(120),
    RANGED(120),
    PRAYER(120),
    MAGIC(120),
    RUNECRAFTING(120),
    CONSTRUCTION(120),
    DUNGEONEERING(120),
    ARCHAEOLOGY(120),

    CONSTITUTION(120),
    AGILITY(120),
    HERBLORE(120),
    THIEVING(120),
    CRAFTING(120),
    FLETCHING(120),
    SLAYER(120),
    HUNTER(120),
    DIVINATION(120),
    NECROMANCY(120),

    MINING(120),
    SMITHING(120),
    FISHING(120),
    COOKING(120),
    FIREMAKING(120),
    WOODCUTTING(120),
    FARMING(120),
    SUMMONING(120),
    INVENTION(120);

    private final int targetLevel;

    MasterMaxCapeTrackerTargetLevels(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
