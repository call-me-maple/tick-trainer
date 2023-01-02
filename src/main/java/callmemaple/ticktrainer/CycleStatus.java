package callmemaple.ticktrainer;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.eventbus.Subscribe;

import java.util.SplittableRandom;

public enum CycleStatus
{
    WAITING_FOR_ANIMATION("waiting for animation"),
    ON_CYCLE(""),
    ERROR,
    IDLE(""),
    LOCKED_OUT,
    ;

    @Getter
    @Setter
    private String message = "";

    CycleStatus()
    {
    }

    CycleStatus(String message)
    {
        this.message = message;
    }
}
