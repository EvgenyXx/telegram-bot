package com.example.parser.formatter;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class LiveMatchImageRenderer {

    public byte[] render(
            String player1,
            int score1,
            String sets1,
            String player2,
            int score2,
            String sets2,
            String league,
            String table
    ) throws Exception {

        int width = 700;
        int height = 220;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 🔥 антиалиасинг
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // фон
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // шрифты
        Font titleFont = new Font("Arial", Font.BOLD, 22);
        Font nameFont = new Font("Arial", Font.BOLD, 20);
        Font scoreFont = new Font("Arial", Font.BOLD, 28);
        Font setsFont = new Font("Monospaced", Font.BOLD, 18);

        // 🔴 LIVE
        g.setColor(Color.RED);
        g.setFont(titleFont);
        g.drawString("LIVE", 20, 30);

        // инфа
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Стол " + table + " | " + league, 120, 30);

        // позиции
        int y1 = 90;
        int y2 = 150;

        // игрок 1
        drawPlayer(g, player1, score1, sets1, y1, nameFont, scoreFont, setsFont, true);

        // игрок 2
        drawPlayer(g, player2, score2, sets2, y2, nameFont, scoreFont, setsFont, false);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return baos.toByteArray();
    }

    private void drawPlayer(Graphics2D g,
                            String name,
                            int score,
                            String sets,
                            int y,
                            Font nameFont,
                            Font scoreFont,
                            Font setsFont,
                            boolean isFirst) {

        List<String> values = parseSets(sets, isFirst);

        // имя
        g.setFont(nameFont);
        g.setColor(Color.WHITE);
        g.drawString(shorten(name), 20, y);

        // счет
        g.setFont(scoreFont);
        g.setColor(Color.GREEN);
        g.drawString(String.valueOf(score), 300, y);

        // сеты
        g.setFont(setsFont);
        g.setColor(Color.WHITE);

        int x = 360;

        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);

            if (i == values.size() - 1) {
                g.setColor(Color.YELLOW); // текущий сет
            } else {
                g.setColor(Color.WHITE);
            }

            g.drawString(v, x, y);
            x += 40;
        }
    }

    private List<String> parseSets(String sets, boolean isFirst) {
        List<String> values = new ArrayList<>();

        if (sets != null && !sets.isEmpty()) {
            String[] setsArr = sets.replace("(", "").replace(")", "").split(" ");

            for (String set : setsArr) {
                String[] parts = set.split(":");
                if (parts.length != 2) continue;

                values.add(isFirst ? parts[0] : parts[1]);
            }
        }

        while (values.size() < 7) {
            values.add("-");
        }

        return values;
    }

    private String shorten(String name) {
        if (name.length() <= 18) return name;
        return name.substring(0, 17) + "…";
    }
}