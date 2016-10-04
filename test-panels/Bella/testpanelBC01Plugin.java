/**
 * Written by Bella Campana (start date: 2016-09-12).
 * 
 * Modified from micromanager HelloWorld and 
 * ArrayScan plugin written by Derin Sevenler (Unlu lab, BU, 2015)
 * 
 * This is a simple GUI interface for micromanager/ImageJ.
 * 
 */

package org.micromanager.testpanelBC01;

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
import javax.swing.JOptionPane;

import mmcorej.CMMCore;

import org.micromanager.api.MMPlugin;
import org.micromanager.api.MultiStagePosition;
import org.micromanager.api.PositionList;
import org.micromanager.api.ScriptInterface;
import org.micromanager.api.StagePosition;
import org.micromanager.utils.MMScriptException;

// File I/O
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class testpanelBC01Plugin implements MMPlugin, ActionListener {
	// Window label/plugin description
   public static final String menuName = "Test Panel (BC01)";
   public static final String tooltipDescription =
      "Simple image acquisition interface";
   public static final String versionNumber = "0.1";

   // Provides access to the Micro-Manager Java API (for GUI control and high-
   // level functions).
   private ScriptInterface app_;
   // Provides access to the Micro-Manager Core API (for direct hardware
   // control)
   private CMMCore core_;
   
   // Initializes GUI objects for usable within this package, class, and subclasses
   protected JFrame frame; // API: https://docs.oracle.com/javase/7/docs/api/javax/swing/JFrame.html
   protected Container panel; // API:  https://docs.oracle.com/javase/7/docs/api/java/awt/Container.html
   protected JButton runB, setB;
   protected JTextField nFilenameField, nXField, nYField, nAreaXField, nAreaYField;

   // required meta functions
	
   @Override
   public void setApp(ScriptInterface app) {
      app_ = app;
      core_ = app.getMMCore();
   }

   @Override
   public void dispose() {
      // We do nothing here as the only object we create, our dialog, should
      // be dismissed by the user.
   }
   
   @Override
   public String getInfo () {
      return tooltipDescription;
   }

   @Override
   public String getDescription() {
      return tooltipDescription;
   }
   
   // In Derin's code, this returns the variable versionNumber
   // For increased generality, this is a recommended change
   @Override
   public String getVersion() {
      return "1.0";
   }
   
   @Override
   public String getCopyright() {
      return "Boston University, 2015";
   }
   
   // This is where the magic happens
   @Override
   public void show() {
	// Create the panel for content layout, using BoxLayout default
	// BoxLayout API: https://docs.oracle.com/javase/7/docs/api/javax/swing/BoxLayout.html
		frame = new JFrame("testpanelBC01");
		panel = frame.getContentPane();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	   
		// add content
		
		// make and add tip label
		JPanel startPosPanel = new JPanel();
		startPosPanel.add(new JLabel("Enter starting positions (um?):"));
		startPosPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(startPosPanel);
		
		//nXField, nYField, nAreaXField, nAreaYField
		nXField = new JTextField("55",3);
		nYField = new JTextField("60",3);
		// labels
		JLabel nXLabel = new JLabel("X: ");
		JLabel nYLabel = new JLabel("     Y: ");
		// put them in their own pane side-by-side
		JPanel nXYPanel = new JPanel();
		nXYPanel.add(nXLabel);
		nXYPanel.add(nXField);
		nXYPanel.add(nYLabel);
		nXYPanel.add(nYField);
		nXYPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(nXYPanel);
		
		// make and add tip label
		JPanel scanAreaPanel = new JPanel();
		scanAreaPanel.add(new JLabel("Enter scan area  (um?):"));
		scanAreaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(scanAreaPanel);
		
		//nXField, nYField, nAreaXField, nAreaYField
		nAreaXField = new JTextField("100",3);
		nAreaYField = new JTextField("120",3);
		// labels
		JLabel nAreaXLabel = new JLabel("dX: ");
		JLabel nAreaYLabel = new JLabel("     dY: ");
		// put them in their own pane side-by-side
		JPanel nAreaXYPanel = new JPanel();
		nAreaXYPanel.add(nAreaXLabel);
		nAreaXYPanel.add(nAreaXField);
		nAreaXYPanel.add(nAreaYLabel);
		nAreaXYPanel.add(nAreaYField);
		nAreaXYPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(nAreaXYPanel);
		
		// make and add two fields for number of rows and columns
		nFilenameField = new JTextField("e.g.: C:\\Users\\mcampana\\Downloads\\test\\",50);
		// labels
		JLabel nFileLabel = new JLabel("Save path:");
		// put them in their own pane side-by-side
		JPanel nFilenamePanel = new JPanel();
		nFilenamePanel.add(nFileLabel);
		nFilenamePanel.add(nFilenameField);
		nFilenamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(nFilenamePanel);
		
		
		// make the 'run' button
		runB = new JButton("Run");
		runB.setActionCommand("runP");
		runB.addActionListener(this);
		runB.setToolTipText("Initiates scan using input parameters, then saves image to the designated folder");
		
		// make a panel for the run button and add button to the GUI and set position
		JPanel runPanel = new JPanel();
		runPanel.add(runB);
		runPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(runPanel);
	   
	 //frame.setResizable(false);
       frame.pack();
       frame.setVisible(true);
   }
   
   
   @Override
	public void actionPerformed(ActionEvent e) {
		// Point2D curXY = new Point2D.Double();

		if ("runP".equals(e.getActionCommand())) {
			// clear all positions
			try {
				// I don't think I have permissions to write to the U:\ drive
				// So this might be limited in functionality right now
				// I can definitely write to specific folders in C:\
				String defaultSavePath = "C:\\Users\\mcampana\\Downloads\\test\\";
				String dirName = nFilenameField.getText(); //"C:\\Users\\mcampana\\Downloads\\test\\";
				String fileName = "test_success.txt";
				File dir = new File (dirName);
				if(!dir.isDirectory()) {
					dirName=defaultSavePath;
					dir = new File (dirName);
				}
				File file = new File (dir, fileName);
				FileWriter fileWriter = new FileWriter(file);
				
				String message1 = "You win " + nXField.getText() + " candies!\n";
				String message2 = "AKA, " + nAreaXField.getText() + " diabeetuseez!!!\n";
				
				fileWriter.write(message1+message2);
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException errorMessage) {
				errorMessage.printStackTrace();
			}
			
		} else  {
			try {
				File file = new File("U:\\eng_research_ocn\\Users\\mcampana\\SCIENION\\Software\\testing-junktest_fail.txt");
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write("You are ");
				fileWriter.write("a failure");
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException errorMessage) {
				errorMessage.printStackTrace();
				}
			}

		}
   
   
}
