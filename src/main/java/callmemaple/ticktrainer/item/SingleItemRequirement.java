package callmemaple.ticktrainer.item;

import net.runelite.api.Item;

public class SingleItemRequirement implements ItemRequirement
{
    private final int itemId;

    public SingleItemRequirement(int itemId)
    {
        this.itemId = itemId;
    }

    @Override
    public boolean fulfilledBy(int itemId)
    {
        return this.itemId == itemId;
    }

    @Override
    public boolean fulfilledBy(Item[] items)
    {
        for (Item item : items)
        {
            if (item.getId() == itemId)
            {
                return true;
            }
        }

        return false;
    }
}