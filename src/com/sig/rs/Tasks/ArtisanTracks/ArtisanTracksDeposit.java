package com.sig.rs.Tasks.ArtisanTracks;

import com.sig.rs.Blackboard.ArtisanBlackboard;
import com.sig.rs.Tasks.Task;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.rt6.*;

import java.util.concurrent.Callable;

import static com.sig.rs.Blackboard.ArtisanBlackboard.*;

public class ArtisanTracksDeposit extends Task<ClientContext> {
    public ArtisanTracksDeposit(ClientContext ctx) {
        super(ctx);
    }

    @Override
    public boolean activate() {
        return     !ctx.players.local().inMotion()
                && ctx.players.local().animation() == -1
                && ctx.backpack.select().id(components[ingotToUse.get()][highestComponentToSmith.get()]).count() >= 14;
    }

    @Override
    public void execute() {
        System.out.println("---------------ArtisanTracksDeposit-----------------");

        int hundredPercentItemId = components[ingotToUse.get()][components[ingotToUse.get()].length-1];
        if (ctx.backpack.select().id(hundredPercentItemId).count() >= 14) {
            final GameObject tunnel = ctx.objects.select().id(tunnelIds).shuffle().poll();
            if (tunnel.valid())
            {
                ctx.camera.turnTo(tunnel);
                if(!tunnel.inViewport()) {
                    ctx.movement.step(tunnel);
                    boolean result = Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return tunnel.inViewport();
                        }
                    }, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                tunnel.interact("Lay-tracks");
                Condition.wait(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return ctx.players.local().tile().distanceTo(tunnel.tile()) <= 2;
                    }
                }, 1000, 10);

                smithingLevel.set(ctx.skills.level(Constants.SKILLS_SMITHING));
                smithingRealLevel.set(ctx.skills.realLevel(Constants.SKILLS_SMITHING));
                smithingExp.set(ctx.skills.experience(Constants.SKILLS_SMITHING));

                Condition.sleep(Random.nextInt(2500, 3500));
            }
        } else {
            final GameObject mineCart = ctx.objects.select().id(mineCartId).shuffle().poll();
            if (mineCart.valid())
            {
                if(!mineCart.inViewport()) {
                    ctx.camera.turnTo(mineCart);
                    ctx.movement.step(mineCart);
                    boolean result = Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return !mineCart.inViewport();
                        }
                    }, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                mineCart.interact("Deposit-components");
                Condition.sleep(Random.nextInt(2000, 3000));
            }
        }
    }

}
