package org.kevwargo.screenpdf;


import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.kevwargo.screenpdf.winextra.*;


public class ScreenshotsPanel extends JPanel {

    private ScreenshotsFrame frame;
    private CardLayout cards;
    private JPanel cardPanel;
    private JLabel cardsState;
    private int currentCard;


    public ScreenshotsPanel(ScreenshotsFrame frame, int w, int h) {
        this.frame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(w, h));

        cards = new CardLayout();
        cardPanel = new JPanel(cards);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

        cardsState = new JLabel();

        JButton buttonNext = new JButton(">");
        buttonNext.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    gotoCard(currentCard + 1);
                }
            });
        JButton buttonPrev = new JButton("<");
        buttonPrev.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    gotoCard(currentCard - 1);
                }
            });
        controlPanel.add(buttonPrev);
        controlPanel.add(cardsState);
        controlPanel.add(buttonNext);

        add(controlPanel);
        add(cardPanel);
    }

    public void gotoCard(int num) {
        int cardCount = cardPanel.getComponents().length;
        currentCard = num % cardCount;
        if (currentCard < 0) {
            currentCard = cardCount + currentCard;
        }
        System.out.printf("setting current to %d\n", currentCard);
        String numString = Integer.toString(currentCard);
        cardsState.setText(numString);
        cardsState.repaint();
        cards.show(cardPanel, numString);
        System.out.printf("panel :%fx%f\n", getSize().getWidth(), getSize().getHeight());
    }

    public void hotKeyPressed() {
        BufferedImage image = captureActiveWindow();
        if (image != null) {
            System.out.printf("Successfully captured: %dx%d\n", image.getWidth(), image.getHeight());
            int number = cardPanel.getComponents().length;
            cardPanel.add(new JScrollPane(new ImagePanel(image)), Integer.toString(number));
            gotoCard(number);
            frame.setVisible(true);
        }
    }

    private BufferedImage captureActiveWindow() {
        HWND hWnd = User32.INSTANCE.GetForegroundWindow();
        if (hWnd == null) {
            System.out.println("Active window is null");
            return null;
        }
        HDC hdcWindow = User32.INSTANCE.GetDC(hWnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        RECT bounds = new RECT();
        User32Extra.INSTANCE.GetClientRect(hWnd, bounds);

        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;

        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);

        HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
        GDI32Extra.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, WinGDIExtra.SRCCOPY);

        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);

        BITMAPINFO bmi = new BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        Memory buffer = new Memory(width * height * 4);
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);

        return image;
    }

}
