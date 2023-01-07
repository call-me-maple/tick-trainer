package callmemaple.ticktrainer.event;

import lombok.Data;

@Data
public class InterruptClick
{
    private final String menuTarget;
    private final String menuOption;
}
