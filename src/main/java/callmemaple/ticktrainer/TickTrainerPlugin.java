package callmemaple.ticktrainer;

import callmemaple.ticktrainer.item.ResourceNodes;
import callmemaple.ticktrainer.item.TickMethods;
import callmemaple.ticktrainer.ui.TickTrainerOverlay;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
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

	@Inject
	private SkillingCycle skillingCycle;

	@Inject
	private PlayerState playerState;

	private static final Set<MenuAction> MENU_ACTIONS_INTERRUPT = ImmutableSortedSet.of(
			WALK, WIDGET_TARGET_ON_GROUND_ITEM, WIDGET_TARGET_ON_PLAYER,
			WIDGET_TARGET_ON_NPC, WIDGET_TARGET_ON_GAME_OBJECT, WIDGET_TARGET_ON_WIDGET,
			WIDGET_FIRST_OPTION, WIDGET_SECOND_OPTION, WIDGET_THIRD_OPTION, WIDGET_FOURTH_OPTION, WIDGET_FIFTH_OPTION);
	private final Collection<Object> eventHandlers = new LinkedList<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		Collections.addAll(eventHandlers,
				injector.getInstance(TickManager.class),
				injector.getInstance(SkillingCycle.class),
				injector.getInstance(PlayerState.class));
		eventHandlers.forEach(eventBus::register);
		log.info("tick trainer started!");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		eventHandlers.forEach(eventBus::unregister);
		log.info("tick trainer stopped!");
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		/*
			iterate over all input events of prev tick
				use, drop, mine, chop, move, etc
			determine action(s)
				inv cycle, move, interact, drop
			update player state?
		 */
	}



	@Provides
	TickTrainerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTrainerConfig.class);
	}
}
