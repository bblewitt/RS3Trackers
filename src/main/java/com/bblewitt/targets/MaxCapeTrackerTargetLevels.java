package main.java.com.bblewitt.targets;

public enum MaxCapeTrackerTargetLevels {
    ATTACK(99),
    STRENGTH(99),
    DEFENCE(99),
    RANGED(99),
    PRAYER(99),
    MAGIC(99),
    RUNECRAFTING(99),
    CONSTRUCTION(99),
    DUNGEONEERING(99),
    ARCHAEOLOGY(99),

    CONSTITUTION(99),
    AGILITY(99),
    HERBLORE(99),
    THIEVING(99),
    CRAFTING(99),
    FLETCHING(99),
    SLAYER(99),
    HUNTER(99),
    DIVINATION(99),
    NECROMANCY(99),

    MINING(99),
    SMITHING(99),
    FISHING(99),
    COOKING(99),
    FIREMAKING(99),
    WOODCUTTING(99),
    FARMING(99),
    SUMMONING(99),
    INVENTION(99);

    private final int targetLevel;

    MaxCapeTrackerTargetLevels(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
