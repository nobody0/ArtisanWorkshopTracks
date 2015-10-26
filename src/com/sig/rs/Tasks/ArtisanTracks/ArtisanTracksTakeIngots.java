package com.sig.rs.Tasks.ArtisanTracks;

import com.sig.rs.Blackboard.ArtisanBlackboard;
import com.sig.rs.Tasks.Task;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.rt6.ItemQuery;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.Constants;
import org.powerbot.script.rt6.GameObject;
import org.powerbot.script.rt6.Item;

import java.util.concurrent.Callable;

import static com.sig.rs.Blackboard.ArtisanBlackboard.*;

public class ArtisanTracksTakeIngots extends Task<ClientContext> {
    public ArtisanTracksTakeIngots(ClientContext ctx) {
        super(ctx);

        updateIngotToUse();
    }

    @Override
    public boolean activate() {
        return     !ctx.players.local().inMotion()
                && ctx.players.local().animation() == -1
                && ctx.backpack.select().count() < 28
                && ctx.backpack.select().id(components[ingotToUse.get()][highestComponentToSmith.get()]).count() < 14;
    }

    @Override
    public void execute() {
        System.out.println("--------------ArtisanTracksTakeIngots------------------");

        if (ctx.backpack.select().count() == 0) {
            updateIngotToUse();
        }

        if (!smithTakeButton.get().visible()) {
            final GameObject trough = ctx.objects.select().id(troughIds[ingotToUse.get()]).nearest().poll();
            if (trough.valid())
            {
                if(!trough.inViewport()) {
                    ctx.camera.turnTo(trough);
                    ctx.movement.step(trough);
                    boolean result = Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return trough.inViewport();
                        }
                    }, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                trough.interact("Take-ingots");
                Condition.wait(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return smithTakeButton.get().visible();
                    }
                }, 500, 10);
            }
        }

        if (smithTakeButton.get().visible()) {
            smithTakeButton.get().click();

            Condition.wait(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return ctx.backpack.select().count() >= 28 && !smithTakeButton.get().visible();
                }
            }, 500, 10);
        }
    }

    private void updateIngotToUse()
    {
        smithingLevel.set(ctx.skills.level(Constants.SKILLS_SMITHING));

        for (int i = levels.length-1; i >= 0; i--) {
            if (levels[i][0] <= smithingLevel.get()) {
                ingotToUse.set(i);
                return;
            }
        }
    }

}
