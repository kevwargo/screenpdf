package org.kevwargo.screenpdf;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.MSG;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.kevwargo.screenpdf.panels.MainPanel;


public class MainFrame extends JFrame {

    private String outputDir;
    private MainPanel panel;


    public MainFrame(String outputDir) {
        super("ScreenPDF");

        this.outputDir = outputDir;
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new MainPanel(this, 800, 600);
        setContentPane(new JScrollPane(panel));
        pack();

        startHotKeyThread();
        initSystemTray();
    }

    public String getOutputDir() {
        return outputDir;
    }

    private void startHotKeyThread() {
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
                        panel.newScreenShot();
                    }
                }
            }
        }.start();        
    }

    private void initSystemTray() {
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
        MenuItem hideItem = new MenuItem("Hide");
        MenuItem showItem = new MenuItem("Show");
        MenuItem exitItem = new MenuItem("Exit");
        popup.add(hideItem);
        popup.add(showItem);
        popup.add(exitItem);
        hideItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MainFrame.this.setVisible(false);
                }
            });
        showItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MainFrame.this.setVisible(true);
                }
            });
        exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Quitting");
                    System.exit(0);
                }
            });

        return popup;
    }

}
