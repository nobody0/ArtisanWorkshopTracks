package com.sig.rs.Tasks.ArtisanTracks;

import com.sig.rs.Tasks.Task;
import com.sig.rs.Blackboard.ArtisanBlackboard;
import static com.sig.rs.Blackboard.ArtisanBlackboard.*;

import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.rt6.*;

public class ArtisanTracksSmithing extends Task<ClientContext> {
    public ArtisanTracksSmithing(ClientContext ctx) {
        super(ctx);

        updateHighestComponentToSmith();
        smithingLevel.onChange((oldValue) -> updateHighestComponentToSmith());
    }

    @Override
    public boolean activate() {
        return     !ctx.players.local().inMotion()
                && ctx.players.local().animation() == -1
                && ctx.backpack.select().id(components[ingotToUse.get()][highestComponentToSmith.get()]).count() < 14;
    }

    @Override
    public void execute() {
        System.out.println("--------------ArtisanTracksSmithing------------------");

        if (ctx.backpack.select().id(ingots[ingotToUse.get()]).count() == 28) {
            smithComponent(0);
            return;
        }

        for (int i = highestComponentToSmith.get() - 1; i >= 0; i--) {
            if (ctx.backpack.select().id(components[ingotToUse.get()][i]).count() >= 14) {
                int componentToSmith = i+1;

                currentComponentToSmith.set(componentToSmith);
                smithComponent(componentToSmith);
                return;
            }
        }
    }

    private void smithComponent(final int componentToSmith)
    {
        System.out.println("smithComponent");
        System.out.println(componentToSmith);
        if (!smithTakeButton.get().visible()) {
            System.out.println("!smithTakeButton.visible()");

            final GameObject anvil = ctx.objects.select().id(anvilId).nearest().poll();
            if (anvil.valid())
            {
                if(!anvil.inViewport()) {
                    ctx.camera.turnTo(anvil);
                    ctx.movement.step(anvil);
                    boolean result = Condition.wait(anvil::inViewport, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                anvil.interact("Smith");
                Condition.wait(smithTakeButton.get()::visible, 500, 10);
            }
        }

        if (smithTakeButton.get().visible()) {
            getSmithComponent(componentToSmith).click();
            Condition.sleep(Random.nextInt(900, 1200));

            smithTakeButton.get().click();

            boolean result = Condition.wait(smithingInProggressComponent.get()::visible, 500, 10);
            if (!result) {
                return;
            }

            result = Condition.wait(() -> !smithingInProggressComponent.get().visible() && !smithTakeButton.get().visible(), 1000, 50);
            if (!result) {
                return;
            }

            smithingLevel.set(ctx.skills.level(Constants.SKILLS_SMITHING));
            smithingRealLevel.set(ctx.skills.realLevel(Constants.SKILLS_SMITHING));
            smithingExp.set(ctx.skills.experience(Constants.SKILLS_SMITHING));
        }
    }

    private void updateHighestComponentToSmith()
    {
        int highestComponentToSmith = 0;
        for (int i = levels[ingotToUse.get()].length-1; i >= 0; i--) {
            if (levels[ingotToUse.get()][i] <= smithingLevel.get()) {
                highestComponentToSmith = i;
                break;
            }
        }
        ArtisanBlackboard.highestComponentToSmith.set(highestComponentToSmith);
    }

    private Component getSmithComponent(int i)
    {
        return smithComponentsWrapperComponent.get().component(i*4+2);
    }

}