package org.kevwargo.screenpdf;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class Note extends JScrollPane {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 200;

    private int pointX;
    private int pointY;
    private JTextArea text;

    public Note(Container parent, int pointX, int pointY) {
        this.pointX = pointX;
        this.pointY = pointY;
        text = new JTextArea();
        text.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBounds(pointX - WIDTH / 2, pointY - HEIGHT - 30, WIDTH, HEIGHT);
        setViewportView(text);
        parent.add(this);
        text.revalidate();
        repaint();
        text.requestFocus();
        text.getDocument().addDocumentListener(new DocumentListener() {
                public void removeUpdate(DocumentEvent e) {log("remove");}
                public void insertUpdate(DocumentEvent e) {log("insert");}
                public void changedUpdate(DocumentEvent e) {log("change");}

                private void log(String event) {
                    JTextArea t = Note.this.text;
                    int w = t.getWidth();
                    int h = t.getHeight();
                    int pw = (int)t.getPreferredSize().getWidth();
                    int ph = (int)t.getPreferredSize().getHeight();
                    System.out.printf("Document %s event. New size: %d:%d, preferred: %d:%d\n", event, w, h, pw, ph);
                }
            });

        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    System.out.printf("note %d%d, %d:%d\n", Note.this.pointX, Note.this.pointY, e.getX(), e.getY());
                }
            });
    }

}
