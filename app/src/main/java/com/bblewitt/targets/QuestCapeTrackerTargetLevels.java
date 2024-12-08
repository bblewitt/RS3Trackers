package com.bblewitt.targets;

public enum QuestCapeTrackerTargetLevels {
    ATTACK(79),
    STRENGTH(85),
    DEFENCE(76),
    RANGED(78),
    PRAYER(80),
    MAGIC(81),
    RUNECRAFTING(85),
    CONSTRUCTION(81),
    DUNGEONEERING(77),
    ARCHAEOLOGY(86),

    CONSTITUTION(80),
    AGILITY(83),
    HERBLORE(80),
    THIEVING(85),
    CRAFTING(91),
    FLETCHING(75),
    SLAYER(90),
    HUNTER(90),
    DIVINATION(90),
    NECROMANCY(95),

    MINING(90),
    SMITHING(85),
    FISHING(90),
    COOKING(91),
    FIREMAKING(82),
    WOODCUTTING(90),
    FARMING(86),
    SUMMONING(75),
    INVENTION(85);

    private final int targetLevel;

    QuestCapeTrackerTargetLevels(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
