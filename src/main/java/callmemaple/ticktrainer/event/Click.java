package callmemaple.ticktrainer.event;

import lombok.Data;

@Data
public class Click
{
    private final int clickedOn;

    public Click(int clickedOn)
    {
        this.clickedOn = clickedOn;
    }
    public int getProcessTick() {
        return clickedOn + 1;
    }
}
