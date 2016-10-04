/**
 * Written by Derin Sevenler (Unlu lab, BU, 2015)
 * 
 * Distributed to Bella Campana (2016-09-12) to be used as reference material.
 */

package org.micromanager.arrayScan;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mmcorej.CMMCore;

import org.micromanager.api.MMPlugin;
import org.micromanager.api.MultiStagePosition;
import org.micromanager.api.PositionList;
import org.micromanager.api.ScriptInterface;
import org.micromanager.api.StagePosition;
import org.micromanager.utils.MMScriptException;

public class ArrayScan implements MMPlugin, ActionListener {

	public static final String menuname = "Array Scan";
	public static final String tooltipDescription = "Scan a microarray";
	public static final String versionNumber = "0.1";
	// Provides access to the Micro-Manager Java API (for GUI control and high-level functions)

	private ScriptInterface app_;
	private CMMCore core_;

	protected JFrame frame;
	protected Container panel;
	protected JButton clearB, setB;
	protected JTextField nRowField, nColField, rowPitchField, colPitchField;


	// required meta functions
	@Override
	public String getCopyright() {
		return "Boston University, 2015";
	}
	@Override
	public String getDescription() {
		return tooltipDescription;
	}
	@Override
	public String getInfo() {
		return tooltipDescription;
	}
	@Override
	public String getVersion() {
		return versionNumber;
	}
	@Override
	public void dispose() {
		// This function has no implementation - our dialog should be dismissed by the user.
	}

	@Override
	public void setApp(ScriptInterface app) {
		// Initialization
		app_ = app;
		core_ = app.getMMCore();
	}

	@Override
	public void show() {
		// Create the panel for content layout
		frame = new JFrame("ArrayScan");
		panel = frame.getContentPane();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		// add content
		
		// make and add the 'clear' button'
		clearB = new JButton("Clear XY position list");
		clearB.setActionCommand("clearP");
		clearB.addActionListener(this);
		clearB.setToolTipText("This will clear all saved positions");
		
		// make a panel and put it inside, for alignment
		JPanel clearPanel = new JPanel();
		clearPanel.add(clearB);
		clearPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(clearPanel);
		

		// make and add two fields for number of rows and columns
		nRowField = new JTextField("5",2);
		nColField = new JTextField("4",2);
		// labels
		JLabel nRowLabel = new JLabel("Rows:");
		JLabel nColLabel = new JLabel("Columns:");
		// put them in their own pane side-by-side
		JPanel nRowColPanel = new JPanel();
		nRowColPanel.add(nRowLabel);
		nRowColPanel.add(nRowField);
		nRowColPanel.add(nColLabel);
		nRowColPanel.add(nColField);
		nRowColPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(nRowColPanel);
		
		// make and add 'x pitch' and 'y pitch'
		rowPitchField = new JTextField("288",3);
		colPitchField = new JTextField("288",3);
		// labels
		JLabel rowPitchLabel = new JLabel("Pitch in microns, row:");
		JLabel colPitchLabel = new JLabel("column:");
		// put them in their own pane side-by-side
		JPanel pitchPanel = new JPanel();
		pitchPanel.add(rowPitchLabel);
		pitchPanel.add(rowPitchField);
		pitchPanel.add(colPitchLabel);
		pitchPanel.add(colPitchField);
		pitchPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(pitchPanel);
		
		// make and add tip label 
		JPanel tipPanel = new JPanel();
		tipPanel.add(new JLabel("Make sure you are currently at the upper-leftmost spot!"));
		tipPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(tipPanel);
		
		// make and add the setter button
		setB = new JButton("Add Array to position list");
		setB.setActionCommand("addP");
		setB.addActionListener(this);
		setB.setToolTipText("This will add to the XY position list");
		JPanel setPanel = new JPanel();
		setPanel.add(setB);
		setPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(setPanel);

        //frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Point2D curXY = new Point2D.Double();
		PositionList posList = null;
		try {
			posList = app_.getPositionList();
		} catch (MMScriptException e2) {
			e2.printStackTrace();
		}
		if ("clearP".equals(e.getActionCommand())) {
			// clear all positions
			posList.clearAllPositions();
			try {
				app_.setPositionList(posList);
			} catch (MMScriptException e1) {
				e1.printStackTrace();
			}
			
		} else if ("addP".equals(e.getActionCommand())) {
			// get current XY position in microns
			try {
				curXY = core_.getXYStagePosition();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String xyStageName = core_.getXYStageDevice();
			
			// make an array of MultiStagePosition objects

			int numR = Integer.parseInt(nRowField.getText());
			int numC = Integer.parseInt(nColField.getText());
			int pitchR = Integer.parseInt(rowPitchField.getText());
			int pitchC = Integer.parseInt(colPitchField.getText());
			
			
			for (int c = 0; c<numC; c++){
				for (int r = 0; r<numR; r++){
					
					double x = curXY.getX() - c*pitchC;
					double y = curXY.getY() - r*pitchR;
					
					StagePosition thisPos = new StagePosition();
					thisPos.numAxes =2;
					thisPos.stageName = xyStageName;
					thisPos.x = x;
					thisPos.y = y;
					MultiStagePosition thisMulti = new MultiStagePosition();
					thisMulti.add(thisPos);
					posList.addPosition(thisMulti);
				}
			}
			
			// put the grid into the XY position list
				try {
					app_.setPositionList(posList);
				} catch (MMScriptException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				};
				
				app_.showXYPositionList();
		}
		
	}

}
