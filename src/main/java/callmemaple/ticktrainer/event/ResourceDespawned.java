package callmemaple.ticktrainer.event;

import lombok.Data;
import net.runelite.api.GameObject;

@Data
public class ResourceDespawned
{
    private final GameObject node;
}
