package callmemaple.ticktrainer.data;

import net.runelite.api.ObjectID;

import static net.runelite.api.ObjectID.*;

public enum ResourceNode
{
    // MINING
    TIN(TIN_ROCKS, TIN_ROCKS_11360, TIN_ROCKS_11361, TIN_ROCKS_37945),
    COPPER(COPPER_ROCKS, COPPER_ROCKS_10943, COPPER_ROCKS_11161, COPPER_ROCKS_37944),
    IRON(IRON_ROCKS, IRON_ROCKS_11365, IRON_ROCKS_36203, IRON_ROCKS_42833),
    COAL(COAL_ROCKS, COAL_ROCKS_11366, COAL_ROCKS_11367, COAL_ROCKS_36204),
    SILVER(SILVER_ROCKS, SILVER_ROCKS_11369, SILVER_ROCKS_36205),
    SANDSTONE(SANDSTONE_ROCKS),
    GOLD(GOLD_ROCKS, GOLD_ROCKS_11371, GOLD_ROCKS_36206),
    GRANITE(GRANITE_ROCKS),
    MITHRIL(MITHRIL_ROCKS, MITHRIL_ROCKS_11373, MITHRIL_ROCKS_36207),
    LOVAKITE(LOVAKITE_ROCKS, LOVAKITE_ROCKS_28597),
    ADAMANTITE(ADAMANTITE_ROCKS, ADAMANTITE_ROCKS_11375, ADAMANTITE_ROCKS_36208),
    RUNITE(RUNITE_ROCKS, RUNITE_ROCKS_11377, RUNITE_ROCKS_36209),
    ASH_VEIN(ASH_PILE),
    GEM(GEM_ROCKS, GEM_ROCKS_11381, GEM_ROCK, GEM_ROCK_9031, GEM_ROCK_9032),
    URT_SALT(URT_SALT_ROCKS),
    EFH_SALT(EFH_SALT_ROCKS),
    TE_SALT(TE_SALT_ROCKS),
    BASALT(BASALT_ROCKS),
    RUNE_ESSENCE(RUNE_ESSENCE_34773),
    DAEYALT_ESSENCE(DAEYALT_ESSENCE_39095),
    AMALGAMATION(ObjectID.AMALGAMATION, AMALGAMATION_49913, AMALGAMATION_49914, AMALGAMATION_49915, AMALGAMATION_49916, AMALGAMATION_49917);
    // TODO add barronite ore
    private final int[] objectIds;

    ResourceNode(int... objectIds)
    {
        this.objectIds = objectIds;
    }

    public static boolean isNode(int testObjectId)
    {
        for (ResourceNode rn : values())
        {
            for (int objectId : rn.objectIds)
            {
                if (testObjectId == objectId)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
