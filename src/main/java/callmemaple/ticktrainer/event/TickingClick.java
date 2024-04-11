package callmemaple.ticktrainer.event;

import callmemaple.ticktrainer.data.TickingMethod;
import lombok.Getter;

@Getter
public class TickingClick extends Click
{
    private final TickingMethod method;

    public TickingClick(int clickedOn, TickingMethod method)
    {
        super(clickedOn);
        this.method = method;
    }

    public int getResourceTick() {
        return getProcessTick() + method.getCycleLength();
    }
}
