package callmemaple.ticktrainer;

import callmemaple.ticktrainer.data.ResourceNode;
import callmemaple.ticktrainer.event.*;
import callmemaple.ticktrainer.data.Pickaxe;
import callmemaple.ticktrainer.data.TickingMethod;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Slf4j
public class PlayerManager
{
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private TickTrainerConfig config;

    @Inject
    private TickTracker tickTracker;

    @Nullable
    private WorldPoint previousLocation;

    @Getter
    @Accessors(fluent = true)
    private boolean hasPlayerMoved = false;
    private boolean hasPlayerMovedLastTick = false;

    @Getter
    @Nullable
    private GameObject targetObject = null;

    @Subscribe
    public void onGameTick(GameTick event)
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        hasPlayerMovedLastTick = hasPlayerMoved;
        hasPlayerMoved = !playerLocation.equals(previousLocation);
        if (hasPlayerMoved) {
            //log.info("player moved tick{} {} to {}", client.getTickCount(), previousLocation, playerLocation);
            previousLocation = playerLocation;
        }

        if (targetObject == null)
        {
            return;
        }

        // Object interactions require the user to already be on the tile
        // TODO not working for b2b with no move needed. is 1 tick off - idk what this means lol
        if (nextToTargetedNode() && !hasPlayerMoved)
        {
            eventBus.post(new ResourceInteract(targetObject));
            targetObject = null;
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        if (event.getGameObject() == targetObject)
        {
            log.info("target node despawned:{}", event.getGameObject());
            targetObject = null;
        }
    }

    @Subscribe
    public void onTickMethodClick(TickingClick click)
    {
        targetObject = null;
    }

    @Subscribe
    public void onNodeClick(ResourceClick click)
    {
        targetObject = click.getNode();
    }

    @Subscribe
    public void onInterruptClick(Interrupt click)
    {
        log.info("menuclick_interrupt tick:{} {}->{} timestamp:{}", client.getTickCount(), click.getMenuOption(), click.getMenuTarget(), System.currentTimeMillis());
        targetObject = null;
    }

    /**
     * @param method the ticking method
     * @return true if player is in tick animation else false
     */
    public boolean isPlayerInTickAnimation(TickingMethod method)
    {
        Player player = client.getLocalPlayer();
        log.info("check animation {} {}", player.getAnimation(), player.getAnimationFrame());
        int animationId = player.getAnimation();
        return animationId == method.getAnimationId();
    }

    /**
     * Get the current pickaxe the player would use base on their equipment and inventory.
     * @return the pickaxe to be used
     * @see Pickaxe
     */
    public Pickaxe getPickaxe()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (equipment == null || inventory == null)
        {
            return Pickaxe.UNKNOWN;
        }
        int miningLevel = client.getBoostedSkillLevel(Skill.MINING);
        Item[] allItems = ArrayUtils.addAll(equipment.getItems(), inventory.getItems());
        return Pickaxe.findPickaxeFromItems(miningLevel, allItems);
    }

    /**
     * Returns whether the player moved in the previous game tick or not.
     * @return true if player moved, else false
     */
    public boolean hasPlayerMovedLastTick()
    {
        return hasPlayerMovedLastTick;
    }

    public boolean nextToAnyResourceNode()
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        Set<WorldPoint> adjacentTiles = Stream.of(
                playerLocation.dx(1),
                playerLocation.dx(-1),
                playerLocation.dy(1),
                playerLocation.dy(-1)).collect(Collectors.toCollection(HashSet::new));

        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();
        int z = client.getPlane();
        for (int x = 0; x < Constants.SCENE_SIZE; ++x)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y)
            {
                Tile tile = tiles[z][x][y];
                if (!adjacentTiles.contains(tile.getWorldLocation()))
                {
                    continue;
                }

                //log.info("found adjacentTile {},{}", tile.getWorldLocation().getX(), tile.getWorldLocation().getY());

                GameObject[] gameObjects = tile.getGameObjects();
                if (gameObjects == null)
                {
                    continue;
                }

                for (GameObject gameObject : gameObjects)
                {
                    if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation()))
                    {
                        // Check if object valid rock, tree, etc
                        if (ResourceNode.isNode(gameObject.getId()))
                        {
                            log.info("found object:{} {}", gameObject.getId(), gameObject);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean nextToTargetedNode()
    {
        if (targetObject == null)
        {
            return false;
        }

        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        Set<WorldPoint> adjacentTiles = Stream.of(
                playerLocation.dx(1),
                playerLocation.dx(-1),
                playerLocation.dy(1),
                playerLocation.dy(-1)).collect(Collectors.toCollection(HashSet::new));

        Point minPoint = targetObject.getSceneMinLocation();
        Point maxPoint = targetObject.getSceneMaxLocation();
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
        log.info("distance to node {} player:{},{}", targetObject.getWorldLocation().distanceTo2D(playerLocation), playerLocation.getRegionX(), playerLocation.getRegionY());
        return false;
    }
}
