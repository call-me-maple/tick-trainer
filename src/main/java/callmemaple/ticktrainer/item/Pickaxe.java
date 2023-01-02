package callmemaple.ticktrainer.item;

import com.google.common.collect.ImmutableSortedSet;
import lombok.Getter;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public enum Pickaxe
{


    BRONZE_PICKAXE(8, ItemID.BRONZE_PICKAXE),
    IRON_PICKAXE(7, ItemID.IRON_PICKAXE),
    STEEL_PICKAXE(6, ItemID.STEEL_PICKAXE),
    BLACK_PICKAXE(5, ItemID.BLACK_PICKAXE),
    MITHRIL_PICKAXE(5, ItemID.MITHRIL_PICKAXE),
    ADAMANT_PICKAXE(4, ItemID.ADAMANT_PICKAXE),
    RUNE_PICKAXE(3,ItemID.RUNE_PICKAXE, ItemID.GILDED_PICKAXE),
    // TODO defo missing some pickaxe variants
    DRAGON_PICKAXE(2.83f, ItemID.DRAGON_PICKAXE, ItemID.DRAGON_PICKAXE_12797, ItemID.DRAGON_PICKAXE_OR,
            ItemID.DRAGON_PICKAXE_OR_25376, ItemID.INFERNAL_PICKAXE, ItemID.INFERNAL_PICKAXE_UNCHARGED,
            ItemID._3RD_AGE_PICKAXE, ItemID.INFERNAL_PICKAXE_OR, ItemID.INFERNAL_PICKAXE_UNCHARGED_25369),
    CRYSTAL_PICKAXE(2.75f, ItemID.CRYSTAL_PICKAXE, ItemID.CRYSTAL_PICKAXE_23863, ItemID.CRYSTAL_PICKAXE_INACTIVE),
    UNKNOWN,
    ;

    @Getter
    private final float cycle;
    private final ImmutableSortedSet<Integer> itemIds;

    Pickaxe()
    {
        cycle = Float.MAX_VALUE;
        itemIds = ImmutableSortedSet.of();
    }

    Pickaxe(float cycle, Integer... itemIds)
    {
        this.cycle = cycle;
        this.itemIds = ImmutableSortedSet.copyOf(itemIds);
    }

    public static Pickaxe findPickaxeFromItems(int itemId)
    {
        return Arrays.stream(values())
                .sorted(Comparator.comparing(Pickaxe::getCycle))
                .filter(pickaxe -> pickaxe.itemIds.contains(itemId))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static Pickaxe findPickaxeFromItems(Item[] items)
    {
        Pickaxe[] availablePickaxes = new Pickaxe[]{};
        for (Item item : items)
        {
            availablePickaxes = ArrayUtils.add(availablePickaxes, findPickaxeFromItems(item.getId()));
        }
        return Arrays.stream(availablePickaxes)
                .min(Comparator.comparing(Pickaxe::getCycle))
                .orElse(UNKNOWN);
    }
}





