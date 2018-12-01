package org.kevwargo.screenpdf.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;


public class ImagePanel extends JPanel {

    private BufferedImage image;
    private MainPanel mainPanel;
    private List<Point> notePoints;

    public ImagePanel(BufferedImage image, MainPanel mainPanel) {
        this.image = image;
        this.mainPanel = mainPanel;
        notePoints = new ArrayList<Point>();
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    Point point = new Point(e.getX(), e.getY());
                    ImagePanel.this.notePoints.add(point);
                    ImagePanel.this.mainPanel.imageClicked(point);
                }
            });
    }

    public BufferedImage getImage() {
        return image;
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, null);
    }

}
