package callmemaple.ticktrainer.event;

import callmemaple.ticktrainer.data.TickMethod;
import lombok.Data;

@Data
public class TickMethodClick
{
    private final TickMethod method;
    private final int predictedTick;
}
