package org.kevwargo.screenpdf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class ImagePanel extends JScrollPane {

    private BufferedImage image;

    public ImagePanel(BufferedImage image) {
        this.image = image;
        setViewportView(new ImagePanelInternal());
    }

    public BufferedImage getImage() {
        return image;
    }

    private class ImagePanelInternal extends JPanel {

        public ImagePanelInternal() {
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(image, 0, 0, null);
        }

    }

}
