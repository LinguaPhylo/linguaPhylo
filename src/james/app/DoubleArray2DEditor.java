package james.app;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class DoubleArray2DEditor extends JPanel {

    Double[][] matrix;

    JTextField[][] textFields;
    JLabel[][] labels;

    int maxFracDigits = 5;

    int GAP = 9;
    int corner = 5;

    public DoubleArray2DEditor(Double[][] matrix, boolean editable) {
        this.matrix = matrix;

        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(maxFracDigits);
        setOpaque(false);

        int rowCount = matrix.length;
        int colCount = matrix[0].length;

        textFields = new JTextField[rowCount][colCount];
        labels = new JLabel[rowCount][colCount];

        GridLayout gridLayout = new GridLayout(rowCount, colCount);
        gridLayout.setHgap(GAP);
        gridLayout.setVgap(GAP);
        setBorder(BorderFactory.createEmptyBorder(GAP,GAP-1,GAP,GAP));
        setLayout(gridLayout);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                textFields[i][j] = new JTextField(Double.toString(matrix[i][j]), 8);
                labels[i][j] = new JLabel(format.format(matrix[i][j]));
                labels[i][j].setHorizontalAlignment(SwingConstants.RIGHT);
                if (editable) {
                    add(textFields[i][j]);

                } else {
                    add(labels[i][j]);
                }
            }
        }

        Dimension preferredElementSize = labels[0][0].getPreferredSize();

        int maxWidth = preferredElementSize.width*colCount + GAP * (colCount+1);
        int maxHeight = preferredElementSize.height*rowCount + GAP * (rowCount+1);

        setMaximumSize(new Dimension(maxWidth, maxHeight));
        System.out.println("Max size = " + maxWidth + ", " + maxHeight);

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(1.5f));

        // draw left bracket
        g.drawLine(1,1,1,getHeight());
        g.drawLine(1,1,corner,1);
        g.drawLine(1,getHeight()-1,corner,getHeight()-1);

        // draw right bracket
        g.drawLine(getWidth()-1,1,getWidth()-corner-1,1);
        g.drawLine(getWidth()-1,getHeight()-1,getWidth()-corner-1,getHeight()-1);
        g.drawLine(getWidth()-1,1,getWidth()-1,getHeight());
    }
}
