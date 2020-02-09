package james.swing;

import org.apache.commons.math3.linear.RealMatrix;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class RealMatrixEditor extends JPanel {

    RealMatrix matrix;

    JTextField[][] textFields;
    JLabel[][] labels;

    int maxFracDigits = 5;


    public RealMatrixEditor(RealMatrix matrix, boolean editable) {
        this.matrix = matrix;

        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(maxFracDigits);

        int rowCount = matrix.getRowDimension();
        int colCount = matrix.getColumnDimension();

        if (editable) {
            textFields = new JTextField[rowCount][colCount];
        } else {
            labels = new JLabel[rowCount][colCount];
        }

        GridLayout gridLayout = new GridLayout(rowCount, colCount);
        gridLayout.setHgap(10);
        gridLayout.setVgap(10);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setLayout(gridLayout);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (editable) {
                    textFields[i][j] = new JTextField(Double.toString(matrix.getEntry(i, j)), 8);
                    //textFields[i][j].setEditable(editable);
                    add(textFields[i][j]);

                } else {
                    labels[i][j] = new JLabel(format.format(matrix.getEntry(i, j)));
                    labels[i][j].setHorizontalAlignment(SwingConstants.RIGHT);
                    add(labels[i][j]);
                }
            }
        }
    }

    public static ViewerFactory<RealMatrix> viewerFactory() {
        return matrix -> new RealMatrixEditor(matrix, false);
    }
}
