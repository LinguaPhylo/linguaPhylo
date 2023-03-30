package lphystudio.core.swing;

import lphystudio.core.theme.ThemeColours;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class CircleButton extends JButton {

    private boolean mouseOver = false;
    private boolean mousePressed = false;

    private Color backgroundColor = ThemeColours.getBackgroundColor();
    private Color borderColor = ThemeColours.getDefaultColor();

    private Color genDistColor = ThemeColours.getGenDistColor();

    public CircleButton(String text, Color backgroundColor, Color borderColor) {

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

    private int getDiameter(){
        int diameter = Math.min(getWidth(), getHeight());
        return diameter;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getGraphics().getFontMetrics(getFont());
        int minDiameter = 10 + Math.max(metrics.stringWidth(getText()), metrics.getHeight());
        return new Dimension(minDiameter, minDiameter);
    }

    @Override
    public boolean contains(int x, int y){
        int radius = getDiameter()/2;
        return Point2D.distance(x, y, getWidth()/2, getHeight()/2) < radius;
    }

    @Override
    public void paintComponent(Graphics g) {

        int diameter = getDiameter();
        int radius = diameter/2;

        if(mousePressed){
            g.setColor(ThemeColours.getMousePressColor());
        }
        else{
            g.setColor(backgroundColor);
        }
        g.fillOval(getWidth()/2 - radius, getHeight()/2 - radius, diameter, diameter);

        if(mouseOver){
            g.setColor(getGenDistColor());
        }
        else{
            g.setColor(borderColor);
        }
        g.drawOval(getWidth()/2 - radius, getHeight()/2 - radius, diameter, diameter);

        super.paintComponent(g);
    }

    public Color getGenDistColor() {
        return genDistColor;
    }

    public void setGenDistColor(Color genDistColor) {
        this.genDistColor = genDistColor;
    }
}