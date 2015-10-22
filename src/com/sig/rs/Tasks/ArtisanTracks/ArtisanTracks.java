package com.sig.rs.Tasks.ArtisanTracks;

import com.sig.rs.Tasks.Task;
import com.sig.rs.Blackboard.ArtisanBlackboard;
import static com.sig.rs.Blackboard.ArtisanBlackboard.*;

import org.powerbot.script.rt6.*;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.Condition;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.GameObject;
import org.powerbot.script.Random;

public class ArtisanTracks extends Task<ClientContext> {
    public ArtisanTracks(ClientContext ctx) {
        super(ctx);

        updateIngotToUse();
        updateHighestComponentToSmith();
    }

    @Override
    public boolean activate() {
        return     !ctx.players.local().inMotion()
                && ctx.players.local().animation() == -1;
    }

    @Override
    public void execute() {
        System.out.println("--------------------------------");

        if (ctx.backpack.select().count() == 0) {
            updateIngotToUse();
        }

        if (ctx.backpack.select().count() < 28) {
            takeIngotOrDeposit();
        }

        if (ctx.backpack.select().count() == 28) {
            smithItemOrDeposit();
        }
    }

    private void reset()
    {
        System.out.println("reset");
        updateIngotToUse();

        if (ctx.backpack.select(item -> {
            for (int[] component : components) {
                for (int aComponent : component) {
                    if (aComponent == item.id()) {
                        return true;
                    }
                }
            }
            return false;
        }).count() > 0) {
            depositComponents();
        }

        for(Item item : ctx.backpack.select()) {
            if (item.id() != ingots[ingotToUse.get()]) {
                item.interact("Drop");
            }
        }

        Condition.sleep(Random.nextInt(1000, 1500));
    }

    private void takeIngotOrDeposit()
    {
        updateHighestComponentToSmith();

        if (ctx.backpack.select().id(components[ingotToUse.get()][highestComponentToSmith.get()]).count() >= 14) {
            depositComponents();
        } else {
            takeIngot();
        }
    }

    private void smithItemOrDeposit()
    {
        System.out.println("smithItemOrDeposit");
        if (ctx.backpack.select().id(ingots[ingotToUse.get()]).count() == 28) {
            smithItem(0);
            return;
        }

        for (int i = components[ingotToUse.get()].length-1; i >= 0; i--) {
            if (ctx.backpack.select().id(components[ingotToUse.get()][i]).count() >= 14) {
                int itemToSmith = i+1;

                if (itemToSmith < components[ingotToUse.get()].length && levels[ingotToUse.get()][itemToSmith] <= smithingLevel.get()) {
                    currentComponentToSmith.set(itemToSmith);
                    smithItem(itemToSmith);
                    return;
                } else {
                    depositComponents();
                    return;
                }
            }
        }

        reset();
    }

    private void takeIngot()
    {
        System.out.println("takeIngot");
        if (!smithTakeButton.get().visible()) {
            final GameObject trough = ctx.objects.select().id(troughIds[ingotToUse.get()]).nearest().poll();
            if (trough.valid())
            {
                if(!trough.inViewport()) {
                    ctx.camera.turnTo(trough);
                    ctx.movement.step(trough);
                    boolean result = Condition.wait(trough::inViewport, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                trough.interact("Take-ingots");
                Condition.wait(smithTakeButton.get()::visible, 500, 10);
            }
        }

        if (smithTakeButton.get().visible()) {
            smithTakeButton.get().click();

            Condition.wait(() -> ctx.backpack.select().count() >= 28 && !smithTakeButton.get().visible(), 500, 10);
        }
    }

    private void smithItem(final int itemToSmith)
    {
        System.out.println("smithItem");
        System.out.println(itemToSmith);
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
            getItemComponent(itemToSmith).click();
            Condition.sleep(Random.nextInt(900, 1200));

            smithTakeButton.get().click();

            int expBefore = ctx.skills.experience(Constants.SKILLS_SMITHING);
            System.out.println("expBefore " + expBefore);

            boolean result = Condition.wait(smithingInProggressComponent.get()::visible, 500, 10);
            if (!result) {
                return;
            }

            result = Condition.wait(() -> !smithingInProggressComponent.get().visible() && !smithTakeButton.get().visible(), 1000, 50);
            if (!result) {
                reset();
                return;
            }

            int expAfter = ctx.skills.experience(Constants.SKILLS_SMITHING);
            System.out.println("expAfter " + expAfter);

            if (expBefore == expAfter) {
                reset();
            }
        }
    }

    private void depositComponents()
    {
        System.out.println("depositComponents");
        int hundredPercentItemId = components[ingotToUse.get()][components[ingotToUse.get()].length-1];
        if (ctx.backpack.select().id(hundredPercentItemId).count() >= 14) {
            final GameObject tunnel = ctx.objects.select().id(tunnelIds).shuffle().poll();
            if (tunnel.valid())
            {
                ctx.camera.turnTo(tunnel);
                if(!tunnel.inViewport()) {
                    ctx.movement.step(tunnel);
                    boolean result = Condition.wait(tunnel::inViewport, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                tunnel.interact("Lay-tracks");
                Condition.wait(() -> ctx.players.local().tile().distanceTo(tunnel.tile()) <= 2, 1000, 10);
                Condition.sleep(Random.nextInt(2500, 3500));
            }
        } else {
            final GameObject mineCart = ctx.objects.select().id(mineCartId).shuffle().poll();
            if (mineCart.valid())
            {
                if(!mineCart.inViewport()) {
                    ctx.camera.turnTo(mineCart);
                    ctx.movement.step(mineCart);
                    boolean result = Condition.wait(mineCart::inViewport, 1000, 10);
                    if (!result) {
                        return;
                    }
                }

                mineCart.interact("Deposit-components");
                Condition.sleep(Random.nextInt(2000, 3000));
            }
        }

        updateIngotToUse();
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

    private Component getItemComponent(int i)
    {
        return smithComponentsWrapperComponent.get().component(i*4+2);
    }

}