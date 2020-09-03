package jebl.evolution.io;


import java.io.IOException;
import java.io.Reader;

/**
 * Extension of {@link NexusImporter}, adding
 * 1) multiple partitions, 2) dates
 * @author Walter Xie
 */
public class ExtNexusImporter extends NexusImporter {

    public ExtNexusImporter(Reader reader) {
        super(reader);
    }

    /**
     * interface NexusBlockImp{ public NexusBlock findNextBlock(); }
     * enum NexusBlock implements NexusBlockImp{ TAXA, ..., DATA; }
     * // or T extends Enum<? extends NexusBlockImp>
     * class NexusImporterDefault<T implements NexusBlockImp> {
     *     protected T enumNexusBlock;
     *     protected NexusImporterDefault(T block){
     *         this.block = block;
     *     } }
     * class NexusImporter extends NexusImporterDefault<NexusBlock>{
     *     public NexusImporter(NexusBlock block){
     *         super(block);
     *     } }
     * protected not private ...
     */
    public enum ExtNexusBlock {
        UNKNOWN,
        TAXA,
        CHARACTERS,
        DATA,
        ASSUMPTIONS, // new
        CALIBRATION, // new
        UNALIGNED,
        DISTANCES,
        TREES
    }

    //****** NexusBlock ******//

    public ExtNexusBlock findNextBlockExt() throws IOException {
        findToken("BEGIN", true);
        nextBlockName = helper.readToken(";").toUpperCase();
        return findBlockName(nextBlockName);
    }

    protected ExtNexusBlock findBlockName(String blockName) {
        try {
            nextBlock = ExtNexusBlock.valueOf(blockName);
        } catch( IllegalArgumentException e ) {
            // handle unknown blocks. java 1.5 throws an exception in valueOf
            nextBlock = null;
        }

        if (nextBlock == null) {
            nextBlock = ExtNexusBlock.UNKNOWN;
        }

        return nextBlock;
    }

    protected void findToken(String query, boolean ignoreCase) throws IOException {
        String token;
        boolean found = false;

        do {
            token = helper.readToken();

            if ( (ignoreCase && token.equalsIgnoreCase(query)) || token.equals(query) ) {
                found = true;
            }
        } while (!found);
    }

    protected ExtNexusBlock nextBlock = null;
    protected String nextBlockName = null;

    //****** Data Type ******//

//TODO new datatypes    readDataBlockHeader


}
