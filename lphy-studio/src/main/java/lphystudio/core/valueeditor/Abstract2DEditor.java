package lphystudio.core.valueeditor;

import javax.swing.*;
import java.awt.*;

public abstract class Abstract2DEditor extends JPanel {
    JTextField[][] textFields;
    JLabel[][] labels;

    int GAP = 5;
    int corner = 5;

    protected abstract String formatMatrixElement(Object obj);

    protected abstract String elementToString(Object matrix);

    protected void draw2DArray(Object[][] matrix, boolean editable) {
        setOpaque(false);

        int rowCount = matrix.length;
        int colCount = rowCount > 0 ? matrix[0].length : rowCount;

        textFields = new JTextField[rowCount][colCount];
        labels = new JLabel[rowCount][colCount];

        if (rowCount > 0 && colCount > 0) {
            GridLayout gridLayout = new GridLayout(rowCount, colCount);
            gridLayout.setHgap(GAP);
            gridLayout.setVgap(GAP);
            setLayout(gridLayout);
        } else
            setLayout(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder(GAP, GAP - 1, GAP, GAP));

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                textFields[i][j] = new JTextField(elementToString(matrix[i][j]), 8);
                labels[i][j] = new JLabel(formatMatrixElement(matrix[i][j]));
                labels[i][j].setHorizontalAlignment(SwingConstants.RIGHT);
                if (editable) {
                    add(textFields[i][j]);

                } else {
                    add(labels[i][j]);
                }
            }
        }

        Dimension preferredElementSize;
        if (rowCount > 0 && colCount > 0) {
            preferredElementSize = labels[0][0].getPreferredSize();
        } else {
            preferredElementSize = new JLabel(" ").getPreferredSize();
        }
        int maxWidth = preferredElementSize.width * colCount + GAP * (colCount + 1);
        int maxHeight = preferredElementSize.height * rowCount + GAP * (rowCount + 1);

        setMaximumSize(new Dimension(maxWidth, maxHeight));
//        LoggerUtils.log.fine("Max size = " + maxWidth + ", " + maxHeight);

    }

    public void redraw2DArray(boolean editable) {
        removeAll();
        if (textFields != null && labels != null) {
            for (int i = 0; i < textFields.length; i++) {
                for (int j = 0; j < textFields[i].length; j++) {
                    if (editable) {
                        add(textFields[i][j]);
                    } else {
                        add(labels[i][j]);
                    }
                }
            }
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(1.5f));

        // draw left bracket
        g.drawLine(1, 1, 1, getHeight());
        g.drawLine(1, 1, corner, 1);
        g.drawLine(1, getHeight() - 1, corner, getHeight() - 1);

        // draw right bracket
        g.drawLine(getWidth() - 1, 1, getWidth() - corner - 1, 1);
        g.drawLine(getWidth() - 1, getHeight() - 1, getWidth() - corner - 1, getHeight() - 1);
        g.drawLine(getWidth() - 1, 1, getWidth() - 1, getHeight());
    }
}
