package callmemaple.ticktrainer.method;

import callmemaple.ticktrainer.*;
import callmemaple.ticktrainer.data.Error;
import callmemaple.ticktrainer.event.ResourceTick;
import callmemaple.ticktrainer.event.TickingStart;
import callmemaple.ticktrainer.data.TickingMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static callmemaple.ticktrainer.data.Error.*;
import static callmemaple.ticktrainer.method.DoubleRollStatus.*;

@Singleton
@Slf4j
public class DoubleRollManager
{
    @Inject
    private Client client;

    @Inject
    private TickTrainerConfig config;

    @Inject
    private TickTracker tickTracker;

    @Inject
    private PlayerManager playerManager;

    @Inject
    private ClickManager clickManager;

    @Inject
    private ResourceTickManager resourceTickManager;

    @Getter private TickingMethod method;
    @Getter private DoubleRollStatus status;
    @Getter private final Set<Error> errors;

    @Getter private int tickStep;
    private int resourceTick;

    DoubleRollManager()
    {
        status = IDLE;
        errors = new HashSet<>();
        method = TickingMethod.UNKNOWN;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        if (status != ON_CYCLE)
        {
            return;
        }
        log.debug("tickStep:{}", tickStep);
        switch (tickStep)
        {
            case 0:
                if (playerManager.hasPlayerMoved())
                {
                    //if (!playerManager.isPlayerInTickAnimation(method))
                    //{
                    //    addError(USED_ITEM_TOO_SOON);
                    //}
                    if (!playerManager.nextToAnyResourceNode())
                    {
                        addError(INVALID_LOCATION);
                    }
                } else
                {
                    addError(LATE_MOVE);
                }
                break;
            case 1:
                if (!playerManager.nextToTargetedNode())
                {
                    addError(NO_INTERACTION);
                }
                break;
            //case 2:
            // should stop interacting with node and start again
        }
        tickStep++;
        if (resourceTick <= client.getTickCount())
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
    public void onTickMethodStart(TickingStart start)
    {
        errors.clear();
        method = start.getClick().getMethod();
        resourceTick = start.getClick().getResourceTick();
        tickStep = 0;
        log.info("tick:{} method:{} resources on:{}", client.getTickCount(), method, resourceTick);
        status = ON_CYCLE;
    }

    //@Subscribe
    //public void onNodeCycleStart(NodeCycleStart start)
    //{
    //    log.info("locked out of tick method cycle till {}", start.getNextSkillingTick());
    //    status = LOCKED_OUT;
    //}

    @Subscribe
    public void onSkillingCycleEnd(ResourceTick end)
    {
        status = IDLE;
    }

}
