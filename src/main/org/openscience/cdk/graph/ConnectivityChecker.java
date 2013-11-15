/* $Revision$ $Author$ $Date$    
 * 
 * Copyright (C) 1997-2007  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA. 
 * 
 */
package org.openscience.cdk.graph;

import com.google.common.collect.Maps;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;
import org.openscience.cdk.interfaces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tool class for checking whether the (sub)structure in an
 * AtomContainer is connected.
 * To check whether an AtomContainer is connected this code
 * can be used:
 * <pre>
 *  boolean isConnected = ConnectivityChecker.isConnected(atomContainer);
 * </pre>
 *
 * <p>A disconnected AtomContainer can be fragmented into connected
 * fragments by using code like:
 * <pre>
 *   MoleculeSet fragments = ConnectivityChecker.partitionIntoMolecules(disconnectedContainer);
 *   int fragmentCount = fragments.getAtomContainerCount();
 * </pre> 
 *
 * @cdk.module standard
 * @cdk.githash
 *
 * @cdk.keyword connectivity
 */
@TestClass("org.openscience.cdk.graph.ConnectivityCheckerTest")
public class ConnectivityChecker 
{
	/**
	 * Check whether a set of atoms in an {@link IAtomContainer} is connected.
	 *
	 * @param   atomContainer  The {@link IAtomContainer} to be check for connectedness
	 * @return                 true if the {@link IAtomContainer} is connected   
	 */
    @TestMethod("testIsConnected_IAtomContainer,testPartitionIntoMolecules_IsConnected_Consistency")
    public static boolean isConnected(IAtomContainer atomContainer)
	{
        // with one atom or less, we define it to be connected, as there is no
        // partitioning needed
        if (atomContainer.getAtomCount() < 2) return true;

		ConnectedComponents cc = new ConnectedComponents(GraphUtil.toAdjList(atomContainer));
        return cc.nComponents() == 1;
    }
	


	/**
	 * Partitions the atoms in an AtomContainer into covalently connected components.
	 *
	 * @param   container  The AtomContainer to be partitioned into connected components, i.e. molecules
	 * @return                 A MoleculeSet.
     *
     * @cdk.dictref   blue-obelisk:graphPartitioning
	 */
    @TestMethod("testPartitionIntoMolecules_IAtomContainer,testPartitionIntoMoleculesKeepsAtomIDs,testPartitionIntoMolecules_IsConnected_Consistency")
    public static IAtomContainerSet partitionIntoMolecules(IAtomContainer container) {

        ConnectedComponents        cc            = new ConnectedComponents(GraphUtil.toAdjList(container));
        int[]                      components    = cc.components();
        IAtomContainer[]           containers    = new IAtomContainer[cc.nComponents() + 1];
        Map<IAtom,IAtomContainer>  componentsMap = Maps.newHashMapWithExpectedSize(container.getAtomCount());
        
        for (int i = 1; i < containers.length; i++)
            containers[i] = container.getBuilder().newInstance(IAtomContainer.class);
        
		IAtomContainerSet containerSet = container.getBuilder().newInstance(IAtomContainerSet.class);

        for (int i = 0; i < container.getAtomCount(); i++) {
            componentsMap.put(container.getAtom(i), containers[components[i]]);
            containers[components[i]].addAtom(container.getAtom(i));
        }
        
        for (IBond bond : container.bonds())
            componentsMap.get(bond.getAtom(0)).addBond(bond);

        for (ISingleElectron electron : container.singleElectrons())
            componentsMap.get(electron.getAtom()).addSingleElectron(electron);

        for (ILonePair lonePair : container.lonePairs())
            componentsMap.get(lonePair.getAtom()).addLonePair(lonePair);
        
        for (IStereoElement stereo : container.stereoElements()) {
            if (stereo instanceof ITetrahedralChirality) {
                componentsMap.get(((ITetrahedralChirality) stereo).getChiralAtom()).addStereoElement(stereo);
            } else if (stereo instanceof IDoubleBondStereochemistry) {
                componentsMap.get(((IDoubleBondStereochemistry) stereo).getStereoBond().getAtom(0)).addStereoElement(stereo);
            } else {
                System.err.println("New stereoelement is not currently paritioned with ConnectivityChecker:" + stereo.getClass());
            }
        }
        
        for (int i = 1; i < containers.length; i++)
            containerSet.addAtomContainer(containers[i]);
        
		return containerSet;
	}
}
