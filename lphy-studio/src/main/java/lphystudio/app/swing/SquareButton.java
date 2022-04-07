package lphystudio.app.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SquareButton extends JButton {

    private boolean mouseOver = false;
    private boolean mousePressed = false;

    private Color backgroundColor = Color.white;
    private Color borderColor = Color.black;

    Point2D origin = new Point2D.Double(0,0);

    public SquareButton(String text, Color backgroundColor, Color borderColor) {

        super(text);

        setBorder(new EmptyBorder(0,0,0,0));

        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;

        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);

        MouseAdapter mouseListener = new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent me){
                if(contains(me.getX(), me.getY())){
                    mousePressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent me){
                mousePressed = false;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent me){
                mouseOver = false;
                mousePressed = false;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent me){
                mouseOver = contains(me.getX(), me.getY());
                repaint();
            }
        };

        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    @Override
    public Dimension getPreferredSize(){
        FontMetrics metrics = getGraphics().getFontMetrics(getFont());
        int minDiameter = 10 + Math.max(metrics.stringWidth(getText()), metrics.getHeight());
        return new Dimension(minDiameter, minDiameter);
    }

    @Override
    public void paintComponent(Graphics g) {

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width-1, height-1);

        if(mousePressed){
            g.setColor(Color.LIGHT_GRAY);
        }
        else{
            g.setColor(backgroundColor);
        }

        origin.setLocation((width-size)/2.0, (height-size)/2.0);

        Graphics2D g2d = (Graphics2D)g;

        Rectangle2D rect = new Rectangle2D.Double(origin.getX(), origin.getY(), size, size);

        g2d.fill(rect);

        if(mouseOver){
            g.setColor(Color.BLUE);
        }
        else{
            g.setColor(borderColor);
        }
        g2d.draw(rect);

        super.paintComponent(g);
    }
}