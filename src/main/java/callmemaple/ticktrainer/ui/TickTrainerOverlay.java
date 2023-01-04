package callmemaple.ticktrainer.ui;

import callmemaple.ticktrainer.*;
import callmemaple.ticktrainer.Error;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static callmemaple.ticktrainer.SkillingCycleStatus.*;

public class TickTrainerOverlay extends OverlayPanel
{
    private final TickTrainerPlugin plugin;
    private final TickTrainerConfig config;

    @Inject
    private TickMethodCycle tickMethodCycle;

    @Inject
    private TickTrainerOverlay(TickTrainerPlugin plugin, TickTrainerConfig config)
    {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        switch (config.displayMode())
        {
            case VERBOSE:
                return renderVerbose(graphics);
            case MINIMAL:
                return renderMinimal(graphics);
            case VISUAL:
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

        List<SkillingCycleStatus> displayStatuses = new ArrayList<>(Arrays.asList(config.errorsOnly() ? new SkillingCycleStatus[]{ERROR} : values()));
        if (!displayStatuses.contains(tickMethodCycle.getStatus()))
        {
            return panelComponent.render(graphics);
        }

        switch (tickMethodCycle.getStatus())
        {
            case ON_CYCLE:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("on cycle tick " + tickMethodCycle.getTickCycle())
                        .leftColor(config.onCycleColor())
                        .build());
                break;
            case IDLE:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Idle...")
                        .leftColor(config.idleColor())
                        .build());
                break;
            case DELAYED:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Delaying to next tick")
                        .build());
                break;
            case ERROR:
                for (Error error: tickMethodCycle.getErrors())
                {
                    panelComponent.getChildren().add((LineComponent.builder())
                            .left("error: " + error.getMessage(config.displayMode()))
                            .leftColor(config.errorColor())
                            .build());
                }
                break;
            case LOCKED_OUT:
                break;
        }

        return panelComponent.render(graphics);
    }

    private Dimension renderVerbose(Graphics2D graphics)
    {
        return panelComponent.render(graphics);

    }
}