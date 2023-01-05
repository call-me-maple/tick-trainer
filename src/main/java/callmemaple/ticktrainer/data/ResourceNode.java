package callmemaple.ticktrainer.data;

import static net.runelite.api.ObjectID.*;

public enum ResourceNode
{
    // MINING
    TIN(ROCKS_11360, ROCKS_11361),
    COPPER(ROCKS_10943, ROCKS_11161),
    IRON(ROCKS_11364, ROCKS_11365, ROCKS_36203),
    COAL(ROCKS_11366, ROCKS_11367, ROCKS_36204),
    SILVER(ROCKS_11368, ROCKS_11369, ROCKS_36205),
    SANDSTONE(ROCKS_11386),
    GOLD(ROCKS_11370, ROCKS_11371, ROCKS_36206),
    GRANITE(ROCKS_11387),
    MITHRIL(ROCKS_11372, ROCKS_11373, ROCKS_36207),
    LOVAKITE(ROCKS_28596, ROCKS_28597),
    ADAMANTITE(ROCKS_11374, ROCKS_11375, ROCKS_36208),
    RUNITE(ROCKS_11376, ROCKS_11377, ROCKS_36209),
    ASH_VEIN(ASH_PILE),
    GEM_ROCK(ROCKS_11380, ROCKS_11381),
    URT_SALT(ROCKS_33254),
    EFH_SALT(ROCKS_33255),
    TE_SALT(ROCKS_33256),
    BASALT(ROCKS_33257),
    RUNE_ESSENCE(RUNE_ESSENCE_34773),
    DAEYALT_ESSENCE(DAEYALT_ESSENCE_39095);

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
