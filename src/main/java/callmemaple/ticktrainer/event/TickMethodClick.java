package callmemaple.ticktrainer.event;

import callmemaple.ticktrainer.data.TickMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TickMethodClick extends Click
{
    private final TickMethod method;
    private final int tick;
}
