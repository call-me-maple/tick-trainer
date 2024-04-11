package callmemaple.ticktrainer.data;

import callmemaple.ticktrainer.data.ui.DisplayMode;
import lombok.Getter;

// Link to readme?
@Getter
public enum Error
{
    STARTED_SKILL_CYCLE("started a regular mining cycle", ":p"),
    LATE_MOVE("didn't move in time", ":p"),
    INVALID_LOCATION("not next to a node", ":p"),
    USED_ITEM_TOO_SOON("used inv item too soon", ":p"),
    NO_INTERACTION("didn't click the node?", ":p"),
    ;

    private final String minimalMessage;
    private final String verboseMessage;

    Error(String minimalMessage, String verboseMessage)
    {
        this.minimalMessage = minimalMessage;
        this.verboseMessage = verboseMessage;
    }

    public String getMessage(DisplayMode displayMode)
    {
        if (displayMode == DisplayMode.VERBOSE_EMPTY)
        {
            return verboseMessage;
        }
        return minimalMessage;
    }
}