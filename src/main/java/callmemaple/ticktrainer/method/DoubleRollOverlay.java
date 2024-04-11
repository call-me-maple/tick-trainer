package callmemaple.ticktrainer.method;

import callmemaple.ticktrainer.*;
import callmemaple.ticktrainer.data.Error;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static callmemaple.ticktrainer.method.DoubleRollStatus.*;

public class DoubleRollOverlay extends OverlayPanel
{
    private final TickTrainerPlugin plugin;
    private final TickTrainerConfig config;

    @Inject
    private DoubleRollManager doubleRollManager;
    @Inject
    private ResourceTickManager resourceTickManager;
    @Inject
    private Client client;
    @Inject
    private PlayerManager playerManager;

    @Inject
    private DoubleRollOverlay(TickTrainerPlugin plugin, TickTrainerConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Open", "More info"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        switch (config.displayMode())
        {
            case VERBOSE_EMPTY:
                return renderVerbose(graphics);
            case MINIMAL:
                return renderMinimal(graphics);
            case VISUAL_EMPTY:
                return renderVisual(graphics);
            default:
                return panelComponent.render(graphics);
        }
    }

    private Dimension renderVisual(Graphics2D graphics)
    {
        return panelComponent.render(graphics);
    }

    private Dimension renderMinimal(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        List<DoubleRollStatus> displayStatuses = new ArrayList<>(Arrays.asList(config.errorsOnly() ? new DoubleRollStatus[]{ERROR} : values()));
        if (!displayStatuses.contains(doubleRollManager.getStatus()))
        {
            return panelComponent.render(graphics);
        }

        switch (doubleRollManager.getStatus())
        {
            case ON_CYCLE:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("on cycle tick " + doubleRollManager.getTickStep())
                        .leftColor(config.onCycleColor())
                        .build());
                break;
            case IDLE:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Idle...")
                        .leftColor(config.idleColor())
                        .build());
                break;
            case ERROR:
                for (Error error: doubleRollManager.getErrors())
                {
                    panelComponent.getChildren().add((LineComponent.builder())
                            .left("error: " + error.getMessage(config.displayMode()))
                            .leftColor(config.errorColor())
                            .build());
                }
                break;
            case LOCKED_OUT:
                int ticksLeft = resourceTickManager.remainingSkillingTicks();
                Color lockedOutColor = config.errorColor();
                if (ticksLeft == 0 && playerManager.getPickaxe().isRng())
                {
                    lockedOutColor = Color.PINK;
                }
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Locked out for " + resourceTickManager.remainingSkillingTicks())
                        .leftColor(lockedOutColor)
                        .build());
                break;
        }

        return panelComponent.render(graphics);
    }

    private Dimension renderVerbose(Graphics2D graphics)
    {
        return panelComponent.render(graphics);

    }
}