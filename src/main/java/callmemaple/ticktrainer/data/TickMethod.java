package callmemaple.ticktrainer.data;
import callmemaple.ticktrainer.data.item.ItemRequirement;
import lombok.Getter;
import net.runelite.api.AnimationID;
import net.runelite.api.Item;

import static callmemaple.ticktrainer.data.item.ItemRequirements.*;
import static net.runelite.api.ItemID.*;


public enum TickMethod
{
    // TODO fix knife log add more tick items
    KNIFE_LOG(3, AnimationID.FLETCHING_ATTACH_HEADS, item(KNIFE), any(item(TEAK_LOGS), item(MAHOGANY_LOGS))),
    CLAW_VAMPS(3, 5243, item(KEBBIT_CLAWS), any(item(LEATHER_VAMBRACES), item(GREEN_DHIDE_VAMBRACES), item(BLUE_DHIDE_VAMBRACES), item(RED_DHIDE_VAMBRACES), item(BLACK_DHIDE_VAMBRACES))),
    HERB_TAR(3, AnimationID.HERBLORE_MAKE_TAR, item(SWAMP_TAR), any(item(GUAM_LEAF), item(HARRALANDER), item(MARRENTILL), item(TARROMIN)), item(PESTLE_AND_MORTAR)),
    UNKNOWN
    {
        @Override
        public boolean meetsRequirement(Item itemOne, Item itemTwo, Item[] inventory)
        {
            return false;
        }
    };

    private final ItemRequirement[] tickItems;
    private final ItemRequirement[] additionalItems;
    @Getter
    private final int animationId;
    @Getter
    private final int skillingTick;

    TickMethod()
    {
        this.animationId = -1;
        this.skillingTick = -1;
        this.tickItems = new ItemRequirement[]{};
        this.additionalItems = new ItemRequirement[]{};
    }

    TickMethod(int tick, int animationId, ItemRequirement tickTool, ItemRequirement tickItem, ItemRequirement... additionalItems)
    {
        this.skillingTick = tick;
        this.animationId = animationId;
        this.tickItems = new ItemRequirement[]{tickTool, tickItem};
        this.additionalItems = additionalItems;
    }

    public boolean meetsRequirement(Item itemOne, Item itemTwo, Item[] inventory)
    {
        // Can't use the item on itself
        if (itemOne.getId() == itemTwo.getId())
        {
            return false;
        }

        for (ItemRequirement ir : additionalItems)
        {
            // If any additional item is missing
            if (!ir.fulfilledBy(inventory))
            {
                return false;
            }
        }

        for (ItemRequirement ir : tickItems)
        {
            if (!ir.fulfilledBy(new Item[]{itemOne, itemTwo}))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isInventoryAction(Item itemOne, Item itemTwo, Item[] inventory)
    {
        for (TickMethod action : TickMethod.values()) {
            if (action.meetsRequirement(itemOne, itemTwo, inventory))
            {
                return true;
            }
        }
        return false;
    }

    public static TickMethod findInventoryAction(Item itemOne, Item itemTwo, Item[] inventory)
    {
        for (TickMethod action : values()) {
            if (action.meetsRequirement(itemOne, itemTwo, inventory))
            {
                return action;
            }
        }
        return UNKNOWN;
    }
}
