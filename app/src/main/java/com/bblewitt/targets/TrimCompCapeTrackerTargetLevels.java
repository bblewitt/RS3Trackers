package com.bblewitt.targets;

public enum TrimCompCapeTrackerTargetLevels {
    ATTACK(99),
    STRENGTH(99),
    DEFENCE(99),
    RANGED(99),
    PRAYER(99),
    MAGIC(99),
    RUNECRAFTING(99),
    CONSTRUCTION(99),
    DUNGEONEERING(120),
    ARCHAEOLOGY(120),

    CONSTITUTION(99),
    AGILITY(99),
    HERBLORE(120),
    THIEVING(99),
    CRAFTING(99),
    FLETCHING(99),
    SLAYER(120),
    HUNTER(99),
    DIVINATION(99),
    NECROMANCY(120),

    MINING(110),
    SMITHING(110),
    FISHING(99),
    COOKING(99),
    FIREMAKING(99),
    WOODCUTTING(99),
    FARMING(120),
    SUMMONING(99),
    INVENTION(120);

    private final int targetLevel;

    TrimCompCapeTrackerTargetLevels(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
