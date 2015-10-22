package com.sig.rs;

import com.sig.rs.Tasks.Task;
import com.sig.rs.Tasks.ArtisanTracks.*;
import com.sig.rs.Blackboard.ArtisanBlackboard;
import static com.sig.rs.Blackboard.ArtisanBlackboard.*;

import org.powerbot.script.Script;
import org.powerbot.script.PollingScript;
import org.powerbot.script.rt6.*;
import org.powerbot.script.PaintListener;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;

@Script.Manifest(name="Artisan Track Maker", description="Automatically makes the best Tracks inside the Artisan Workshop", properties="topic=0")

public class ArtisanTracksScript extends PollingScript<ClientContext> implements PaintListener {
    private boolean init = true;

    private List<Task> taskList = new ArrayList<Task>();

    @Override
    public void start() {
        smithComponentsWrapperComponent.set(ctx.widgets.component(1371, 44));
        smithTakeButton.set(ctx.widgets.component(1370, 33).component(4));
        smithingInProggressComponent.set(ctx.widgets.component(1251, 0));

        smithingLevelRealStart.set(ctx.skills.realLevel(Constants.SKILLS_SMITHING));
        smithingExpStart.set(ctx.skills.experience(Constants.SKILLS_SMITHING));

        smithingLevel.set(ctx.skills.level(Constants.SKILLS_SMITHING));
        smithingRealLevel.set(ctx.skills.realLevel(Constants.SKILLS_SMITHING));
        smithingExp.set(ctx.skills.experience(Constants.SKILLS_SMITHING));

        taskList.addAll(Arrays.asList(
                new ArtisanTracksTakeIngots(ctx),
                new ArtisanTracksSmithing(ctx),
                new ArtisanTracksDeposit(ctx)
        ));

        init = false;
    }

    @Override
    public void poll() {
        for (Task task : taskList) {
            if (task.activate()) {
                task.execute();
            }
        }
    }

    public static final Font TAHOMA = new Font("Tahoma", Font.PLAIN, 12);

    @Override
    public void repaint(Graphics graphics) {
        if (init) {
            return;
        }

        final Graphics2D g = (Graphics2D) graphics;
        g.setFont(TAHOMA);

        final int expGained = smithingExp.get() - smithingExpStart.get();
        final int expHr = (int) ((expGained * 3600000D) / getRuntime());
        final int level = smithingRealLevel.get();
        final int levelGained = level - smithingLevelRealStart.get();

        final String ingotToUse = getIngotToUseName();
        final String highestComponent = getHighestComponentToSmithName();
        final String currentComponent = getCurrentComponentToSmithName();

        g.setColor(Color.BLACK);

        g.fillRect(5, 5, 220, 65);

        g.setColor(Color.WHITE);

        g.drawString(String.format("Smithing: %,d +%,d", level, levelGained), 10, 20);
        g.drawString(String.format("Exp: %,d (%,d/h)", expGained, expHr), 10, 40);

        g.drawString(String.format("%s %s (%s %s)", ingotToUse, currentComponent, ingotToUse, highestComponent), 10, 60);
    }

    @Override
    public void stop() {
        screenShot();
    }

    private void screenShot() {
        final int width = (int)ctx.game.getViewport().width, height = (int)ctx.game.getViewport().height;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // your paint's repaint(Graphics) method
        repaint(img.createGraphics());
        img = img.getSubimage(5, 5, 220, 65);
        final String fileName = ctx.controller.script().getName() + "_" + String.valueOf(new SimpleDateFormat("ddMMyy-HHmmssSSS").format(new Date())).concat(".png");
        final File screenshot = new File(getStorageDirectory(), fileName);

        System.out.println(getStorageDirectory().getAbsolutePath());
        System.out.println(fileName);

        try {
            ImageIO.write(img, "png", screenshot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}