package com.bblewitt.targets;

public enum AreaTasksTrackerTargetLevels {
    ATTACK(79),
    STRENGTH(85),
    DEFENCE(76),
    RANGED(78),
    PRAYER(95),
    MAGIC(90),
    RUNECRAFTING(91),
    CONSTRUCTION(81),
    DUNGEONEERING(95),
    ARCHAEOLOGY(86),

    CONSTITUTION(80),
    AGILITY(90),
    HERBLORE(96),
    THIEVING(95),
    CRAFTING(98),
    FLETCHING(93),
    SLAYER(95),
    HUNTER(90),
    DIVINATION(90),
    NECROMANCY(95),

    MINING(90),
    SMITHING(95),
    FISHING(96),
    COOKING(95),
    FIREMAKING(92),
    WOODCUTTING(90),
    FARMING(91),
    SUMMONING(95),
    INVENTION(85);

    private final int targetLevel;

    AreaTasksTrackerTargetLevels(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
