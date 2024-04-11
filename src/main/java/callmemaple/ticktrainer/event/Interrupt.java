package callmemaple.ticktrainer.event;

import lombok.Getter;

@Getter
public class Interrupt extends Click
{
    private final String menuTarget;
    private final String menuOption;

    public Interrupt(int clickedOn, String menuTarget, String menuOption)
    {
        super(clickedOn);
        this.menuTarget = menuTarget;
        this.menuOption = menuOption;

    }
}
