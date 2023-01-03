package callmemaple.ticktrainer;

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

	@Inject
	private TickManager tickManager;

	@Inject CycleState cycleState;

	private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
			WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER,
			WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET,
			WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION, WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION);
	private Object eventInjector;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		eventInjector = injector.getInstance(TickManager.class);
		eventBus.register(eventInjector);
		log.info("tick trainer started!");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		eventBus.unregister(eventInjector);
		log.info("tick trainer stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
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
		long predictedTime = tickManager.getNextTickTime();
		long currentTime = System.currentTimeMillis();
		if (predictedTime - currentTime < 150)
		{
			predictedTime += tickManager.getAverageTickTime();
		}
		log.info("predicting:{} timestamp:{}", predictedTime, currentTime);
		InputEvent inputEvent = new InputEvent(action, playerLocation, tickClicked, currentTime, predictedTime);

		log.info("found inv action click: {}", inputEvent);
		log.info("stat: {}", cycleState.getStatus());
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
				if (ResourceNodes.isNode(evt.getId()))
				{
					log.info("menuclick tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), evt.getMenuOption(), evt.getMenuTarget(), evt.getId(), System.currentTimeMillis());
					cycleState.setObjectTarget(evt.getId());
				}
				break;
			case WIDGET_TARGET_ON_WIDGET:
				startTickCycle(evt);
				log.info("menuclick tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), evt.getMenuOption(), evt.getMenuTarget(), evt.getId(), System.currentTimeMillis());
				break;//?
			default:
				if (MENU_ACTIONS_INTERRUPT.contains(evt.getMenuAction()))
				{
					log.info("menuclick_interrupt tick:{} {}->{} id:{} timestamp:{}", client.getTickCount(), evt.getMenuOption(), evt.getMenuTarget(), evt.getId(), System.currentTimeMillis());
					cycleState.setObjectTarget(-1);
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

		if (cycleState.getStatus() == WAITING_FOR_ANIMATION && cycleState.getMethod().getAnimationId() == event.getActor().getAnimation())
		{
			long currentTime = System.currentTimeMillis();
			long predictedTime = cycleState.getPredictedTickTime();
			log.info("animation: {} tick:{} at {}", event.getActor().getAnimation(), client.getTickCount(), currentTime);
			log.info("predicted:{} actual:{} {}", predictedTime, currentTime, (predictedTime>currentTime ? "+" : "-" ) + Math.abs(predictedTime-currentTime));
			cycleState.confirmAnimation();
		}
	}

	@Provides
	TickTrainerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTrainerConfig.class);
	}
}
