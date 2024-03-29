/*
 * Copyright (C) 2008-2010 Martin Riesz <riesz.martin at gmail.com>
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
package org.pneditor.editor.commands;

import java.util.HashSet;
import java.util.Set;

import org.pneditor.editor.gpetrinet.GraphicArc;
import org.pneditor.editor.gpetrinet.GraphicPetriNet;
import org.pneditor.editor.gpetrinet.GraphicPlace;
import org.pneditor.petrinet.AbstractPlace;
import org.pneditor.petrinet.adapters.pneAdaptor.PetriNetAdapter;
import org.pneditor.util.Command;

/**
 *
 * @author Martin Riesz <riesz.martin at gmail.com>
 */
public class DeletePlaceCommand implements Command {

	final private Set<Command> deleteAllArcEdges = new HashSet<>();
	final private GraphicPetriNet gPetriNet;
	final private GraphicPlace gPlace;
	final private Set<GraphicArc> connectedArcs;
	
	public DeletePlaceCommand(final GraphicPlace gPlace, final GraphicPetriNet gPetriNet) {
		this.gPetriNet = gPetriNet;
		this.gPlace = gPlace;
		this.connectedArcs = gPetriNet.getConnectedGraphicArcs(gPlace);
		for (final GraphicArc arc : this.connectedArcs) {
			this.deleteAllArcEdges.add(new DeleteArcCommand(arc, gPetriNet));
		}

	}

	@Override
	public void execute() {
		for (final Command deleteArc : this.deleteAllArcEdges) {
			deleteArc.execute();
		}
		this.gPetriNet.removeElement(this.gPlace);
		this.gPetriNet.getPetriNet().removeAbstractPlace(this.gPlace.getPlace());


		
	}

	@Override
	public void undo() {
		final AbstractPlace place = this.gPetriNet.getPetriNet().addAbstractPlace();
        place.setLabel(this.gPlace.getLabel());
        place.setTokens(this.gPlace.getPlace().getTokens());
        this.deleteAllArcEdges.clear();
        this.gPetriNet.addElement(this.gPlace);
        this.gPlace.setPlace(place);
    	for (final GraphicArc arc : this.connectedArcs) {
    		if (arc.getSource() == this.gPlace) {
    			this.deleteAllArcEdges.add(new AddArcCommand(this.gPetriNet, this.gPlace, arc.getDestination()));
    		}
    		else {
    			this.deleteAllArcEdges.add(new AddArcCommand(this.gPetriNet, arc.getSource(), this.gPlace));
    		}
    	}
    	for (final Command addArc : this.deleteAllArcEdges) {
    		addArc.execute();
    	}
    }

    @Override
	public void redo() {
        for (final Command addArc : this.deleteAllArcEdges) {
            addArc.undo();
        }
		this.gPetriNet.removeElement(this.gPlace);
		this.gPetriNet.getPetriNet().removeAbstractPlace(this.gPlace.getPlace());
	}

	@Override
	public String toString() {
		return "Delete place node";
	}

}
