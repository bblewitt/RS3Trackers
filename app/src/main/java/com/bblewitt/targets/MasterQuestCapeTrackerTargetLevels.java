package com.bblewitt.targets;

public enum MasterQuestCapeTrackerTargetLevels {
    ATTACK(90),
    STRENGTH(90),
    DEFENCE(90),
    RANGED(90),
    PRAYER(95),
    MAGIC(93),
    RUNECRAFTING(99),
    CONSTRUCTION(90),
    DUNGEONEERING(117),
    ARCHAEOLOGY(120),

    CONSTITUTION(91),
    AGILITY(90),
    HERBLORE(96),
    THIEVING(95),
    CRAFTING(98),
    FLETCHING(95),
    SLAYER(115),
    HUNTER(90),
    DIVINATION(95),
    NECROMANCY(120),

    MINING(95),
    SMITHING(95),
    FISHING(96),
    COOKING(95),
    FIREMAKING(95),
    WOODCUTTING(90),
    FARMING(114),
    SUMMONING(95),
    INVENTION(90);

    private final int targetLevel;

    MasterQuestCapeTrackerTargetLevels(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
