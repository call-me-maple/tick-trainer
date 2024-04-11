package callmemaple.ticktrainer;

import callmemaple.ticktrainer.event.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class ResourceTickManager
{
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private PlayerManager playerManager;

    @Getter private int nextSkillingTick;
    @Getter private int skillingTickStart;


    @Subscribe
    public void onTickMethodClick(TickingClick click)
    {
        if (click.getResourceTick() < nextSkillingTick)
        {
            log.info("cant start new TICK METHOD cycle already in a skilling cycle");
            return;
        }
        skillingTickStart = click.getProcessTick();
        nextSkillingTick = skillingTickStart + click.getMethod().getCycleLength();
        log.info("tick:{} nextSkillingTick:{} method:{}", click.getProcessTick(), nextSkillingTick, click.getMethod());
        eventBus.post(new TickingStart(click));
    }

    @Subscribe
    public void onObjectInteraction(ResourceInteract interact)
    {
        skillingTickStart = client.getTickCount();
        nextSkillingTick = skillingTickStart + (int) Math.ceil(playerManager.getPickaxe().getCycle());
        log.info("starting skilling tick start:{} end:{}", skillingTickStart, nextSkillingTick);
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        if (client.getTickCount() == nextSkillingTick)
        {
            eventBus.post(new ResourceTick());
        }
    }

    public boolean inSkillingTick()
    {
        return client.getTickCount() < nextSkillingTick;
    }

    public int remainingSkillingTicks()
    {
        return nextSkillingTick - client.getTickCount();
    }
}
