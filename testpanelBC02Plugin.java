/**
 * Written by Bella Campana (start date: 2016-09-12).
 * 
 * Modified from micromanager HelloWorld and 
 * ArrayScan plugin written by Derin Sevenler (Unlu lab, BU, 2015)
 * 
 * This will start the process of interfacing with previously written code to run 
 * machines and stuff, and will therefore be hard to bug test.  Wish me luck!
 * 
 * To create a new one of these, follow instructions at:
 * https://micro-manager.org/wiki/Writing_plugins_for_Micro-Manager
 */

package org.micromanager.testpanelBC02Plugin;

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
import mmcorej.TaggedImage;

import org.micromanager.api.Autofocus;
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


// fourColor 
import ij.*;
import ij.plugin.ImageCalculator;
import ij.process.*;
import ij.measure.*;

import java.util.Date;
import java.text.SimpleDateFormat;

public class testpanelBC02Plugin implements MMPlugin, ActionListener {
	// Window label/plugin description
   public static final String menuName = "Test Panel (BC02)";
   public static final String tooltipDescription =
      "Image acquisition interface";
   public static final String versionNumber = "0.1";

   // Provides access to the Micro-Manager Java API (for app_ control and high-
   // level functions).
   private ScriptInterface app_;
   // Provides access to the Micro-Manager Core API (for direct hardware
   // control)
   private CMMCore core_;
   
   // Initializes app_ objects for usable within this package, class, and subclasses
   protected JFrame frame; // API: https://docs.oracle.com/javase/7/docs/api/javax/swing/JFrame.html
   protected Container panel; // API:  https://docs.oracle.com/javase/7/docs/api/java/awt/Container.html
   protected JButton runB, setB;
   protected JTextField nFilenameField, nXField, nYField, nAreaXField, nAreaYField, nChipNumField;
   protected JPanel updatePanel;
   
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
		
		// make and add field for chip number
		nChipNumField = new JTextField("1",2);
		JLabel nChipNumLabel = new JLabel("Chip number:");
		JPanel nChipNumPanel = new JPanel();
		nChipNumPanel.add(nChipNumLabel);
		nChipNumPanel.add(nChipNumField);
		nChipNumPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(nChipNumPanel);
		
		// make and add field for path
		nFilenameField = new JTextField("e.g.: C:\\Users\\mcampana\\Downloads\\test\\",50);
		JLabel nFileLabel = new JLabel("Save path:");
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
		
		// make a panel for the run button and add button to the app_ and set position
		JPanel runPanel = new JPanel();
		runPanel.add(runB);
		runPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(runPanel);
		
		// trying to have a running report output
		updatePanel = new JPanel();
		updatePanel.add(new JLabel(" "));
		updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(updatePanel);
	   
	 //frame.setResizable(false);
       frame.pack();
       frame.setVisible(true);
       frame.setLocationByPlatform(true);
   }
   
   
   @Override
	public void actionPerformed(ActionEvent e) {
		// Point2D curXY = new Point2D.Double();

		if ("runP".equals(e.getActionCommand())) {
			// clear all positions
			try {
				double numX = Double.parseDouble(nXField.getText());
				double numAreaX = Double.parseDouble(nAreaXField.getText());
				double numY = Double.parseDouble(nYField.getText());
				double numAreaY = Double.parseDouble(nAreaYField.getText());
				double exposureTime = 20.0; //  TODO: should be a field input?
				int chipNumber = Integer.parseInt(nChipNumField.getText()); 
				String today_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				
				// I don't think I have permissions to write to the U:\ drive
				// So this might be limited in functionality right now
				// I can definitely write to specific folders in C:\
				String defaultSavePath = "C:\\Users\\mcampana\\Downloads\\test\\";
				String dirName = nFilenameField.getText(); //"C:\\Users\\mcampana\\Downloads\\test\\";

				String fileName = today_date + "_fast_acquisition_test.txt"; //TODO: final file format will not be .txt
				File dir = new File (dirName);
				if(!dir.isDirectory()) {
					dirName=defaultSavePath;
					dir = new File (dirName);
				}
				File file = new File (dir, fileName);
				FileWriter fileWriter = new FileWriter(file);
				
				String message = fourColor(numX,numY,numAreaX,numAreaY,exposureTime,dirName,defaultSavePath,chipNumber);

				fileWriter.write(message);
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException errorMessage) {
				errorMessage.printStackTrace();
			}
			
		} else  {
			try {
				File file = new File("C:\\Users\\mcampana\\Downloads\\test\\testing-junktest_fail.txt");
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
   
   public String fourColor(double startX,double startY,double fieldSizemmX,double fieldSizemmY,double exposureTime,String mirrorFile,String rootDirName,int chipNumber) {

	   double now = System.currentTimeMillis();
	   /*
    File f = new File(rootDirName+"dummy");
	f.getParentFile().mkdirs();
				
	try {
		core_.setExposure(exposureTime);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	String[] channels = new String[4];
	channels[0] = "Blue";
	channels[1] = "Green";
	channels[2] = "Orange";
	channels[3] = "Red";

	double width = core_.getImageWidth();
	double height = core_.getImageHeight();
	double bitDepth = core_.getImageBitDepth();

	try {
		app_.clearMessageWindow();
		core_.stopSequenceAcquisition();
		core_.clearCircularBuffer();
		app_.enableLiveMode(false);
		app_.closeAllAcquisitions();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	int numRepeats = 2;
	int numFrames = 8;

	app_.message("averaging " + (numFrames*numRepeats) + " frames at each color & position.");

	app_.message("Focusing in the four corners:");
	core_.setConfig("LEDs", ".Off");
	app_.sleep(10);
	core_.setConfig("LEDs", channels[1]);

	core_.setFocusDevice("zAxis");
	Autofocus af = app_.getAutofocus();

	app_.message("top-left");
	core_.setFocusDevice("xAxis");
	core_.setPosition(startX);
	core_.setFocusDevice("yAxis");
	core_.setPosition(startY);
	core_.setFocusDevice("zAxis");
	af.fullFocus();
	double topLeftZ = core_.getPosition("zAxis"); // we should already be in focus here.

	app_.message("top-right");
	core_.setFocusDevice("xAxis");
	core_.setPosition(startX+1000.0*fieldSizemmX);
	core_.setFocusDevice("yAxis");
	core_.setPosition(startY);
	core_.setFocusDevice("zAxis");
	af.fullFocus();
	double topRightZ = core_.getPosition("zAxis");

	app_.message("bottom-left");
	core_.setFocusDevice("xAxis");
	core_.setPosition(startX);
	core_.setFocusDevice("yAxis");
	core_.setPosition(startY-1000.0*fieldSizemmY);
	core_.setFocusDevice("zAxis");
	af.fullFocus();
	double bottomLeftZ = core_.getPosition("zAxis");

	app_.message("bottom-right");
	core_.setFocusDevice("xAxis");
	core_.setPosition(startX+1000.0*fieldSizemmX);
	core_.setFocusDevice("yAxis");
	core_.setPosition(startY-1000.0*fieldSizemmY);
	core_.setFocusDevice("zAxis");
	af.fullFocus();
	double bottomRightZ = core_.getPosition("zAxis");


	int newPct = 1; // 20% of width overlap with EACH neighboring image
	double stepSizeX = 1242.14*newPct; // field-of-view for 10x objective in um
	double stepSizeY = 995.17*newPct;

	double nPosX = Math.ceil(fieldSizemmX*1000.0 / stepSizeX);
	double nPosY = Math.ceil(fieldSizemmY*1000.0 / stepSizeY);
	
	//initialize some variables before for loop
	double thisX = 0;
	double thisY = 0;
	double relativeX = 0;
	double relativeY = 0;
	double colStartZ = 0;
	double colEndZ = 0;
	double zPos = 0;
	int currentFrame = 0;
	
	double relativeX = 0;

	String saveName = "";
	String miniStack = "";
	ImagePlus miniAvg = null;
	TaggedImage img = null;
	ImagePlus totalAvg = null;
	ImagePlus metaSlices = null;
	ImagePlus mir = null;
	ImageCalculator ic = null;
	ImagePlus normedStack = null;
	ImageConverter converter = null;
	
	ImageStack colorStack = new ImageStack((int)width, (int)height, channels.length);
	ImageStack metaStack = new ImageStack((int)width, (int)height, numRepeats);
	
	for(int idx = 0; idx<nPosX; idx++){
		// Move descending column-wise, across left-to-right
		core_.setFocusDevice("xAxis");
		thisX = startX + idx*stepSizeX;
		core_.setPosition(thisX);

		// Calculate the start and end Z for this column of spots
		relativeX = (idx*stepSizeX)/(fieldSizemmX*1000);
		colStartZ = topLeftZ + relativeX*(topRightZ-bottomLeftZ);
		colEndZ = bottomLeftZ + relativeX*(bottomRightZ-bottomLeftZ);

		for(int idy = 0; idy<nPosY; idy++){
			// Move down the column - starting at y=max
			core_.setFocusDevice("yAxis");
			thisY = startY -idy*stepSizeY;
			core_.setPosition(thisY);
			app_.message("Position " + (idy+1+(nPosY*idx)) + " of " + (nPosY*nPosX));

			// Calculate and move to corresponding Z position
			relativeY = (idy*stepSizeY)/(fieldSizemmY*1000);
			zPos = colStartZ + relativeY*(colEndZ- colStartZ);
			core_.setFocusDevice("zAxis");
			core_.setPosition(zPos);

			
			for(int c=0; c<channels.length; c++){
				app_.message("Scanning " + channels[c]);
				
				core_.setConfig("LEDs", ".Off");
				app_.sleep(10);
				core_.setConfig("LEDs", channels[c]);
				
				for (int k=0; k< numRepeats; k++)  {
					// Create a mini-stack
					miniStack = app_.getUniqueAcquisitionName("raw");
					app_.openAcquisition(miniStack, rootDirName, numFrames, 1, 1, 1, false, false); // TODO: not sure these booleans should be false
					core_.startSequenceAcquisition(numFrames, 200, true); //numImages, intervalMs, stopOnOverflow
					currentFrame = 0;
					while(currentFrame<numFrames) {
						if (core_.getRemainingImageCount() > 0) {
							img = core_.popNextTaggedImage();
							app_.addImageToAcquisition(miniStack, currentFrame, 0, 0, 0, img);
							currentFrame = currentFrame+1;
						}
						else {
							core_.sleep(3);
						}
					}
					core_.stopSequenceAcquisition();
					core_.clearCircularBuffer();

					// Average the mini-stack...
					IJ.run("Z Project...", "start=0 stop=" + numFrames + " projection=[Average Intensity]");
					miniAvg = IJ.getImage(); // this is an ImagePlus object

					// ... and add it to the meta-stack
					metaStack.setPixels(miniAvg.getProcessor().getPixels(), k+1);

					// close the mini-stack and mini-average
					app_.getAcquisition(miniStack).promptToSave(false);
					app_.closeAcquisitionWindow(miniStack);
					miniAvg.close();
				}

				// average the meta-stack.
				metaSlices = new ImagePlus("repeatImages", metaStack);
				metaSlices.show();
				IJ.run("Z Project...", "start=0 stop=" + numRepeats + " projection=[Average Intensity]");
				totalAvg = IJ.getImage();
				metaSlices.close();
				totalAvg.show();
				colorStack.setPixels(totalAvg.getProcessor().getPixels(), c+1);
				totalAvg.close();
			}
			ImagePlus colorIms = new ImagePlus("colorImages", colorStack);
			colorIms.show();

			// perform mirror normalization
			mir = IJ.openImage(mirrorFile);
			ic = new ImageCalculator(); //http://rsb.info.nih.gov/ij/developer/source/ij/plugin/ImageCalculator.java.html
			normedStack = ic.run("Divide create 32-bit stack", colorIms, mir);
			//		normedStack.show();
			IJ.run(normedStack, "Multiply...", "value=30000 stack");
			converter = new ImageConverter(normedStack);
			converter.setDoScaling(false);
			converter.convertToGray16();
			
			// save the image
			saveName = rootDirName + "chip" + chipNumber + "_" + idx + "_" + idy + ".tif";
			IJ.save(normedStack, saveName);
			//		normedStack.close();
			colorIms.close();


		}
	}
	*/
	double itTook = System.currentTimeMillis() - now;

	displayUpdate("Acquisition took " + itTook/60000 + " minutes. Images are stored at " + rootDirName );

	
	//app_.message("Acquisition of " + (nPosX*nPosY) + " fields took " + itTook/60000 + " minutes");

	   
	   return "buttmunchers";
   }
   
   protected void displayUpdate(String update){
	   updatePanel.removeAll();
	   JLabel newUpdate=new JLabel(update);
	   updatePanel.add(newUpdate);
	   updatePanel.revalidate();
	   updatePanel.repaint();
   }
   
   
}
