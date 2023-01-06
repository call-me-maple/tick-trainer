package callmemaple.ticktrainer.event;

import callmemaple.ticktrainer.data.TickMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TickMethodStart
{
    private final TickMethod method;
}
