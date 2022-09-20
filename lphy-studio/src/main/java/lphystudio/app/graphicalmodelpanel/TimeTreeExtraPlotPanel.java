package lphystudio.app.graphicalmodelpanel;

import lphystudio.app.treecomponent.TimeTreeExtraPlotComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Walter Xie
 */
public class TimeTreeExtraPlotPanel extends JPanel implements ActionListener {

    final TimeTreeExtraPlotComponent plotComponent;

    final JRadioButton lttB, neB;

    public TimeTreeExtraPlotPanel(TimeTreeExtraPlotComponent plotComponent) {
        this.plotComponent = plotComponent;

        lttB = new JRadioButton(TimeTreeExtraPlotComponent.LTT_TITLE);
        neB = new JRadioButton(TimeTreeExtraPlotComponent.NE_TITLE);
        lttB.setHorizontalAlignment(SwingConstants.CENTER);
        neB.setHorizontalAlignment(SwingConstants.CENTER);
        lttB.addActionListener(this);
        neB.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(lttB);
        group.add(neB);
        group.setSelected(lttB.getModel(), TimeTreeExtraPlotComponent.isLTTPlot());
        group.setSelected(neB.getModel(), !TimeTreeExtraPlotComponent.isLTTPlot());

        JPanel radioPanel = new JPanel(new GridLayout(1, 0));
        radioPanel.setBorder(new EmptyBorder(1, 10, 1, 10));
        radioPanel.add(lttB);
        radioPanel.add(neB);
        radioPanel.setBackground(Color.white);

        setLayout(new BorderLayout());
//TODO        add(radioPanel, BorderLayout.PAGE_START);
        add(plotComponent, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TimeTreeExtraPlotComponent.setLTTPlot(true);
//        TimeTreeExtraPlotComponent.setLTTPlot(lttB.isSelected());
        plotComponent.repaint();
    }

}
