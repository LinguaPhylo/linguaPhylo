/*
 * Copyright (C) 2014 Tim Vaughan <tgvaughan@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package lphy.base.parser.nexus;

import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.tree.TimeTree;

import java.io.PrintStream;
import java.util.List;

/**
 * Class for producing NEXUS files.
 * 
 * @author Tim Vaughan &lt;tgvaughan@gmail.com&gt;
 */
public class NexusWriter {
    
    /**
     * Write an alignment and/or one or more trees to the provided print stream
     * in Nexus format.
     * 
     * @param alignment Alignment to write (may be null)
     * @param trees Zero or more trees with taxa corresponding to alignment. (May be null)
     * @param pstream Print stream where output is sent
     * @throws Exception
     */
    public static void write(SimpleAlignment alignment, List<TimeTree> trees,
                             PrintStream pstream) throws Exception {
        
        String[] taxa = null;
        if (alignment != null) {
            taxa = alignment.getTaxaNames();
        } else {
            if (trees != null && !trees.isEmpty()) {
                taxa = trees.get(0).getTaxaNames();
            }
        }

        NexusBuilder nb = new NexusBuilder();
        
        if (taxa != null) {
            nb.append(new TaxaBlock(taxa));
        }
        
        if (alignment != null)
            nb.append(new CharactersBlock(alignment));
        
        if (trees != null && !trees.isEmpty())
            nb.append(new TreesBlock(trees));
        
        nb.write(pstream);
    }

    public static void close(PrintStream pstream) {
        pstream.close();
    }
    
}
