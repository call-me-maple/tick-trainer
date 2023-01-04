package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.TickMethods;
import lombok.Data;

@Data
public class TickMethodClick
{
    private final TickMethods method;
    private final int predictedTick;
}
