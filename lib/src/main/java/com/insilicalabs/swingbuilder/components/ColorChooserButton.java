package com.insilicalabs.swingbuilder.components;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by jzwolak on 3/10/16.
 */
public class ColorChooserButton extends JButton {

    private Color color;
    private final java.util.List<Consumer<Color>> listeners = new ArrayList<>();
    private final static int WIDTH = 28;
    private final static int HEIGHT = 17;

    public ColorChooserButton() {
        super();
        //setContentAreaFilled(true);
        //setOpaque(true);
        setColor(Color.RED);
        addActionListener((ae) -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a Color", color);
            // Only set if a color was actually selected by the user.
            if (newColor != null) {
                setColor(newColor);
                fireColorSelected();
            }
        });
    }

    @Override
    public void setForeground(Color fg) {
        setColor(fg);
    }

    public void setColor(Color color) {
        this.color = color;
        setIcon(createIcon(color, WIDTH, HEIGHT));
    }

    public Color getColor() {
        return color;
    }

    public void addColorSelectedListener(Consumer<Color> listener) {
        listeners.add(listener);
    }

    public void removeColorSelectedListener(Consumer<Color> listener) {
        listeners.remove(listener);
    }

    private void fireColorSelected() {
        for (Consumer<Color> listener : listeners) {
            listener.accept(color);
        }
    }

    private static ImageIcon createIcon(Color main, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        /*
        graphics.setXORMode(Color.BLUE);
        graphics.drawRect(0, 0, width-1, height-1);
        */
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}
