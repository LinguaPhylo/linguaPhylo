package lphystudio.app.manager;

import lphystudio.core.swing.EasyTableModel;

import java.util.List;

/**
 * @author Walter Xie
 */
public class ExtManagerTableModel extends EasyTableModel {

    public ExtManagerTableModel(List<LPhyExtension> extList) {
        super(new String[]{"ID", "GroupID", "Installed", "Dependencies", "Description"}, extList);
    }

    @Override
    public Object getValueAt(int row, int col) {
        LPhyExtension ext = (LPhyExtension) dataList.get(row);
        return switch (col) {
            case 0 -> ext.getArtifactId();
            case 1 -> ext.getGroupId();
            case 2 -> ext.getVersion();
            // TODO insert col Latest version
            case 3 -> ext.getDependenciesStr();
            case 4 ->
//                    return ext.getWebsite();
//                case 5:
                    ext.getDesc();
            default -> throw new IllegalArgumentException("unknown column, " + col);
        };
    }

}
