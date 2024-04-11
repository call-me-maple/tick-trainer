package callmemaple.ticktrainer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.GameObject;

@Getter
@AllArgsConstructor
public class ResourceInteract
{
    private final GameObject node;
}