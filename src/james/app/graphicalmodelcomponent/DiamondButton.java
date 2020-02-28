package james.app.graphicalmodelcomponent;

import javax.swing.JButton;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DiamondButton extends JButton {

    private boolean mouseOver = false;
    private boolean mousePressed = false;

    private Color backgroundColor;
    private Color borderColor;

    public DiamondButton(String text, Color backgroundColor, Color borderColor) {

        super(text);

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
    public Dimension getPreferredSize(){
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

        Graphics2D g2d= (Graphics2D)g;

        GeneralPath path = new GeneralPath();
        path.moveTo(getWidth()/2.0f - radius, getHeight()/2.0f);
        path.lineTo(getWidth()/2.0f, getHeight()/2.0f - radius);
        path.lineTo(getWidth()/2.0f + radius, getHeight()/2.0f);
        path.lineTo(getWidth()/2.0f, getHeight()/2.0f + radius);
        path.closePath();

        if(mousePressed){
            g.setColor(Color.LIGHT_GRAY);
        }
        else{
            g.setColor(backgroundColor);
        }
        g2d.fill(path);

        if(mouseOver){
            g.setColor(Color.BLUE);
        }
        else{
            g.setColor(borderColor);
        }
        g2d.draw(path);

        super.paintComponent(g);
    }
}