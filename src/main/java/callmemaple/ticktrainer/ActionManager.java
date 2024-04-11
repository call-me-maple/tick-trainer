package callmemaple.ticktrainer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class ActionManager
{
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private TickTracker tickTracker;
}
