package callmemaple.ticktrainer.event;

import lombok.Data;
import net.runelite.api.GameObject;

@Data
public class NodeClick
{
    private final GameObject node;
    private final int predictedTick;
    private boolean consumed = false;
}
