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
public class PlayerState
{
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private TickTrainerConfig config;

    @Inject
    private TickManager tickManager;

    @Inject
    private TickMethodCycle tickMethodCycle;

    @Nullable
    private WorldPoint previousLocation;
    private boolean hasPlayerMoved;
    private boolean hasPlayerMovedLastTick;

    // TODO make this the object in scene? not handling all interruptions
    @Getter
    private int targetedNode;

    private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
            WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER,
            WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET,
            WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION, WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION);

    @Inject
    public PlayerState()
    {
        previousLocation = null;
        hasPlayerMoved = false;
        hasPlayerMovedLastTick = false;
        targetedNode = -1;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        hasPlayerMovedLastTick = hasPlayerMoved;
        hasPlayerMoved = !playerLocation.equals(previousLocation);
        if (hasPlayerMoved) {
            log.info("player moved tick{} {} to {}", client.getTickCount(), previousLocation, playerLocation);
            previousLocation = playerLocation;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        switch (event.getMenuAction())
        {
            case WIDGET_TARGET_ON_WIDGET:
                isTickMethodClick(event);
                log.info("menuclick tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), event.getMenuOption(), event.getMenuTarget(), event.getId(), System.currentTimeMillis());
                break;
            case GAME_OBJECT_FIRST_OPTION:
            case GAME_OBJECT_SECOND_OPTION:
            case GAME_OBJECT_THIRD_OPTION:
            case GAME_OBJECT_FOURTH_OPTION:
            case GAME_OBJECT_FIFTH_OPTION:
                log.info("menuclick tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), event.getMenuOption(), event.getMenuTarget(), event.getId(), System.currentTimeMillis());
                isResourceNodeClick(event);
                if (ResourceNode.isNode(event.getId()))
                {
                    targetedNode = event.getId();
                }
                break;
            default:
                if (MENU_ACTIONS_INTERRUPT.contains(event.getMenuAction()))
                {
                    log.info("menuclick_interrupt tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), event.getMenuOption(), event.getMenuTarget(), event.getId(), System.currentTimeMillis());
                    targetedNode = -1;
                }
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

    private void isResourceNodeClick(MenuOptionClicked event)
    {
        int clickedId = event.getId();
        if (!ResourceNode.isNode(clickedId))
        {
            return;
        }

        int z = client.getPlane();
        int x = event.getParam0();
        int y = event.getParam1();
        Tile sceneTile = client.getScene().getTiles()[z][x][y];

        GameObject clickedGameObject = Arrays.stream(sceneTile.getGameObjects())
                .filter(gameObject -> gameObject != null && gameObject.getId() == clickedId)
                .findFirst().orElse(null);
        if (clickedGameObject == null)
        {
            return;
        }
        int predictedTick = tickManager.getPredictedTick();
        NodeClick click = new NodeClick(clickedGameObject, predictedTick);
        eventBus.post(click);
        log.info("is resource node click");
    }




    /**
     * If the menu option clicked is a valid tick method then start the cycle
     *
     * @return
     */
    private TickMethod isTickMethodClick(MenuOptionClicked event)
    {
        int tickClicked = client.getTickCount();
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return UNKNOWN;
        }

        // the selected widget is the item selected before the click (the white outlined item)
        Widget widgetSelected = client.getSelectedWidget();
        if (!client.isWidgetSelected() || widgetSelected == null)
        {
            return UNKNOWN;
        }
        Item selectedItem = new Item(widgetSelected.getItemId(), widgetSelected.getItemQuantity());

        // getParam0() is the index of the item widget targeted
        int inventoryIndex = event.getParam0();
        Item targetedItem = inventory.getItem(inventoryIndex);
        if (targetedItem == null)
        {
            return UNKNOWN;
        }
        TickMethod method = findInventoryAction(selectedItem, targetedItem, inventory.getItems());
        log.info("predictingTick:{} currentTick:{}", tickManager.getPredictedTick(), client.getTickCount());
        TickMethodClick tickMethodClick = new TickMethodClick(method, tickManager.getPredictedTick());
        eventBus.post(tickMethodClick);
        return method;
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
