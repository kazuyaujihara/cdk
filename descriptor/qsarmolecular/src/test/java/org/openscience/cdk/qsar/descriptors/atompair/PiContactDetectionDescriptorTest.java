/* Copyright (c) 2018 Kazuya Ujihara <ujihara.kazuya@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openscience.cdk.qsar.descriptors.atompair;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IAtomPairDescriptor;
import org.openscience.cdk.qsar.descriptors.DescriptorTest;
import org.openscience.cdk.qsar.result.BooleanResult;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.templates.TestMoleculeFactory;

/**
 * @cdk.module test-qsarmolecular
 */
public class PiContactDetectionDescriptorTest extends DescriptorTest<IAtomPairDescriptor> {
    @Before
    public void setUp() throws Exception {
        setDescriptor(PiContactDetectionDescriptor.class);
    }
    
    @Test
    public void testButadiene() {
        IAtomContainer mol = TestMoleculeFactory.makeAlkane(4);
        mol.getBond(0).setOrder(IBond.Order.DOUBLE);
        mol.getBond(2).setOrder(IBond.Order.DOUBLE);
        for (int i = 0; i < 4; i++) 
            for (int j = 0; j < 4; j++) 
                Assert.assertEquals(true, checkAtomAtom(mol, i, j));
    }
        
    @Test
    public void test137() {
        IAtomContainer mol = TestMoleculeFactory.makeAlkane(8);
        mol.getBond(0).setOrder(IBond.Order.DOUBLE);
        mol.getBond(2).setOrder(IBond.Order.DOUBLE);
        mol.getBond(6).setOrder(IBond.Order.DOUBLE);
        
        for (int i = 0; i < 4; i++) 
        	for (int j = 0; j < 4; j++) 
                Assert.assertEquals(true, checkAtomAtom(mol, i, j));
        for (int i = 0; i < 4; i++) 
            for (int j = 4; j < 8; j++) 
                Assert.assertEquals(false, checkAtomAtom(mol, i, j));
        Assert.assertEquals(true, checkAtomAtom(mol, 6, 7));
   }
    
    private boolean checkAtomAtom(IAtomContainer mol, int i, int j) {
        DescriptorValue result = descriptor.calculate(mol.getAtom(i), mol.getAtom(j), mol);
        BooleanResult val = (BooleanResult)result.getValue();
        return val.booleanValue();
    }
}
