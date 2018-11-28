package org.kevwargo.screenpdf;


import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.MSG;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;


public class ScreenshotsFrame extends JFrame {

    private String outputDir;


    public ScreenshotsFrame(String outputDir) {
        super("ScreenPdf");

        this.outputDir = outputDir;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final ScreenshotsPanel panel = new ScreenshotsPanel(this, 620, 800);
        setContentPane(new JScrollPane(panel));
        pack();

        new Thread() {
            public void run() {
                final int HOTKEY_ID = 1;
                if (User32.INSTANCE.RegisterHotKey(null, HOTKEY_ID, User32.MOD_CONTROL | User32.MOD_SHIFT, 0x47)) {
                    System.out.println("HOTKEY REGISTER: success");
                } else {
                    System.out.println("HOTKEY REGISTER: fail");
                }
                MSG msg = new MSG();
                while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                    if (msg.message == User32.WM_HOTKEY && msg.wParam.longValue() == HOTKEY_ID) {
                        panel.hotKeyPressed();
                    }
                }
            }
        }.start();

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = createTrayPopupMenu();
            try {
                TrayIcon icon = createTrayIcon(popup);
                tray.add(icon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Tray is not supported");
        }
    }

    public String getOutputDir() {
        return outputDir;
    }

    private TrayIcon createTrayIcon(PopupMenu popup) throws IOException {
        Image image = ImageIO.read(getClass().getResourceAsStream("/images/screenshot.png"));
        TrayIcon icon = new TrayIcon(image, "ScreenPdf", popup);
        icon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    System.out.printf("Mouse clicked: %d %d %d %d\n", e.getX(), e.getXOnScreen(), e.getY(), e.getYOnScreen());
                }
            });
        icon.setImageAutoSize(true);
        return icon;
    }

    private PopupMenu createTrayPopupMenu() {
        PopupMenu popup = new PopupMenu();
        MenuItem item1 = new MenuItem("Hide");
        MenuItem item2 = new MenuItem("Show");
        MenuItem item3 = new MenuItem("Exit");
        popup.add(item1);
        popup.add(item2);
        popup.add(item3);
        item1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Item 1 clicked");
                    ScreenshotsFrame.this.setVisible(false);
                }
            });
        item2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Item 2 clicked");
                    ScreenshotsFrame.this.setVisible(true);
                }
            });
        item3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Quitting");
                    System.exit(0);
                }
            });

        return popup;
    }

}
