package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.Pickaxe;
import callmemaple.ticktrainer.item.TickMethods;
import com.google.common.collect.ImmutableSortedSet;
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static callmemaple.ticktrainer.item.TickMethods.UNKNOWN;
import static callmemaple.ticktrainer.item.TickMethods.findInventoryAction;
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
    private SkillingCycle skillingCycle;

    @Nullable
    private WorldPoint previousLocation;
    private boolean hasPlayerMoved;
    private GameObject targetedNode;

    private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
            WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER,
            WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET,
            WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION, WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION);

    @Inject
    public PlayerState()
    {
        previousLocation = null;
        hasPlayerMoved = false;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
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

                break;
            default:
                if (MENU_ACTIONS_INTERRUPT.contains(event.getMenuAction()))
                {
                    log.info("menuclick_interrupt tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), event.getMenuOption(), event.getMenuTarget(), event.getId(), System.currentTimeMillis());
                    //skillingCycle.setObjectTarget(-1);
                }
        }
    }

    /**
     * @param method the ticking method
     * @return true if player is in tick animation else false
     */
    public boolean isPlayerInTickAnimation(TickMethods method)
    {
        Player player = client.getLocalPlayer();
        log.info("check animation {} {}", player.getAnimation(), player.getAnimationFrame());
        int animationId = player.getAnimation();
        return animationId == method.getAnimationId();
    }

    /**
     * If the menu option clicked is a valid tick method then start the cycle
     *
     * @return
     */
    private TickMethods isTickMethodClick(MenuOptionClicked evt)
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
        int inventoryIndex = evt.getParam0();
        Item targetedItem = inventory.getItem(inventoryIndex);
        if (targetedItem == null)
        {
            return UNKNOWN;
        }
        TickMethods method = findInventoryAction(selectedItem, targetedItem, inventory.getItems());
        int predictedTick = client.getTickCount();
        long predictedTime = tickManager.getNextTickTime();
        long currentTime = System.currentTimeMillis();
        if (predictedTime - currentTime < 150)
        {
            predictedTime += tickManager.getAverageTickTime();
            predictedTick += 1;
        }
        log.info("predicting:{} timestamp:{}", predictedTime, currentTime);
        TickMethodClick tickMethodClick = new TickMethodClick(method, playerLocation, tickClicked, currentTime, predictedTime, predictedTick);
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

    // TODO make work for object bigger than 1x1
    public boolean nextToResourceNode()
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
