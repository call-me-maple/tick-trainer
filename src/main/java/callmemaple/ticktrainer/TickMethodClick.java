package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.TickMethods;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
public class TickMethodClick
{
    private final TickMethods method;
    private final WorldPoint startLocation;
    private final int tick;
    private final long timestamp;
    private final long predictedTickTime;
    private final int predictedTick;
    //private final long predictedTick;
}
