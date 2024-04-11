package callmemaple.ticktrainer.event;

import lombok.Getter;
import net.runelite.api.GameObject;

@Getter
public class ResourceClick extends Click
{
    private final GameObject node;

    public ResourceClick(int clickedOn, GameObject node)
    {
        super(clickedOn);
        this.node = node;
    }
}
