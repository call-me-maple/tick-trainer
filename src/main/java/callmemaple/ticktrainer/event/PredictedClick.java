package callmemaple.ticktrainer.event;

import lombok.Data;

@Data
public class PredictedClick
{
    private final Click click;
    private final int predictedTick;
    private final long timestamp;
}
