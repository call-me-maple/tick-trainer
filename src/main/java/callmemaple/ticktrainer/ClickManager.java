package callmemaple.ticktrainer;

import callmemaple.ticktrainer.data.Pickaxe;
import callmemaple.ticktrainer.data.ResourceNode;
import callmemaple.ticktrainer.data.TickMethod;
import callmemaple.ticktrainer.event.Click;
import callmemaple.ticktrainer.event.NodeClick;
import callmemaple.ticktrainer.event.PredictedClick;
import callmemaple.ticktrainer.event.TickMethodClick;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;

import static callmemaple.ticktrainer.data.TickMethod.UNKNOWN;
import static callmemaple.ticktrainer.data.TickMethod.findInventoryAction;
import static net.runelite.api.MenuAction.*;

@Singleton
@Slf4j
public class ClickManager
{
    private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
            WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER,
            WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET,
            WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION, WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION);

    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private TickManager tickManager;

    private PredictedClick[] clicks;

    // TODO make this the object in scene? not handling all interruptions
    @Getter
    private int targetedNode = -1;

    public ClickManager()
    {
        clicks = new PredictedClick[]{};
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {

        for (PredictedClick click : clicks)
        {
            log.info("{}", click);
        }
        int currentTick = client.getTickCount();
        Arrays.stream(clicks)
                .filter(click -> click.getPredictedTick() == currentTick)
                .sorted((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()))
                .forEach(click -> eventBus.post(click.getClick()));

        clicks = Arrays.stream(clicks).filter(click -> click.getPredictedTick() != currentTick).toArray(PredictedClick[]::new);
        if (clicks.length != 0)
            log.info("waiting till next tick");
        for (PredictedClick click : clicks)
        {
            log.info("{}", click);
        }
    }


    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click)
    {
        switch (click.getMenuAction())
        {
            case WIDGET_TARGET_ON_WIDGET:
                isTickMethodClick(click);
                log.info("menuclick tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), click.getMenuOption(), click.getMenuTarget(), click.getId(), System.currentTimeMillis());
                break;
            case GAME_OBJECT_FIRST_OPTION:
            case GAME_OBJECT_SECOND_OPTION:
            case GAME_OBJECT_THIRD_OPTION:
            case GAME_OBJECT_FOURTH_OPTION:
            case GAME_OBJECT_FIFTH_OPTION:
                log.info("menuclick tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), click.getMenuOption(), click.getMenuTarget(), click.getId(), System.currentTimeMillis());
                isResourceNodeClick(click);
                if (ResourceNode.isNode(click.getId()))
                {
                    targetedNode = click.getId();
                }
                break;
            default:
                if (MENU_ACTIONS_INTERRUPT.contains(click.getMenuAction()))
                {
                    log.info("menuclick_interrupt tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), click.getMenuOption(), click.getMenuTarget(), click.getId(), System.currentTimeMillis());
                    targetedNode = -1;
                }
        }
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
        PredictedClick predictedClick = new PredictedClick(tickMethodClick, tickManager.getPredictedTick(), System.currentTimeMillis());
        //TODO send now it this tick, else add to thing send next ontick
        ArrayUtils.add(clicks, predictedClick);
        return method;
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
        PredictedClick predictedClick = new PredictedClick(click, tickManager.getPredictedTick(), System.currentTimeMillis());
        ArrayUtils.add(clicks, predictedClick);
        log.info("is resource node click");
    }
}
