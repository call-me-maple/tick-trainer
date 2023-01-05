package callmemaple.ticktrainer.data;

import com.google.common.collect.ImmutableSortedSet;
import lombok.Getter;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public enum Pickaxe
{


    BRONZE_PICKAXE(8, 1, ItemID.BRONZE_PICKAXE),
    IRON_PICKAXE(7, 1, ItemID.IRON_PICKAXE),
    STEEL_PICKAXE(6, 6, ItemID.STEEL_PICKAXE),
    BLACK_PICKAXE(5, 11, ItemID.BLACK_PICKAXE),
    MITHRIL_PICKAXE(5, 21, ItemID.MITHRIL_PICKAXE),
    ADAMANT_PICKAXE(4, 31, ItemID.ADAMANT_PICKAXE),
    RUNE_PICKAXE(3, 41, ItemID.RUNE_PICKAXE, ItemID.GILDED_PICKAXE),
    // TODO defo missing some pickaxe variants
    DRAGON_PICKAXE(2.83f, 61, ItemID.DRAGON_PICKAXE, ItemID.DRAGON_PICKAXE_12797,
            ItemID.DRAGON_PICKAXE_OR, ItemID.DRAGON_PICKAXE_OR_25376, ItemID.INFERNAL_PICKAXE,
            ItemID.INFERNAL_PICKAXE_UNCHARGED, ItemID._3RD_AGE_PICKAXE, ItemID.INFERNAL_PICKAXE_OR, ItemID.INFERNAL_PICKAXE_UNCHARGED_25369),
    CRYSTAL_PICKAXE(2.75f, 71, ItemID.CRYSTAL_PICKAXE, ItemID.CRYSTAL_PICKAXE_23863, ItemID.CRYSTAL_PICKAXE_INACTIVE),
    UNKNOWN,
    ;

    @Getter
    private final float cycle;
    private final int miningLevel;
    private final ImmutableSortedSet<Integer> itemIds;

    Pickaxe()
    {
        cycle = Float.MAX_VALUE;
        miningLevel = 0;
        itemIds = ImmutableSortedSet.of();
    }

    Pickaxe(float cycle, int miningLevel, Integer... itemIds)
    {
        this.cycle = cycle;
        this.miningLevel = miningLevel;
        this.itemIds = ImmutableSortedSet.copyOf(itemIds);
    }

    public boolean isRng()
    {
        return cycle - (int) cycle != 0;
    }

    public static Pickaxe findPickaxeFromItems(int itemId)
    {
        return Arrays.stream(values())
                .sorted(Comparator.comparing(Pickaxe::getCycle))
                .filter(pickaxe -> pickaxe.itemIds.contains(itemId))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static Pickaxe findPickaxeFromItems(int miningLevel, Item[] items)
    {
        Pickaxe[] availablePickaxes = new Pickaxe[]{};
        for (Item item : items)
        {
            Pickaxe pickaxe = findPickaxeFromItems(item.getId());
            if (miningLevel >= pickaxe.miningLevel && pickaxe != UNKNOWN)
            {
                availablePickaxes = ArrayUtils.add(availablePickaxes, pickaxe);
            }
        }
        return Arrays.stream(availablePickaxes)
                .min(Comparator.comparing(Pickaxe::getCycle))
                .orElse(UNKNOWN);
    }
}





