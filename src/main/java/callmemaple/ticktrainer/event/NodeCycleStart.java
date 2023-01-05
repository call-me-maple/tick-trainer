package callmemaple.ticktrainer.event;

import lombok.Data;
import net.runelite.api.GameObject;

@Data
public class NodeCycleStart
{
    private final GameObject node;
    private final int nextSkillingTick;
}
