package callmemaple.ticktrainer.data.item;

public class ItemRequirements
{

    public static SingleItemRequirement item(int itemId)
    {
        return new SingleItemRequirement(itemId);
    }

    public static AnyRequirementCollection any(ItemRequirement... requirements)
    {
        return new AnyRequirementCollection(requirements);
    }
}

