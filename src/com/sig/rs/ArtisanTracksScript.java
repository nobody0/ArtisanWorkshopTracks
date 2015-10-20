package com.sig.rs;

import com.sig.rs.Tasks.*;
import org.powerbot.script.Script;
import org.powerbot.script.Condition;
import org.powerbot.script.PollingScript;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.PaintListener;

import org.powerbot.script.rt6.Game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;

@Script.Manifest(name="Artisan Track Maker", description="Automatically makes the best Tracks inside the Artisan Workshop", properties="topic=0")

public class ArtisanTracksScript extends PollingScript<ClientContext> implements PaintListener {
    private List<Task> taskList = new ArrayList<Task>();

    private ArtisanTracks artisanTracks;

    @Override
    public void start() {
        artisanTracks = new ArtisanTracks(ctx);
        taskList.addAll(Arrays.asList(
                artisanTracks
        ));
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
        if (artisanTracks == null)
        {
            return;
        }

        final Graphics2D g = (Graphics2D) graphics;
        g.setFont(TAHOMA);

        final int expGained = artisanTracks.getCurrentExp() - artisanTracks.getStartExp();
        final int expHr = (int) ((expGained * 3600000D) / getRuntime());
        final int level = artisanTracks.getCurrentLevel();
        final int levelGained = artisanTracks.getCurrentLevel() - artisanTracks.getStartLevel();

        final String ingotToUse = artisanTracks.getIngotToUseName();
        final String highestComponent = artisanTracks.getHighestComponentToSmithName();
        final String currentComponent = artisanTracks.getCurrentComponentToSmithName();

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