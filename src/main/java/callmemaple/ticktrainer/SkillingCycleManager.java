package callmemaple.ticktrainer;

import callmemaple.ticktrainer.event.*;
import callmemaple.ticktrainer.data.Pickaxe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j
public class SkillingCycleManager
{
    @Inject
    private PlayerManager playerManager;
    @Inject
    private TickingCycleManager tickingCycleManager;
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;

    @Getter private GameObject node;
    @Getter private int nextSkillingTick;
    @Getter private int skillingTickStart;
    @Getter private Pickaxe pickaxe;
    private int clickTick;

    @Nullable
    private NodeClick nodeClick;

    @Subscribe
    public void onNodeClick(NodeClick click)
    {
        node = click.getNode();
        clickTick = click.getTick();
        nodeClick = click;
        pickaxe = playerManager.getPickaxe();
    }

    @Subscribe
    public void onTickMethodClick(TickMethodClick click)
    {
        if (click.getTick() < nextSkillingTick)
        {
            log.info("cant start new TICK METHOD cycle already in a skilling cycle");
            return;
        }
        skillingTickStart = click.getTick();
        nextSkillingTick = skillingTickStart + click.getMethod().getSkillingTick();
        pickaxe = playerManager.getPickaxe();
        log.info("tick:{} nextSkillingTick:{} method:{}", client.getTickCount(), nextSkillingTick, click.getMethod());
        eventBus.post(new TickMethodStart(click.getMethod()));
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        if (client.getTickCount() == nextSkillingTick)
        {
            eventBus.post(new SkillingCycleEnd());
        }

        // Nothing to do
        if (nodeClick == null)
        {
            return;
        }
        // Check if the player moved in tick or not. Object interactions require the user to already be on the tile.
        if (nextToResourceNode() &&
                !playerManager.hasPlayerMovedLastTick() &&
                !playerManager.hasPlayerMoved() &&
                !inSkillingTick())
        {
            skillingTickStart = client.getTickCount();
            nextSkillingTick = skillingTickStart + (int) Math.ceil(pickaxe.getCycle());
            log.info("starting skilling tick start:{} end:{}", skillingTickStart, nextSkillingTick);
            eventBus.post(new NodeCycleStart(node, nextSkillingTick));
            nodeClick = null;
        } else
        {
            log.info("cant start new NODE skilling cycle");
        }
    }

    public boolean inSkillingTick()
    {
        log.info("tick:{} skillingTick:{}", client.getTickCount(), nextSkillingTick);
        return client.getTickCount() < nextSkillingTick;
    }

    public int remainingSkillingTicks()
    {
        return nextSkillingTick - client.getTickCount();
    }

    public boolean nextToResourceNode()
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        Set<WorldPoint> adjacentTiles = Stream.of(
                playerLocation.dx(1),
                playerLocation.dx(-1),
                playerLocation.dy(1),
                playerLocation.dy(-1)).collect(Collectors.toCollection(HashSet::new));
        Point minPoint = node.getSceneMinLocation();
        Point maxPoint = node.getSceneMaxLocation();
        int z = client.getPlane();
        for (int x = minPoint.getX(); x <= maxPoint.getX(); x++)
        {
            for (int y = minPoint.getY(); y <= maxPoint.getY(); y++)
            {
                WorldPoint wp = WorldPoint.fromScene(client, x, y, z);
                log.info("{} toPlayer:{}", wp, playerLocation.distanceTo2D(wp));
                if (adjacentTiles.contains(wp))
                {
                    return true;
                }
            }
        }
        log.info("distance to node {} player:{},{}", node.getWorldLocation().distanceTo2D(playerLocation), playerLocation.getRegionX(), playerLocation.getRegionY());
        return false;
    }
}
