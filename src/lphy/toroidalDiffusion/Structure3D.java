package lphy.toroidalDiffusion;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.io.PDBFileReader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class Structure3D extends JComponent {

    static double BORDER = 30.0;
    static double ATOM_RADIUS = 5.0;

    Structure structure;

    enum Mode {XY, XZ, YZ}

    Mode mode = Mode.XY;

    public Structure3D(Structure structure, Mode mode) {

        this.structure = structure;
        this.mode = mode;
    }

    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D)g;

        Chain chain = structure.getChain(0);

        GeneralPath path = getPath(chain);

        double scale = Math.min((getWidth()-BORDER*2.0)/path.getBounds().getWidth(), (getHeight()-BORDER*2.0)/path.getBounds().getHeight());

        path.transform(AffineTransform.getScaleInstance(scale, scale));

        double tx = (getWidth() - path.getBounds().getWidth())/2.0 - path.getBounds().getMinX();
        double ty = (getHeight() - path.getBounds().getHeight())/2.0 - path.getBounds().getMinY();

        path.transform(AffineTransform.getTranslateInstance(tx,ty));

        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(path);

        List<Group> atomGroups = chain.getAtomGroups();
        for (int i = 0; i < atomGroups.size(); i++) {
            Group atomGroup = atomGroups.get(i);

            if (atomGroup instanceof AminoAcid) {
                AminoAcid aminoAcid = (AminoAcid)atomGroup;
                Atom carbon = aminoAcid.getC();
                drawAtom(carbon, Color.darkGray, scale, tx, ty, g2d);

                Atom alphaCarbon = aminoAcid.getCA();
                drawAtom(alphaCarbon, Color.black, scale, tx, ty, g2d);

                Atom betaCarbon = aminoAcid.getCB();
                if (betaCarbon != null) drawAtom(betaCarbon, Color.lightGray, scale, tx, ty, g2d);

                Atom nitrogen = aminoAcid.getN();
                drawAtom(nitrogen, Color.blue, scale, tx, ty, g2d);

                Atom oxygen = aminoAcid.getO();
                drawAtom(oxygen, Color.red, scale, tx, ty, g2d);


            }
        }
    }

    GeneralPath getPath(Chain chain) {

        GeneralPath path = new GeneralPath();

        List<Group> atomGroups = chain.getAtomGroups();

        int index = 0;
        for (int i = 0; i < atomGroups.size(); i++) {
            Group atomGroup = atomGroups.get(i);

            if (atomGroup instanceof AminoAcid) {
                AminoAcid aminoAcid = (AminoAcid)atomGroup;
                Atom nitrogen = aminoAcid.getN();
                Atom alphaCarbon = aminoAcid.getCA();
                Atom carbon = aminoAcid.getC();
                Atom oxygen = aminoAcid.getO();
                Atom betaCarbon = aminoAcid.getCB();

                if (index == 0) {
                    moveTo(path, nitrogen);
                } else {
                    lineTo(path, nitrogen);
                }
                lineTo(path, alphaCarbon);
                if (betaCarbon != null) {
                    lineTo(path, betaCarbon);
                    lineTo(path, alphaCarbon);
                }
                lineTo(path, carbon);
                lineTo(path, oxygen);
                lineTo(path, carbon);
                index += 5;
            }
        }

        return path;

    }

    private void moveTo(GeneralPath path, Atom atom) {
        switch (mode) {
            case XY: path.moveTo(atom.getX(), atom.getY()); break;
            case XZ: path.moveTo(atom.getX(), atom.getZ()); break;
            case YZ: path.moveTo(atom.getY(), atom.getZ()); break;
        }
    }

    private void lineTo(GeneralPath path, Atom atom) {
        switch (mode) {
            case XY: path.lineTo(atom.getX(), atom.getY()); break;
            case XZ: path.lineTo(atom.getX(), atom.getZ()); break;
            case YZ: path.lineTo(atom.getY(), atom.getZ()); break;
        }
    }

    private void drawAtom(Atom atom, Color color, double scale, double tx, double ty, Graphics2D g) {

        double rx = 0, ry = 0;
        switch (mode) {
            case XY: rx = atom.getX(); ry = atom.getY(); break;
            case XZ: rx = atom.getX(); ry = atom.getZ(); break;
            case YZ: rx = atom.getY(); ry = atom.getZ(); break;
        }

        double x = rx*scale + tx;
        double y = ry*scale + ty;

        g.setColor(color);
        fillEllipse(x, y, ATOM_RADIUS, g);
    }

    private void fillEllipse(double x, double y, double radius, Graphics2D g) {

        Ellipse2D ellipse2D = new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius);
        g.fill(ellipse2D);
    }

    public static void main(String[] args) throws IOException {

        PDBFileReader fileReader = new PDBFileReader();

        FileInputStream inputStream = new FileInputStream("examples/M27.pdb");

        Structure structure = fileReader.getStructure(inputStream);

        JFrame frame = new JFrame("Structure3D");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,2));

        panel.add(new Structure3D(structure, Mode.XY));
        panel.add(new Structure3D(structure, Mode.XZ));
        panel.add(new Structure3D(structure, Mode.YZ));

        frame.getContentPane().add(panel);
        frame.setSize(1000,1000);
        frame.setVisible(true);
    }
}
