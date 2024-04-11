package callmemaple.ticktrainer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TickingStart
{
    private final TickingClick click;
}
