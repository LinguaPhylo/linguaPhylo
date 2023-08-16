package lphy.evolution.traits;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Walter Xie
 */
class CharSetBlockTest {



    @Test
    void testParseCharSet() {
        String str = "3-629\\3 1-629\\3 2-629\\3";
        assertTrue(CharSetBlock.Utils.isValid(str));

        List<CharSetBlock> charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(str);
        assertEquals(charSetBlocks.size(), 3);

        for (CharSetBlock block : charSetBlocks) {
            System.out.println(block);
        }
        assertEquals(charSetBlocks.get(0).toString(), "CharSet{from=3, to=629, every=3}");
        assertEquals(charSetBlocks.get(1).toString(), "CharSet{from=1, to=629, every=3}");
        assertEquals(charSetBlocks.get(2).toString(), "CharSet{from=2, to=629, every=3}");

        str = "1 458-659 897-898";
        assertTrue(CharSetBlock.Utils.isValid(str));

        charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(str);
        assertEquals(charSetBlocks.size(), 3);

        assertEquals(charSetBlocks.get(0).toString(), "CharSet{from=1, to=1, every=1}");
        assertEquals(charSetBlocks.get(1).toString(), "CharSet{from=458, to=659, every=1}");
        assertEquals(charSetBlocks.get(2).toString(), "CharSet{from=897, to=898, every=1}");

        str = "1 458-659 897-898";
        assertTrue(CharSetBlock.Utils.isValid(str));

        charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(str);
        assertEquals(charSetBlocks.size(), 3);

        assertEquals(charSetBlocks.get(0).toString(), "CharSet{from=1, to=1, every=1}");
        assertEquals(charSetBlocks.get(1).toString(), "CharSet{from=458, to=659, every=1}");
        assertEquals(charSetBlocks.get(2).toString(), "CharSet{from=897, to=898, every=1}");

        str = "4-457\\3 662-.\\3";
        assertTrue(CharSetBlock.Utils.isValid(str));

        charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(str);
        assertEquals(charSetBlocks.size(), 2);

        assertEquals(charSetBlocks.get(0).toString(), "CharSet{from=4, to=457, every=3}");
        assertEquals(charSetBlocks.get(1).toString(), "CharSet{from=662, to=-1, every=3}");
    }
}