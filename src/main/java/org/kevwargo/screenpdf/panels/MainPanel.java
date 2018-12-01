package org.kevwargo.screenpdf.panels;


import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.kevwargo.screenpdf.MainFrame;
import org.kevwargo.screenpdf.winextra.*;



public class MainPanel extends JPanel implements ActionListener {

    private MainFrame frame;
    private CardLayout cards;
    private JPanel controlPanel;
    private JPanel cardPanel;
    private int currentCard;
    private JLabel cardsState;


    public MainPanel(MainFrame frame, int width, int height) {
        this.frame = frame;
        // setPreferredSize(new Dimension(width, height));
        // setLayout(new GridBagLayout());
        setLayout(new BorderLayout());

        cards = new CardLayout();
        cardPanel = new JPanel(cards);

        controlPanel = createControlPanel();

        // GridBagConstraints c = new GridBagConstraints();

        // c.fill = GridBagConstraints.HORIZONTAL;
        // c.weightx = 1;
        // c.gridwidth = 3;
        // c.gridx = 0;
        // c.gridy = 0;
        // add(controlPanel, c);

        // c.gridy = 1;
        // c.gridwidth = 1;
        // c.gridx = 1;
        // add(cardPanel, c);
        add(controlPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
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
                    gotoCard(currentCard + 1);
                }
            });
        JButton buttonSave = new JButton("Save");
        buttonSave.setActionCommand("save");
        buttonSave.addActionListener(this);

        controlPanel.add(buttonSave);
        controlPanel.add(buttonPrev);
        controlPanel.add(cardsState);
        controlPanel.add(buttonNext);

        System.out.printf("control %d:%d\n", controlPanel.getWidth(), controlPanel.getHeight());

        return controlPanel;
    }
    
    public void gotoCard(int num) {
        int cardCount = cardPanel.getComponents().length;
        currentCard = num % cardCount;
        if (currentCard < 0) {
            currentCard = cardCount + currentCard;
        }
        System.out.printf("setting current to %d\n", currentCard);
        String numString = Integer.toString(currentCard);
        String numStringDisplay = Integer.toString(currentCard + 1);
        cardsState.setText(numStringDisplay);
        cardsState.repaint();
        cards.show(cardPanel, numString);
        revalidate();
        System.out.printf("panel: %d:%d\n", getWidth(), getHeight());
    }

    public void imageClicked(Point point) {
        System.out.printf("Image clicked at %f:%f\n", point.getX(), point.getY());
    }
    
    public void newScreenShot() {
        BufferedImage image = captureActiveWindow();
        if (image != null) {
            System.out.printf("Successfully captured: %dx%d\n", image.getWidth(), image.getHeight());
            int number = cardPanel.getComponents().length;
            cardPanel.add(new ImagePanel(image, this), Integer.toString(number));
            gotoCard(number);

            frame.pack();
            frame.setVisible(true);
        }
    }

    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
        case "save":
            savePdf();
            break;
        }
    }

    private void savePdf() {
        JFileChooser fc = new JFileChooser(frame.getOutputDir());
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fc.getSelectedFile().getAbsolutePath();
                System.out.printf("Chosen output PDF file: %s\n", filename);
                int pageCount = cardPanel.getComponents().length;
                if (pageCount > 0) {
                    PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filename));
                    ImagePanel first = (ImagePanel)cardPanel.getComponent(0);
                    BufferedImage image = first.getImage();
                    Document doc = new Document(pdfDoc, new PageSize(image.getWidth() / 2, image.getHeight() / 2));
                    doc.setMargins(0, 0, 0, 0);
                    doc.add(new Image(ImageDataFactory.create(image, null)));
                    for (int i = 1; i < pageCount; i++) {
                        ImagePanel panel = (ImagePanel)cardPanel.getComponent(i);
                        image = panel.getImage();
                        doc.add(new AreaBreak(new PageSize(image.getWidth() / 2, image.getHeight() / 2)));
                        doc.add(new Image(ImageDataFactory.create(image, null)));
                    }
                    doc.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
