package callmemaple.ticktrainer.event;

import callmemaple.ticktrainer.data.TickMethod;
import lombok.Data;

@Data
public class TickMethodStart
{
    private final TickMethod method;
}
