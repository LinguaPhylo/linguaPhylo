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

import java.util.ArrayList;
import java.util.List;

/**
 * Nexus Taxa block.
 *
 * @author Tim Vaughan &lt;tgvaughan@gmail.com&gt;
 */
public class TaxaBlock extends NexusBlock {
    
    private final String[] taxa;

    public TaxaBlock(String[] taxa) {
        this.taxa = taxa;
    }

    @Override
    public String getBlockName() {
        return "taxa";
    }

    @Override
    public List<String> getBlockLines() {
        List<String> lines = new ArrayList<>();

        int ntax = taxa.length;

        lines.add("dimensions ntax=" + ntax);

        StringBuilder taxLabels = new StringBuilder("taxlabels");
        for (int i=0; i<ntax; i++)
            taxLabels.append(" ").append(taxa[i]);

        lines.add(taxLabels.toString());
        
        return lines;
    }
    
}
