package callmemaple.ticktrainer.data.item;

import net.runelite.api.Item;

public class AllRequirementsCollection implements ItemRequirement
{
    private String name;
    private ItemRequirement[] requirements;

    public AllRequirementsCollection(String name, ItemRequirement... requirements)
    {
        this.name = name;
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
            if (!requirement.fulfilledBy(items))
            {
                return false;
            }
        }

        return true;
    }
}
