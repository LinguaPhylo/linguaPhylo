package lphystudio.core.swing;

/**
 * Implement this to pass it into a method, so that it can set progress inside that method.
 * <code>
 *     class Task extends SwingWorker<Void, Void> implements Progress
 * </code>
 * Use start and end to control the interval which can be each replicate during the progress.
 * @author Walter Xie
 */
public interface Progress {

    /**
     * Call SwingWorker.setProgress(int) here
     * after computing the progress from percentage.
     * @param percentage   between 0 and 1
     */
    void setProgressPercentage(double percentage);

    /**
     * @return the start point of progress, which is between 0 and 100.
     */
    int getStart();

    /**
     * @return the end point of progress, which is between 0 and 100.
     */
    int getEnd();

    /**
     * @param start  set the start point of progress.
     */
    void setStart(int start);
    /**
     * @param end  set the end point of progress.
     */
    void setEnd(int end);

    /**
     * @return  the interval between start and end.
     */
    default int getInterval() {
        return getEnd() - getStart();
    }
}
