package com.sig.rs.Tasks;

import org.powerbot.script.rt6.*;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.Condition;
import org.powerbot.script.Filter;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.GameObject;

import java.util.concurrent.Callable;

/**
 Take-ingots
 Bronze: 24821 1,2,3,5,6,8,9,11,12
 Iron: 24822 15,19,20,24,25,29,30,34,35
 Steel: 24823 39,44,45,49,50,54,55,59,60

 Items: ingot, rails, plate, 40%, spikes,
 Bronze 20502, 20506, 20507, 20511, 20508, 20512, 20509, 20513, 20510, 20514
 Iron 20503, 20515, 20516, 20525

 Herstellen i=0-8
 Widget 1371
 Component 44
 Component i*4+2

 Smith Button
 Widget 1370
 Component 34
 Component 4

 Deposit-components
 24824

 Lay-tracks
 24826
 24743
 24825
 */

public class ArtisanTracks extends Task<ClientContext> {
    boolean init = true;

    int mySmithingLevel = 0;
    int ingotToUse = 0; //0=bronze, 1=iron, 2=steel
    int highestComponentToSmith = 0;
    int currentComponentToSmith = 0;

    final int[][] levels = {
            //rails, plate, 40%, spikes, 60%, joint, 80%, tie, 100%
            { 1,     2,     3,   5,      6,   8,     9,   11,  12},
            {15,     19,    20,  24,     25,  29,    30,  34,  35},
            {39,     44,    45,  49,     50,  54,    55,  59,  60},
    };
    final int[] ingots = {
            20502,
            20503,
            20504,
    };
    final int[][] components = {
            //rails, plate, 40%,   spikes, 60%,   joint, 80%,   tie,   100%
            {20506,  20507, 20511, 20508,  20512, 20509, 20513, 20510, 20514},
            {20515,  20516, 20525, 20517,  20526, 20518, 20527, 20519, 20528},
            {20520,  20521, 20529, 20522,  20530, 20523, 20531, 20524, 20532},
    };
    final int[] troughIds = {24821, 24822, 24823}; //Take-ingots

    final int anvilId = 24820; //Smith
    final int mineCartId = 24824; //Deposit-components
    final int[] tunnelIds = {24826, 24743, 24825}; //Lay-tracks

    final String[] ingotNames = {"Bronze", "Iron", "Steel"};
    final String[] componentNames = {"Rails", "Base Plate", "Track 40%", "Spikes", "Track 60%", "Joint", "Track 80%", "Ties", "Track 100%"};

    int startExp = ctx.skills.experience(Constants.SKILLS_SMITHING);
    int startLevel = ctx.skills.realLevel(Constants.SKILLS_SMITHING);

    Component itemsComponent = ctx.widgets.component(1371, 44);
    Component smithTakeButton = ctx.widgets.component(1370, 33).component(4);
    Component smithingInProggressComponent = ctx.widgets.component(1251, 0);

    public ArtisanTracks(ClientContext ctx) {
        super(ctx);
    }

    @Override
    public boolean activate() {
        return     !ctx.players.local().inMotion()
                && ctx.players.local().animation() == -1;
    }

    @Override
    public void execute() {
        System.out.println("--------------------------------");
        if (init) {
            init=false;
            updateIngotToUse();
            updateHighestComponentToSmith();
        }

        if (ctx.backpack.select().count() == 0) {
            updateIngotToUse();
        }

        if (ctx.backpack.select().count() < 28) {
            takeIngotOrDeposit();
            return;
        }

        if (ctx.backpack.select().count() == 28) {
            smithItemOrDeposit();
            return;
        }
    }

    public int getStartExp() {
        return startExp;
    }

    public int getCurrentExp() {
        return ctx.skills.experience(Constants.SKILLS_SMITHING);
    }

    public int getStartLevel() {
        return startLevel;
    }

    public int getCurrentLevel() {
        return ctx.skills.realLevel(Constants.SKILLS_SMITHING);
    }

    public String getIngotToUseName() {
        return ingotNames[ingotToUse];
    }
    public String getHighestComponentToSmithName() {
        return componentNames[highestComponentToSmith];
    }
    public String getCurrentComponentToSmithName() {
        return componentNames[currentComponentToSmith];
    }

    private void reset()
    {
        System.out.println("reset");
        updateIngotToUse();

        if (ctx.backpack.select(new Filter<Item>() {
            @Override
            public boolean accept(Item item) {
                for (int i = 0; i < components.length; i++) {
                    for (int ii = 0; ii < components[i].length; ii++) {
                        if (components[i][ii] == item.id()) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }).count() > 0) {
            depositComponents();
        }

        for(Item item : ctx.backpack.select()) {
            if (item.id() != ingots[ingotToUse]) {
                item.interact("Drop");
            }
        }

        Condition.sleep(1500);
    }

    private void takeIngotOrDeposit()
    {
        updateHighestComponentToSmith();

        if (ctx.backpack.select().id(components[ingotToUse][highestComponentToSmith]).count() >= 14) {
            depositComponents();
        } else {
            takeIngot();
        }
    }

    private void smithItemOrDeposit()
    {
        System.out.println("smithItemOrDeposit");
        if (ctx.backpack.select().id(ingots[ingotToUse]).count() == 28) {
            smithItem(0);
            return;
        }

        for (int i = components[ingotToUse].length-1; i >= 0; i--) {
            if (ctx.backpack.select().id(components[ingotToUse][i]).count() >= 14) {
                int itemToSmith = i+1;

                if (itemToSmith < components[ingotToUse].length && levels[ingotToUse][itemToSmith] <= mySmithingLevel) {
                    currentComponentToSmith = itemToSmith;
                    smithItem(itemToSmith);
                    return;
                } else {
                    depositComponents();
                    return;
                }
            }
        }

        reset();
        return;
    }

    private void takeIngot()
    {
        System.out.println("takeIngot");
        if (!smithTakeButton.visible()) {
            GameObject trough = ctx.objects.select().id(troughIds[ingotToUse]).nearest().poll();
            if (trough.valid())
            {
                if(!trough.inViewport()) {
                    ctx.movement.step(trough);
                    ctx.camera.turnTo(trough);
                    Condition.sleep(2000);
                }
                if(!trough.inViewport()) {
                    Condition.sleep(2000);
                }
                if(!trough.inViewport()) {
                    Condition.sleep(2000);
                }

                trough.interact("Take-ingots");
                Condition.wait(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return smithTakeButton.visible();
                    }
                }, 500, 10);
            }
        }

        if (smithTakeButton.visible()) {
            smithTakeButton.click();

            Condition.wait(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return ctx.backpack.select().count() >= 28 && !smithTakeButton.visible();
                }
            }, 500, 10);
        }
    }

    private void smithItem(final int itemToSmith)
    {
        System.out.println("smithItem");
        System.out.println(itemToSmith);
        if (!smithTakeButton.visible()) {
            System.out.println("!smithTakeButton.visible()");

            GameObject anvil = ctx.objects.select().id(anvilId).nearest().poll();
            if (anvil.valid())
            {
                if(!anvil.inViewport()) {
                    ctx.movement.step(anvil);
                    ctx.camera.turnTo(anvil);
                    Condition.sleep(2000);
                }

                if(!anvil.inViewport()) {
                    Condition.sleep(2000);
                }

                if(!anvil.inViewport()) {
                    Condition.sleep(2000);
                }

                anvil.interact("Smith");
                Condition.wait(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return smithTakeButton.visible();
                    }
                }, 500, 10);
            }
        }

        if (smithTakeButton.visible()) {
            getItemComponent(itemToSmith).click();
            Condition.sleep(1000);

            smithTakeButton.click();

            int expBefore = ctx.skills.experience(Constants.SKILLS_SMITHING);
            System.out.println("expBefore " + expBefore);

            boolean result = Condition.wait(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return smithingInProggressComponent.visible();
                }
            }, 500, 10);
            if (!result) {
                return;
            }

            result = Condition.wait(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return !smithingInProggressComponent.visible() && !smithTakeButton.visible();
                }
            }, 1000, 50);
            if (!result) {
                reset();
                return;
            }

            int expAfter = ctx.skills.experience(Constants.SKILLS_SMITHING);
            System.out.println("expAfter " + expAfter);

            if (expBefore == expAfter) {
                reset();
                return;
            }

            return;
        }
    }

    private void depositComponents()
    {
        System.out.println("depositComponents");
        int hundredPercentItemId = components[ingotToUse][components[ingotToUse].length-1];
        if (ctx.backpack.select().id(hundredPercentItemId).count() >= 14) {
            GameObject tunnel = ctx.objects.select().id(tunnelIds).shuffle().poll();
            if (tunnel.valid())
            {
                if(!tunnel.inViewport()) {
                    ctx.movement.step(tunnel);
                    ctx.camera.turnTo(tunnel);
                    Condition.sleep(4000);
                }
                if(!tunnel.inViewport()) {
                    Condition.sleep(2000);
                }
                if(!tunnel.inViewport()) {
                    Condition.sleep(2000);
                }
                if(!tunnel.inViewport()) {
                    Condition.sleep(2000);
                }

                tunnel.interact("Lay-tracks");
                Condition.sleep(5500);
            }
        } else {
            GameObject mineCart = ctx.objects.select().id(mineCartId).shuffle().poll();
            if (mineCart.valid())
            {
                if(!mineCart.inViewport()) {
                    ctx.movement.step(mineCart);
                    ctx.camera.turnTo(mineCart);
                    Condition.sleep(1500);
                }
                if(!mineCart.inViewport()) {
                    Condition.sleep(1500);
                }
                if(!mineCart.inViewport()) {
                    Condition.sleep(1500);
                }

                mineCart.interact("Deposit-components");
                Condition.sleep(2500);
            }
        }

        updateIngotToUse();
    }

    private void updateIngotToUse()
    {
        mySmithingLevel = ctx.skills.level(Constants.SKILLS_SMITHING);

        for (int i = levels.length-1; i >= 0; i--) {
            if (levels[i][0] <= mySmithingLevel) {
                ingotToUse = i;
                return;
            }
        }
    }

    private void updateHighestComponentToSmith()
    {
        highestComponentToSmith = 0;
        for (int i = levels[ingotToUse].length-1; i >= 0; i--) {
            if (levels[ingotToUse][i] <= mySmithingLevel) {
                highestComponentToSmith = i;
                break;
            }
        }
    }

    private Component getItemComponent(int i)
    {
        return itemsComponent.component(i*4+2);
    }

}