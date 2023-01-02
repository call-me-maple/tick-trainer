package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.Pickaxe;
import callmemaple.ticktrainer.item.ResourceNodes;
import callmemaple.ticktrainer.item.TickMethods;
import callmemaple.ticktrainer.ui.TickTrainerOverlay;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

import static callmemaple.ticktrainer.CycleStatus.*;
import static net.runelite.api.MenuAction.*;
import static callmemaple.ticktrainer.item.TickMethods.UNKNOWN;
import static callmemaple.ticktrainer.item.TickMethods.findInventoryAction;

@Slf4j
@PluginDescriptor(
	name = "tick trainer"
)
public class TickTrainerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private TickTrainerOverlay overlay;

	@Inject
	private TickTrainerConfig config;

	@Inject CycleState cycleState;

	private List<InputEvent> clicks;
	private MenuOptionClicked test;

	private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
			WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER,
			WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET,
			WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION, WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION);

	@Override
	protected void startUp()
	{
		clicks = new ArrayList<>();
		overlayManager.add(overlay);
		log.info("tick trainer started!");
	}

	@Override
	protected void shutDown()
	{
		clicks.clear();
		overlayManager.remove(overlay);
		log.info("tick trainer stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		log.info("clicked consumed??? {}", test.isConsumed());
		cycleState.increment();
		/*
		on tick
			iterate over all input events of prev tick
				use, drop, mine, chop, move, etc
			determine action(s)
				inv cycle, move, interact, drop
			process 3t2r cycle
				if on cycle
					check if needed actions happened
					display results
				else
					start on move+3t actions
		 */
	}

	/**
	 * If the menu option clicked is a valid tick method then start the cycle
	 */
	private void startTickCycle(MenuOptionClicked evt)
	{
		int tickClicked = client.getTickCount();
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

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
		int inventoryIndex = evt.getParam0();
		Item targetedItem = inventory.getItem(inventoryIndex);
		if (targetedItem == null)
		{
			return;
		}

		TickMethods action = findInventoryAction(selectedItem, targetedItem, inventory.getItems());
		if (action == UNKNOWN)
		{
			return;
		}
		InputEvent inputEvent = new InputEvent(action, playerLocation, tickClicked);
		log.info("found inv action click: {}", action);

		cycleState.startCycle(inputEvent);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked evt)
	{
		switch (evt.getMenuAction())
		{
			case GAME_OBJECT_FIRST_OPTION:
			case GAME_OBJECT_SECOND_OPTION:
			case GAME_OBJECT_THIRD_OPTION:
			case GAME_OBJECT_FOURTH_OPTION:
			case GAME_OBJECT_FIFTH_OPTION:
				log.info("game object click: {}->{} {}", evt.getMenuOption(), evt.getMenuTarget(), evt.getId());
				if (ResourceNodes.isNode(evt.getId()))
				{
					cycleState.setObjectTarget(evt.getId());
				}
				break;
			case WIDGET_TARGET_ON_WIDGET:
				startTickCycle(evt);
			default:
				if (MENU_ACTIONS_INTERRUPT.contains(evt.getMenuAction()))
				{
					cycleState.setObjectTarget(-1);
					test = evt;
				}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!(event.getActor() instanceof Player))
		{
			return;
		}
		WorldPoint playerLocation = event.getActor().getWorldLocation();
		log.info("animation: {} on {}, {},{}", event.getActor().getAnimation(), client.getTickCount(), playerLocation.getX(), playerLocation.getY());
		if (cycleState.getStatus() == WAITING_FOR_ANIMATION && cycleState.getMethod().getAnimationId() == event.getActor().getAnimation())
		{
			cycleState.confirmAnimation();
		}
	}

	@Provides
	TickTrainerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTrainerConfig.class);
	}
}
