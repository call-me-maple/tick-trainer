package callmemaple.ticktrainer.data.item;

import net.runelite.api.Client;
import net.runelite.api.Item;

public interface ItemRequirement
{
        boolean fulfilledBy(int itemId);

        boolean fulfilledBy(Item[] items);
}
