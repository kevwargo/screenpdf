package org.kevwargo.screenpdf;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class Main extends JFrame {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ScreenshotsFrame(args.length > 0 ? args[0] : null);
            }
        });
    }

}
