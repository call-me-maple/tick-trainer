package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.TickMethods;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static callmemaple.ticktrainer.Error.*;
import static callmemaple.ticktrainer.SkillingCycleStatus.*;

@Singleton
@Slf4j
public class SkillingCycle
{
    @Inject
    private Client client;

    @Inject
    private TickTrainerConfig config;

    @Inject
    private TickManager tickManager;

    @Inject
    private PlayerState playerState;

    @Getter private int nextSkillingTick;
    @Getter private int skillingTickStart;
    @Getter private TickMethods method;
    @Getter private SkillingCycleStatus status;
    @Getter private final Set<Error> errors;

    SkillingCycle()
    {
        status = IDLE;
        errors = new HashSet<>();
        nextSkillingTick = -1;
        skillingTickStart = -1;
        method = TickMethods.UNKNOWN;
    }

    public void startCycle(TickMethodClick tickMethodClick)
    {
    }

    public int getTickCycle()
    {
        // Show 0 if in the future
        return client.getTickCount() - skillingTickStart;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        /*
		validate states?
		add any errors
			too late, bad move, no node, another skill cycle
		 */
        if (status != ON_CYCLE)
        {
            return;
        }
        switch (getTickCycle())
        {
            case 0:
                if (playerState.hasPlayerMoved())
                {
                    if (!playerState.isPlayerInTickAnimation(method))
                    {
                        addError(USED_ITEM_TOO_SOON);
                    }
                    if (!playerState.nextToResourceNode())
                    {
                        addError(INVALID_LOCATION);
                    }
                } else
                {
                    addError(LATE_MOVE);
                }
                break;
            case 1:
                break;
        }
        if (nextSkillingTick <= client.getTickCount())
        {
            status = IDLE;
        }
    }

    public void addError(Error error)
    {
        log.info("error: {} on tick {}", error, client.getTickCount());
        errors.add(error);
        status = ERROR;
    }

    @Subscribe
    public void onTickMethodClick(TickMethodClick click)
    {

        // TODO check if can start cycle
        errors.clear();
        method = click.getMethod();
        skillingTickStart = click.getPredictedTick();
        nextSkillingTick = skillingTickStart + method.getSkillingTick();
        log.info("tick:{} nextSkillingTick:{} method:{}", client.getTickCount(), nextSkillingTick, method);
        status = ON_CYCLE;
    }
}
