package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.Pickaxe;
import callmemaple.ticktrainer.item.TickMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static callmemaple.ticktrainer.CycleStatus.*;
import static callmemaple.ticktrainer.Error.*;

@Singleton
@Slf4j
public class CycleState
{
    @Getter
    private Set<Error> errors;

    @Getter
    private WorldPoint startLocation;

    @Getter
    private CycleStatus status;

    @Inject
    private Client client;

    @Inject
    private TickTrainerConfig config;

    @Getter
    private int tickCount = 0;
    @Getter
    private float lockedOutTimer = 0;
    private boolean active;
    @Getter
    private TickMethods method;

    @Getter
    @Setter
    // TODO change this to be the location or the object itself
    private int objectTarget;

    CycleState()
    {
        status = IDLE;
    }

    public void startCycle(InputEvent inputEvent)
    {
        // Can't start a new cycle while locked out
        // TODO i think you can delay lock out longing with retrying?
        if (status == LOCKED_OUT)
        {
            return;
        }

        active = true;
        method = inputEvent.getMethod();
        startLocation = inputEvent.getStartLocation();
        errors = new HashSet<>();
        status = WAITING_FOR_ANIMATION;
        tickCount = 0;
        objectTarget = -1;
    }

    public void increment()
    {
        tickCount++;
        switch (status)
        {
            case WAITING_FOR_ANIMATION:
                if (tickCount == 2)
                {
                    addError(USED_ITEM_TOO_SOON);
                } else if (tickCount == 3)
                {
                    status = IDLE;
                }
                break;
            case ON_CYCLE:
                log.info("incrementing current cycle, totalTick {} cycleTick {}", client.getTickCount(), tickCount);
                //log.info("player loc {} started at {}", getPlayerLocation(), startLocation);
                switch (tickCount)
                {
                    case 1:
                        // Need to have moved since start of cycle
                        if (!hasPlayerMoved())
                        {
                            addError(LATE_MOVE);
                        }

                        break;
                    case 2:
                        if (objectTarget < 0)
                        {
                            addError(NO_INTERACTION);
                        }
                        // Need to be next to node
                        if (!nextToResourceNode())
                        {
                            addError(INVALID_LOCATION);
                        }
                        break;
                    default:
                        status = IDLE;
                }
                break;
            case ERROR:
                // if moved late and clicked rock then locked out
                if (errors.contains(LATE_MOVE) && (objectTarget >= 0))
                {
                    errors.add(STARTED_SKILL_CYCLE);
                    status = LOCKED_OUT;
                    Pickaxe pickaxe = getPlayerPickaxe();
                    lockedOutTimer = pickaxe.getCycle();
                }

                if (tickCount >= config.errorTimeout())
                {
                    status = IDLE;
                }
                break;
            case LOCKED_OUT:
                lockedOutTimer--;
                if (lockedOutTimer <= 0)
                {
                    status = IDLE;
                }
                break;
        }
    }

    public boolean hasPlayerMoved()
    {
        if (startLocation == null)
        {
            return false;
        }
        WorldPoint currentLocation = getPlayerLocation();
        return !startLocation.equals(currentLocation);
    }

    private void addError(Error error)
    {
        log.info("error: {} on tick {}", error, tickCount);
        errors.add(error);
        this.status = ERROR;
        this.tickCount = 0;
    }

    public Pickaxe getPlayerPickaxe()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        if (equipment == null || inventory == null)
        {
            return Pickaxe.UNKNOWN;
        }

        Item[] allItems = ArrayUtils.addAll(equipment.getItems(), inventory.getItems());
        return Pickaxe.findPickaxeFromItems(allItems);
    }

    public boolean isPlayerInTickAnimation()
    {
        int animationId = client.getLocalPlayer().getAnimation();
        return animationId == method.getAnimationId();
    }

    public WorldPoint getPlayerLocation()
    {
        return client.getLocalPlayer().getWorldLocation();
    }

    // TODO make work for object bigger than 1x1
    private boolean nextToResourceNode()
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

                log.info("found adjacentTile {},{}", tile.getWorldLocation().getX(), tile.getWorldLocation().getY());

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
                        log.info("found object "+gameObject.getId());
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean isActive()
    {
        return active;
    }

    public int getTotalTick()
    {
        return client.getTickCount();
    }

    public void confirmAnimation()
    {
        status = ON_CYCLE;
        tickCount = 0;
    }
}
