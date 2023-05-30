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

package lphy.base.parser.nexus;

import lphy.base.evolution.alignment.SimpleAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Nexus block containing a sequence alignment.  Note that this is preferred
 * over the often-used "data" block by Maddison et al.
 *
 * @author Tim Vaughan &lt;tgvaughan@gmail.com&gt;
 */
public class CharactersBlock extends NexusBlock {

    private final SimpleAlignment alignment;

    public CharactersBlock(SimpleAlignment alignment) {
        this.alignment = alignment;
    }
    
    @Override
    public String getBlockName() {
        return "characters";
    }

    @Override
    public List<String> getBlockLines() {
        List<String> lines = new ArrayList<>();
        
        lines.add("dimensions nchar=" + alignment.nchar());
        
        // Assumes BEAST sequence data types map directly
        // onto nexus data types.  No doubt a bad idea in general...
        lines.add("format datatype=" + alignment.getSequenceTypeStr());
        
        StringBuilder matrix = new StringBuilder("matrix ");
        for (int i=0; i<alignment.ntaxa(); i++) {
            try {
                String taxonName = alignment.getTaxaNames()[i];
                String sequence = alignment.getSequence(i);
                matrix.append("\n\t\t").append(taxonName).append(" ").append(sequence);
            } catch (Exception ex) {
                Logger.getLogger(CharactersBlock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lines.add(matrix.toString());
        
        return lines;
    }
    
}
