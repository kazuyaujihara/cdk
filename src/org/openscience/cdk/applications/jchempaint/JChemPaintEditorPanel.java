/*
 *  $RCSfile$
 *  $Author$
 *  $Date$
 *  $Revision$
 *
 *  Copyright (C) 1997-2005  The JChemPaint project
 *
 *  Contact: jchempaint-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.openscience.cdk.applications.jchempaint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.EventObject;
import javax.swing.JPanel;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.SetOfMolecules;
import org.openscience.cdk.SetOfReactions;
import org.openscience.cdk.controller.PopupController2D;
import org.openscience.cdk.dict.DictionaryDatabase;
import org.openscience.cdk.event.CDKChangeListener;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.ReactionManipulator;
import org.openscience.cdk.validate.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.URL;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.openscience.cdk.*;
import org.openscience.cdk.applications.plugin.*;
import org.openscience.cdk.controller.*;
import org.openscience.cdk.io.*;
import org.openscience.cdk.validate.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.applications.jchempaint.*;
import org.openscience.cdk.applications.jchempaint.io.*;
import org.openscience.cdk.applications.jchempaint.action.*;
import org.openscience.cdk.applications.jchempaint.dialogs.*;
import org.openscience.cdk.applications.jchempaint.dnd.JCPTransferHandler;

/**
 * This class implements an editing JChemPaintPanel.
 * @cdk.module jchempaint
 * @author     steinbeck
 * @created    16. Februar 2005
 * @see        JChemPaintViewerPanel
 */
public class JChemPaintEditorPanel extends JChemPaintPanel
		 implements ChangeListener, CDKChangeListener, SwingConstants, CDKEditBus
{

	String recentSymbol = "C";
	PopupController2D inputAdapter;

	ReallyPaintPanel drawingPanel;
	private JToolBar toolbar2;
		
	/**
	 *  Description of the Field
	 */
	public JButton selectButton;

	/**
	 *  Description of the Field
	 */
	public final static String TIPSUFFIX = "Tooltip";

	private static DictionaryDatabase dictdb = null;
	private static ValidatorEngine engine = null;

	private static LoggingTool logger;

	/**
	 *  sets configurations for the layout of the panel, adds several listeners
	 *
	 *@param  jcpm  The JChemPaintModel
	 */
	public JChemPaintEditorPanel(JChemPaintModel jcpm)
	{
		super();
		if (logger == null)
		{
			logger = new LoggingTool(this);
		}
		AtomContainer ac = ChemModelManipulator.getAllInOneContainer(jcpm.getChemModel());

		setLayout(new BorderLayout());
		setJChemPaintModel(jcpm);
        
        Renderer2DModel rendererModel = jcpm.getRendererModel();
		if (ac.getAtomCount() != 0) {
            logger.info("ChemModel is already non-empty! Sizing things to get it visible!");
			// do some magic to get it all in one page
			Dimension oneMoleculeDimension = new Dimension(700, 400);
			// should be based on molecule/reaction size!
			Dimension dimension = makeChemModelFit(
					oneMoleculeDimension, jcpm.getChemModel()
					);
			rendererModel.setBackgroundDimension(dimension);

			/*
			 *  this code ensures that the molecule ends up somewhere in the model
			 *  of the view screen
			 */
			GeometryTools.translateAllPositive(ac);
			double scaleFactor = GeometryTools.getScaleFactor(ac, rendererModel.getBondLength());
			GeometryTools.scaleMolecule(ac, scaleFactor);
			layoutInTable(oneMoleculeDimension, jcpm.getChemModel());
			// GeometryTools.center(ac, getPreferredSize());
			// GeometryTools.center(ac, new Dimension(300,200));
		}

		jcpm.getControllerModel().setBondPointerLength(rendererModel.getBondLength());
		jcpm.getControllerModel().setRingPointerLength(rendererModel.getBondLength());

		// set the drag-n-drop/copy-paste handler
		this.setTransferHandler(new JCPTransferHandler("JCPPanel"));

		JPanel toolbarsPanel = new JPanel();
		toolbarsPanel.setLayout(new BorderLayout());
    JPanel toolbar1 = new JPanel();
		toolbar1.add(createToolbar(HORIZONTAL, "toolbar"));
		toolbarsPanel.add(toolbar1, BorderLayout.LINE_START);

		JPanel chemtoolbar = new JPanel();
		chemtoolbar.add(createToolbar(HORIZONTAL, "chemtoolbar"));
		toolbarsPanel.add(chemtoolbar, BorderLayout.CENTER);
        add(toolbarsPanel, BorderLayout.NORTH);
        
        drawingPanel = new ReallyPaintPanel(jcpm);
		drawingPanel.setOpaque(true);
        drawingPanel.setBackground(Color.white);
    drawingPanel.addMouseListener(inputAdapter);
    drawingPanel.addMouseMotionListener(inputAdapter);
        JScrollPane scrollPane = new JScrollPane(drawingPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(500, 400));
		logger.debug("JCPPanel set and done...");
	}


	/**
	 *  Overwrites the default background color and returns the color taken from
	 *  the Renderer2DModel.
	 *
	 *@return    The background value
	 */
	public Color getBackground()
	{
		if (jcpm != null)
		{
			return jcpm.getRendererModel().getBackColor();
		} else
		{
			return Color.WHITE;
		}
	}


	/**
	 *  Gets the toolBar attribute of the JChemPaintEditorPanel object
	 *
	 *@return    The toolBar value
	 */
	public JToolBar getToolBar()
	{
		return this.toolbar2;
	}


	/**
	 *  Gets the preferredSize attribute of the JChemPaintPanel object
	 *
	 *@return    The preferredSize value
	 */
	public Dimension getPreferredSize()
	{
		return jcpm.getRendererModel().getBackgroundDimension();
	}

		/**
	 *  Creates a new JFrame that owns a new JChemPaintModel and returns
	 *  it. 
	 *
	 *@return    The new JFrame containing the JChemPaintEditorPanel
	 */
	public static JFrame getEmptyFrameWithModel()
	{
		JChemPaintModel jcpm = new JChemPaintModel();
		jcpm.setTitle(getNewFrameName());
		jcpm.setAuthor(JCPPropertyHandler.getInstance().getJCPProperties().getProperty("General.UserName"));
		Package self = Package.getPackage("org.openscience.cdk.applications.jchempaint");
		String version = self.getImplementationVersion();
		jcpm.setSoftware("JChemPaint " + version);
		jcpm.setGendate((Calendar.getInstance()).getTime().toString());
		JFrame jcpf = getNewFrame(jcpm);
		return jcpf;
	}

	
	/**
	 *  Creates a new JChemPaintEditorPanel and assigns a given Model to it.
	 *
	 *@param  jcpm  The model to be assigned to the new frame.
	 *@return       The new JChemPaintFrame with its new JChemPaintModel
	 */
	public static JFrame getNewFrame(JChemPaintModel jcpm)
	{
		JFrame frame = new JFrame();
		frame.getContentPane().add(new JChemPaintEditorPanel(jcpm));
		frame.setTitle(jcpm.getTitle());
		return frame;
	}

	
	/**
	 *  Sets the jChemPaintModel attribute of the JChemPaintPanel object
	 *
	 *@param  model  The new jChemPaintModel value
	 */
	public void setJChemPaintModel(JChemPaintModel model)
	{
    this.jcpm = model;
		String property = null;
		try
		{
			property = System.getProperty("renderer", "default");
		} catch (Exception exc)
		{
			logger.info("Could not read System property. I might be in a sandbox");
		}
		inputAdapter = new PopupController2D(jcpm.getChemModel(), jcpm.getRendererModel(),
				jcpm.getControllerModel());
		jcpm.getRendererModel().addCDKChangeListener(this);
		inputAdapter.addCDKChangeListener(jcpm);
	}


	/**
	 *  Description of the Method
	 */
	public void setupPopupMenus()
	{
		if (inputAdapter.getPopupMenu(new Atom("H")) == null)
		{
			inputAdapter.setPopupMenu(new Atom("H"), new JChemPaintPopupMenu(this, "atom"));
		}
		if (inputAdapter.getPopupMenu(new PseudoAtom("R")) == null)
		{
			inputAdapter.setPopupMenu(new PseudoAtom("R"), new JChemPaintPopupMenu(this, "pseudo"));
		}
		if (inputAdapter.getPopupMenu(new Bond()) == null)
		{
			inputAdapter.setPopupMenu(new Bond(), new JChemPaintPopupMenu(this, "bond"));
		}
		if (inputAdapter.getPopupMenu(new ChemModel()) == null)
		{
			inputAdapter.setPopupMenu(new ChemModel(), new JChemPaintPopupMenu(this, "chemmodel"));
		}
		if (inputAdapter.getPopupMenu(new Reaction()) == null)
		{
			inputAdapter.setPopupMenu(new Reaction(), new JChemPaintPopupMenu(this, "reaction"));
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public Image takeSnapshot()
	{
		Image snapImage = null;
		try
		{
			logger.info("Making snapshot... ");
			snapImage = createImage(this.getSize().width, this.getSize().height);
			logger.info("created...");

			Graphics snapGraphics = snapImage.getGraphics();
			paint(snapGraphics);

			logger.debug("painting succeeded.");
		} catch (NullPointerException e)
		{
			snapImage = null;
		}
		return snapImage;
	}


	/**
	 *  Method to notify this CDKChangeListener if something has changed in another
	 *  object
	 *
	 *@param  e  The EventObject containing information on the nature and source of
	 *      the event
	 */
	public void stateChanged(EventObject e)
	{
		try
		{
			logger.debug("State Change.");
		} catch (Exception ex)
		{
			System.out.println(ex.toString());
			ex.printStackTrace();
		}
		if (!drawingPanel.drawingNow) repaint();
	}


	/**
	 *  Enlarges the 'one molecule' dimension to fit a set of reactions or
	 *  molecules.
	 *
	 *@param  baseDim  Description of the Parameter
	 *@param  model    Description of the Parameter
	 *@return          Description of the Return Value
	 */
	private Dimension makeChemModelFit(Dimension baseDim, ChemModel model)
	{
		Dimension newDim = new Dimension(baseDim);
		// a bit ugly, but assume moleculeSet *or* reactionSet
		SetOfMolecules moleculeSet = model.getSetOfMolecules();
		if (moleculeSet != null)
		{
			newDim.height = newDim.height * (moleculeSet.getMoleculeCount());
			return newDim;
		}
		SetOfReactions reactionSet = model.getSetOfReactions();
		if (reactionSet != null)
		{
			newDim.height = newDim.height * (reactionSet.getReactionCount());
		}
		return newDim;
	}


	/**
	 *  Lays out the molecules in a SetOfMolecules, or reaction in a SetOfReactions
	 *  in a one column table.
	 *
	 *@param  baseDim  Description of the Parameter
	 *@param  model    Description of the Parameter
	 */
	private void layoutInTable(Dimension baseDim, ChemModel model)
	{
		// a bit ugly, but assume moleculeSet *or* reactionSet
		SetOfMolecules moleculeSet = model.getSetOfMolecules();
		if (moleculeSet != null)
		{
			Molecule[] mols = moleculeSet.getMolecules();
			for (int i = 0; i < mols.length; i++)
			{
				GeometryTools.center(mols[i], baseDim);
				GeometryTools.translate2D(mols[i], 0, baseDim.height * i);
			}
			return;
		}
		SetOfReactions reactionSet = model.getSetOfReactions();
		if (reactionSet != null)
		{
			Reaction[] reactions = reactionSet.getReactions();
			for (int i = 1; i <= reactions.length; i++)
			{
				AtomContainer ac = ReactionManipulator.getAllInOneContainer(reactions[i - 1]);
				GeometryTools.center(ac, baseDim);
				GeometryTools.translate2D(ac, 0, baseDim.height * (reactions.length - i));
			}
		}
	}


	/**
	 *  Creates a JButton given by a String with an Image and adds the right
	 *  ActionListener to it.
	 *
	 *@param  key  String The string used to identify the button
	 *@return      JButton The JButton with already added ActionListener
	 */
	protected JButton createToolbarButton(String key)
	{
		JCPPropertyHandler jcpph = JCPPropertyHandler.getInstance();
		logger.debug("Trying to find resource for key: ", key);
		URL url = jcpph.getResource(key + JCPAction.imageSuffix);
		logger.debug("Trying to find resource: ", url);
		if (url == null)
		{
			logger.error("Cannot find resource: ", key, JCPAction.imageSuffix);
			return null;
		}
		ImageIcon image = new ImageIcon(url);
		if (image == null)
		{
			logger.error("Cannot find image: ", url);
			return null;
		}
		JButton b =
			new JButton(image)
			{
				public float getAlignmentY()
				{
					return 0.5f;
				}
			};
		b.setRequestFocusEnabled(false);
		b.setMargin(new Insets(1, 1, 1, 1));
		String astr = jcpph.getResourceString(key + JCPAction.actionSuffix);
		if (astr == null)
		{
			astr = key;
		}
		JCPAction a = JCPAction.getAction(this, astr);
		if (a != null)
		{
			b.setActionCommand(astr);
			logger.debug("Coupling action to button...");
			b.addActionListener(a);
			b.setEnabled(a.isEnabled());
		} else
		{
			logger.error("Could not find JCPAction class for:", astr);
			b.setEnabled(false);
		}
		try
		{
			String tip = JCPLocalizationHandler.getInstance().getString(key + TIPSUFFIX);
			if (tip != null)
			{
				b.setToolTipText(tip);
			}
		} catch (MissingResourceException e)
		{
			logger.warn("Could not find Tooltip resource for: ", key);
			logger.debug(e);
		}
		;
		return b;
	}


	/**
	 *  Creates a toolbar given by a String with all the buttons that are specified
	 *  in the properties file.
	 *
	 *@param  orientation  int The orientation of the toolbar
	 *@param  kind         String The String used to identify the toolbar
	 *@return              Component The created toolbar
	 */
	private Component createToolbar(int orientation, String kind)
	{
		toolbar2 = new JToolBar(orientation);
		String[] toolKeys = StringHelper.tokenize(getToolbarResourceString(kind));
		JButton button = null;

		if (toolKeys.length != 0)
		{
			String[] sdiToolKeys = new String[(toolKeys.length) - 4];
			for (int i = 4; i < toolKeys.length; i++)
			{
				int j = i - 4;
				sdiToolKeys[j] = toolKeys[i];
			}
			toolKeys = sdiToolKeys;
		}

		for (int i = 0; i < toolKeys.length; i++)
		{
			if (toolKeys[i].equals("-"))
			{
				if (orientation == HORIZONTAL)
				{
					toolbar2.add(Box.createHorizontalStrut(5));
				} else if (orientation == VERTICAL)
				{
					toolbar2.add(Box.createVerticalStrut(5));
				}
			} else
			{
				button = (JButton) createToolbarButton(toolKeys[i]);
				if (toolKeys[i].equals("lasso"))
				{
					selectButton = button;
				}
				if (button != null)
				{
					toolbar2.add(button);
					if (i == 0)
					{
						button.setBackground(Color.GRAY);
					} else
					{
						button.setBackground(Color.LIGHT_GRAY);
					}
				} else
				{
					logger.error("Could not create button");
				}
			}
		}
		if (orientation == HORIZONTAL)
		{
			toolbar2.add(Box.createHorizontalGlue());
		}
		return toolbar2;
	}


	/**
	 *  Gets the menuResourceString attribute of the JChemPaint object
	 *
	 *@param  key  Description of the Parameter
	 *@return      The menuResourceString value
	 */
	public String getToolbarResourceString(String key)
	{
		String str;
		try
		{
			str = JCPPropertyHandler.getInstance().getGUIDefinition().getString(key);
		} catch (MissingResourceException mre)
		{
			str = null;
		}
		return str;
	}


	/**
	 *  Gets the validatorEngine attribute of the JChemPaintEditorPanel class
	 *
	 *@return    The validatorEngine value
	 */
	public static ValidatorEngine getValidatorEngine()
	{
		if (engine == null)
		{
			engine = new ValidatorEngine();
			// default validators
			engine.addValidator(new BasicValidator());
			engine.addValidator(new ValencyValidator());
			engine.addValidator(new CDKValidator());
			engine.addValidator(new DictionaryValidator(dictdb));
			engine.addValidator(new PDBValidator());
		}
		return engine;
	}


	/**
	 *  Gets the dictionaryDatabase attribute of the JChemPaint object
	 *
	 *@return    The dictionaryDatabase value
	 */
	public static DictionaryDatabase getDictionaryDatabase()
	{
		if (dictdb == null)
		{
			dictdb = new DictionaryDatabase();
			try
			{
				File dictdir = new File(JCPPropertyHandler.getInstance().getJChemPaintDir(), "dicts");
				logger.info("User dict dir: ", dictdir);
				logger.debug("       exists: ", dictdir.exists());
				logger.debug("  isDirectory: ", dictdir.isDirectory());
				if (dictdir.exists() && dictdir.isDirectory())
				{
					File[] dicts = dictdir.listFiles();
					for (int i = 0; i < dicts.length; i++)
					{
						// loop over these files and load them
						try
						{
							FileReader reader = new FileReader(dicts[i]);
							String filename = dicts[i].getName();
							dictdb.readDictionary(reader, filename.substring(0, filename.indexOf('.')));
						} catch (IOException exception)
						{
							logger.error("Problem with reading macie dictionary...");
						}
					}
				}
				logger.info("Read these dictionaries: ");
				Enumeration dicts = dictdb.listDictionaries();
				while (dicts.hasMoreElements())
				{
					logger.info(" - ", dicts.nextElement().toString());
				}
			} catch (Exception exc)
			{
				logger.error("Could not handle dictionary initialization. Maybe I'm running in a sandbox.");
			}
		}
		return dictdb;
	}


	/*
	 *  Methods implementing the CDKEditBus interface
	 */
	/**
	 *  Gets the aPIVersion attribute of the JChemPaintEditorPanel object
	 *
	 *@return    The aPIVersion value
	 */
	public String getAPIVersion()
	{
		return "1.8";
	}

	public void runScript(String s1, String s2)
	{
		logger.info("runScript method currently not supported");
	}

	/**
	 *  Mandatory because JChemPaint is a ChangeListener. Used by other classes to
	 *  update the information in one of the three statusbar fields.
	 *
	 *@param  e  ChangeEvent
	 */
	public void stateChanged(ChangeEvent e)
	{
		// send event to plugins
		/*
		 *  if (pluginManager != null)
		 *  {
		 *  pluginManager.stateChanged(new ChemObjectChangeEvent(this));
		 *  }
		 */
	}


	/**
	 *  Adds a feature to the ChangeListener attribute of the JChemPaint object
	 *
	 *@param  listener  The feature to be added to the ChangeListener attribute
	 */
	public void addChangeListener(ChangeListener listener)
	{
		/*
		 *  getCurrentModel().addChangeListener(listener);
		 */
	}

}


