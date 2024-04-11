package callmemaple.ticktrainer;

import callmemaple.ticktrainer.data.ResourceNode;
import callmemaple.ticktrainer.data.TickingMethod;
import callmemaple.ticktrainer.event.*;
import com.google.common.collect.ImmutableSortedSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;

import static callmemaple.ticktrainer.data.TickingMethod.findInventoryAction;
import static net.runelite.api.MenuAction.*;

@Singleton
@Slf4j
public class ClickManager
{
    private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
            WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER, WIDGET_TARGET_ON_NPC,
            WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET, WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION,
            WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION, GAME_OBJECT_FIRST_OPTION,
            GAME_OBJECT_SECOND_OPTION, GAME_OBJECT_THIRD_OPTION, GAME_OBJECT_FOURTH_OPTION, GAME_OBJECT_FIFTH_OPTION);

    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private TickTracker tickTracker;

    private final Queue<Click> possibleClicks;

    public ClickManager()
    {
        possibleClicks = new LinkedList<>();
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        int currentTick = client.getTickCount();

        Click click = possibleClicks.peek();
        if (click == null)
        {
            log.info("nothing to processes this currentTick:{}", currentTick);
            return;
        }

        Click gameWorldClick = null;
        for (int i = 0; i < possibleClicks.size(); i++)
        {
            click = possibleClicks.poll();
            if (click instanceof TickingClick) {
                eventBus.post(click);
            }
            if (click instanceof ResourceClick) {
                gameWorldClick = click;
            }
            if (click instanceof Interrupt) {
                gameWorldClick = null;
            }
        }

        if (gameWorldClick != null) {
            eventBus.post(gameWorldClick);
        }
    }


    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click)
    {
        // TODO i think the ordering should be diff
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
                break;
            default:
                if (MENU_ACTIONS_INTERRUPT.contains(click.getMenuAction()))
                {
                    possibleClicks.add(new Interrupt(tickTracker.getPredictedTick(), click.getMenuTarget(), click.getMenuOption()));
                }
        }
    }


    /**
     * If the menu option clicked is a valid tick method then start the cycle
     */
    private void isTickMethodClick(MenuOptionClicked event)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return;
        }

        // the selected widget is the item selected before the click (the white outlined item)
        Widget widgetSelected = client.getSelectedWidget();
        if (!client.isWidgetSelected() || widgetSelected == null)
        {
            return;
        }
        Item selectedItem = new Item(widgetSelected.getItemId(), widgetSelected.getItemQuantity());

        // getParam0() is the index of the item widget targeted
        int inventoryIndex = event.getParam0();
        Item targetedItem = inventory.getItem(inventoryIndex);
        if (targetedItem == null)
        {
            return;
        }
        TickingMethod method = findInventoryAction(selectedItem, targetedItem, inventory.getItems());
        if (method == TickingMethod.UNKNOWN)
        {
            return;
        }
        log.info("predictingTick:{} currentTick:{}", tickTracker.getPredictedTick(), client.getTickCount());
        possibleClicks.add(new TickingClick(tickTracker.getPredictedTick(), method));

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
        possibleClicks.add(new ResourceClick(tickTracker.getPredictedTick(), clickedGameObject));
    }
}
