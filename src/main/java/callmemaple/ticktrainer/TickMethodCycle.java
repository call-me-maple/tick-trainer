package callmemaple.ticktrainer;

import callmemaple.ticktrainer.data.Error;
import callmemaple.ticktrainer.data.SkillingCycleStatus;
import callmemaple.ticktrainer.event.NodeCycleStart;
import callmemaple.ticktrainer.event.SkillingCycleEnd;
import callmemaple.ticktrainer.event.TickMethodClick;
import callmemaple.ticktrainer.event.TickMethodStart;
import callmemaple.ticktrainer.data.TickMethod;
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
import static callmemaple.ticktrainer.data.SkillingCycleStatus.*;

@Singleton
@Slf4j
public class TickMethodCycle
{
    @Inject
    private Client client;

    @Inject
    private TickTrainerConfig config;

    @Inject
    private TickManager tickManager;

    @Inject
    private PlayerState playerState;

    @Inject
    private SkillingCycle skillingCycle;

    @Getter private TickMethod method;
    @Getter private SkillingCycleStatus status;
    @Getter private final Set<Error> errors;

    TickMethodCycle()
    {
        status = IDLE;
        errors = new HashSet<>();
        method = TickMethod.UNKNOWN;
    }

    public void startCycle(TickMethodClick tickMethodClick)
    {
    }

    public int getTickCycle()
    {
        return client.getTickCount() - skillingCycle.getSkillingTickStart();
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
                    if (!playerState.nextToAnyResourceNode())
                    {
                        addError(INVALID_LOCATION);
                    }
                } else
                {
                    addError(LATE_MOVE);
                }
                break;
            case 1:
                if (playerState.getTargetedNode() < 0)
                {
                    addError(NO_INTERACTION);
                }
                break;
        }
        if (skillingCycle.getNextSkillingTick() <= client.getTickCount())
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
    public void onTickMethodStart(TickMethodStart start)
    {
        errors.clear();
        method = start.getMethod();
        log.info("tick:{} method:{}", client.getTickCount(), method);
        status = ON_CYCLE;
    }

    @Subscribe
    public void onNodeCycleStart(NodeCycleStart start)
    {
        log.info("locked out of tick method cycle till {}", start.getNextSkillingTick());
        status = LOCKED_OUT;
    }

    @Subscribe
    public void onSkillingCycleEnd(SkillingCycleEnd end)
    {
        status = IDLE;
    }

}
