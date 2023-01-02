package callmemaple.ticktrainer.item;

import net.runelite.api.Item;

public class AnyRequirementCollection implements ItemRequirement
{
    private final ItemRequirement[] requirements;

    public AnyRequirementCollection(ItemRequirement... requirements)
    {
        this.requirements = requirements;
    }

    @Override
    public boolean fulfilledBy(int itemId)
    {
        for (ItemRequirement requirement : requirements)
        {
            if (requirement.fulfilledBy(itemId))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fulfilledBy(Item[] items)
    {
        for (ItemRequirement requirement : requirements)
        {
            if (requirement.fulfilledBy(items))
            {
                return true;
            }
        }

        return false;
    }
}
