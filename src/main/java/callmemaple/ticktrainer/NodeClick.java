package callmemaple.ticktrainer;

import lombok.Data;
import net.runelite.api.GameObject;

@Data
public class NodeClick
{
    private final GameObject node;
    private final int predictedTick;
}
