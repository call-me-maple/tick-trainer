package callmemaple.ticktrainer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.GameObject;

@Getter
@AllArgsConstructor
public class NodeCycleStart
{
    private final GameObject node;
    private final int nextSkillingTick;
}
