package lphy.core.model.datatype;

import lphy.core.model.component.Value;

public class Array2DUtils {

    /**
     * @param arr2d
     * @return     common info for toString in 2d array
     * @param <T>
     */
    public static <T> String toString(Value<T[][]> arr2d) {

        StringBuilder builder = new StringBuilder();
        if (!arr2d.isAnonymous()) builder.append(arr2d.getId()).append(" = ");
        builder.append("[");
        for (int i = 0; i < arr2d.value().length; i++) {
            builder.append("[");
            if (arr2d.value()[i].length > 0) {
                builder.append(arr2d.value()[i][0]);
                for (int j = 1; j < arr2d.value()[i].length; j++) {
                    builder.append(", ");
                    builder.append(arr2d.value()[i][j]);
                }
            }
            builder.append("]");
            if (i < arr2d.value().length -1) {
                builder.append(", ");
            }
        }
        builder.append("]");

        return builder.toString();

    }

}
