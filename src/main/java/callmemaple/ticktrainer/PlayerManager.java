package callmemaple.ticktrainer;

import callmemaple.ticktrainer.event.NodeClick;
import callmemaple.ticktrainer.event.TickMethodClick;
import callmemaple.ticktrainer.data.Pickaxe;
import callmemaple.ticktrainer.data.ResourceNode;
import callmemaple.ticktrainer.data.TickMethod;
import com.google.common.collect.ImmutableSortedSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static callmemaple.ticktrainer.data.TickMethod.UNKNOWN;
import static callmemaple.ticktrainer.data.TickMethod.findInventoryAction;
import static net.runelite.api.MenuAction.*;

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
    private TickManager tickManager;

    @Nullable
    private WorldPoint previousLocation;
    private boolean hasPlayerMoved = false;
    private boolean hasPlayerMovedLastTick = false;

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
    }


    /**
     * @param method the ticking method
     * @return true if player is in tick animation else false
     */
    public boolean isPlayerInTickAnimation(TickMethod method)
    {
        Player player = client.getLocalPlayer();
        log.info("check animation {} {}", player.getAnimation(), player.getAnimationFrame());
        int animationId = player.getAnimation();
        return animationId == method.getAnimationId();
    }

    /**
     * Get the current pickaxe the player would use base on their equipment and inventory.
     * TODO check if required level
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
     * Returns whether the player moved in the current game tick or not.
     * @return true if player moved, else false
     */
    public boolean hasPlayerMoved()
    {
        return hasPlayerMoved;
    }

    /**
     * Returns whether the player moved in the previous game tick or not.
     * @return true if player moved, else false
     */
    public boolean hasPlayerMovedLastTick()
    {
        return hasPlayerMovedLastTick;
    }

    // TODO make work for object bigger than 1x1
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
                        //log.info("found object "+gameObject.getId());
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
