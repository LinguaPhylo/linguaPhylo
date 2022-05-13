package lphystudio.app.modelguide;

import lphystudio.core.swing.EasyTableModel;

import java.util.List;

/**
 * @author Walter Xie
 */
public class ModelGuideTableModel extends EasyTableModel {

    public ModelGuideTableModel(List<Model> modelList) {
        super(new String[]{"Name", "Category", "Description"}, modelList);
    }

    @Override
    public Object getValueAt(int row, int col) {
        Model model = (Model) dataList.get(row);
        switch (col) {
            case 0:
                return model.getName();
            case 1:
                return model.getCategory();
            case 2:
                String desc = model.getDescription().replaceAll("<br>", " ");
                final int max = 80;
                if (desc.length() > max) {
                    desc = desc.substring(0, max);
                    desc += " ...";
                }
                return desc;
//                case 3:
//                    return model.getDependenciesStr();
//                case 4:
//                    return ext.getWebsite();
//                case 5:
//                    return model.getDesc();
            default:
                throw new IllegalArgumentException("unknown column, " + col);
        }
    }

}
