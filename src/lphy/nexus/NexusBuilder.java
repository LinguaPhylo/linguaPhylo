/*
 * Copyright (C) 2014 Tim Vaughan <tgvaughan@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lphy.nexus;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for piecewise construction of Nexus files.  Analogous to StringBuilder,
 * but instead of piecing together strings, we piece together NexusBlocks.
 *
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class NexusBuilder {
    List<NexusBlock> blocks;

    /**
     * Create new NexusBuilder, optionally specifying blocks to include.
     * 
     * @param blockArray vararg of NexusBlock objects
     */
    public NexusBuilder(NexusBlock ... blockArray) {
        blocks = new ArrayList<>(Arrays.asList(blockArray));
    }
    /**
     * Append a block to the Nexus file.
     * 
     * @param block
     * @return the NexusBuilder (for method chaining)
     */
    public NexusBuilder append(NexusBlock block) {
        blocks.add(block);
        
        return this;
    }

    /**
     * Write the assembled Nexus file to pstream.
     * 
     * @param pstream 
     */
    public void write(PrintStream pstream) {
        pstream.println("#NEXUS");
        
        for (NexusBlock block : blocks)
            pstream.print("\n" + block);
    }
}
