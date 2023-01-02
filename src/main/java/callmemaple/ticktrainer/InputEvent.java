package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.TickMethods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
@Getter
public class InputEvent
{
    private final TickMethods method;
    private final WorldPoint startLocation;
    private final int tick;

    @Override
    public String toString()
    {
        return method.name() + " on tick " + tick + " at "+ startLocation.toString();
    }
}
