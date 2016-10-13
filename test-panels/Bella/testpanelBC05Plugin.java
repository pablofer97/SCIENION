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

package org.micromanager.testpanelBC04Plugin;

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
import org.micromanager.utils.MMException;
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

import java.util.ArrayList;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class testpanelBC04Plugin implements MMPlugin, ActionListener {
	// Window label/plugin description
	public static final String menuName = "Test Panel (BC05)";
	public static final String tooltipDescription = "Image acquisition interface";
	public static final String versionNumber = "0.1";

	// Provides access to the Micro-Manager Java API (for app_ control and high-
	// level functions).
	private ScriptInterface app_;
	// Provides access to the Micro-Manager Core API (for direct hardware
	// control)
	private CMMCore core_;

	// Initializes app_ objects for usable within this package, class, and
	// subclasses
	protected JFrame frame; // API:
							// https://docs.oracle.com/javase/7/docs/api/javax/swing/JFrame.html
	protected Container panel; // API:
								// https://docs.oracle.com/javase/7/docs/api/java/awt/Container.html
	protected JButton runB, setB;
	protected JTextField nFilenameField, nXField, nYField, nAreaXField,
			nAreaYField, nChipNumField;
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
	public String getInfo() {
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
		return "Boston University, 2016";
	}

	// This is where the magic happens
	@Override
	public void show() {
		// Create the panel for content layout, using BoxLayout default
		// BoxLayout API:
		// https://docs.oracle.com/javase/7/docs/api/javax/swing/BoxLayout.html
		frame = new JFrame("testpanelBC05");
		panel = frame.getContentPane();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		// add content

		// make and add tip label
		JPanel startPosPanel = new JPanel();
		startPosPanel.add(new JLabel("Enter starting positions (mm):"));
		startPosPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(startPosPanel);

		// nXField, nYField, nAreaXField, nAreaYField
		nXField = new JTextField("0", 3);
		nYField = new JTextField("0", 3);
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
		scanAreaPanel.add(new JLabel("Enter scan area  (mm):"));
		scanAreaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(scanAreaPanel);

		// nXField, nYField, nAreaXField, nAreaYField
		nAreaXField = new JTextField("4", 3);
		nAreaYField = new JTextField("2", 3);
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
		nChipNumField = new JTextField("1", 2);
		JLabel nChipNumLabel = new JLabel("Chip number:");
		JPanel nChipNumPanel = new JPanel();
		nChipNumPanel.add(nChipNumLabel);
		nChipNumPanel.add(nChipNumField);
		nChipNumPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(nChipNumPanel);

		// make and add field for path
		nFilenameField = new JTextField(
				"e.g.: C:\\Users\\mcampana\\Downloads\\test\\", 50);
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

		// make a panel for the run button and add button to the app_ and set
		// position
		JPanel runPanel = new JPanel();
		runPanel.add(runB);
		runPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(runPanel);

		// trying to have a running report output
		updatePanel = new JPanel();
		updatePanel.add(new JLabel(" "));
		updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(updatePanel);

		// frame.setResizable(false);
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
				double exposureTime = 20.0; // TODO: should be a field input
											// along with numFrames/numRepeats
				int numRepeats = 2;
				int numFrames = 2;
				int chipNumber = Integer.parseInt(nChipNumField.getText());
				String today_date = new SimpleDateFormat("yyyy-MM-dd")
						.format(new Date());

				String defaultSavePath = "C:\\User_Scratch\\SCIENION\\Data\\testing-txt-files";
				String dirName = nFilenameField.getText(); // "C:\\Users\\mcampana\\Downloads\\test\\";

				String fileName = today_date + "_fast_acquisition_test.txt";
				File dir = new File(dirName);
				if (!dir.isDirectory()) {
					dirName = defaultSavePath;
					dir = new File(dirName);
				}
				File file = new File(dir, fileName);
				FileWriter fileWriter = new FileWriter(file);

				String message = moveStage(numX, numY, numAreaX, numAreaY,
						exposureTime, dirName, defaultSavePath, chipNumber,
						numRepeats, numFrames);

				fileWriter.write(message);
				fileWriter.flush();
				fileWriter.close();

				try {
					app_.clearMessageWindow();
					core_.stopSequenceAcquisition();
					core_.clearCircularBuffer();
					app_.enableLiveMode(false);
					app_.closeAllAcquisitions();
				} catch (MMScriptException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} catch (IOException errorMessage) {
				errorMessage.printStackTrace();
			}

		} else {
			try {
				File file = new File(
						"C:\\User_Scratch\\SCIENION\\Data\\testing-txt-files\\testing-junktest_fail.txt");
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

	public String moveStage(double startXmm, double startYmm,
			double fieldSizemmX, double fieldSizemmY, double exposureTime,
			String mirrorFile, String rootDirName, int chipNumber,
			int numRepeats, int numFrames) {

		DecimalFormat FMT2 = new DecimalFormat("#0.0");

		double now = System.currentTimeMillis();

		double width = core_.getImageWidth();
		double height = core_.getImageHeight();
		double bitDepth = core_.getImageBitDepth();

		double stepSizeX = core_.getImageWidth() * core_.getPixelSizeUm();
		double stepSizeY = core_.getImageHeight() * core_.getPixelSizeUm();

		String str1 = "height: " + height + " width: " + width + " bitDepth: "
				+ bitDepth;

		// home XY Stage
		try {
			core_.stopSequenceAcquisition();
			core_.clearCircularBuffer();

			// core_.home("xAxis"); doesn't work, needs added functionality
			// core_.home("yAxis");

		} catch (Exception e2) {
			e2.printStackTrace();
		}
		// Stages already named "xAxis" and "yAxis"

		// report starting position
		double x = 1234.5;
		double y = 1234.5;
		try {
			x = core_.getPosition("xAxis");
			y = core_.getPosition("yAxis");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String str3 = "";
		String str4 = "";

		// define test points in um
		ArrayList<Double> xPos = new ArrayList<Double>();
		ArrayList<Double> yPos = new ArrayList<Double>();
		ArrayList<Double> zPos = new ArrayList<Double>();

		double nPosX = Math.ceil(fieldSizemmX * 1000 / stepSizeX);
		double nPosY = Math.ceil(fieldSizemmY * 1000 / stepSizeY);
		String str2 = "\n\n\nInitial position (x,y) [um]: ( " + FMT2.format(x)
				+ ", " + FMT2.format(y) + " )" + "fieldSizemmX " + fieldSizemmX
				+ "fieldSizemmY " + fieldSizemmY + "nPosX " + nPosX + "nPosY"
				+ nPosY;
		double relativeX = 0.0;
		double colStartZ = 0.0;
		double colEndZ = 0.0;
		double relativeY = 0.0;
		double zHere = 0.0;

		boolean level = true; // TODO: make into a field input
		double corners[] = { 0, 0, 0, 0 };

		if (level == true) {
			corners = fourCorners(startXmm, startYmm, fieldSizemmX,
					fieldSizemmY);
		}
		double topLeftZ = corners[0];
		double topRightZ = corners[1];
		double bottomLeftZ = corners[2];
		double bottomRightZ = corners[3];

		for (int xi = 0; xi < nPosX; xi++) {
			// colStartZ = interpolate between z(0,0) and z(x_max, 0)
			// colEndZ = interpolate between z(0,y_max) and z(x_max, y_max)
			relativeX = (xi * stepSizeX) / (fieldSizemmX * 1000);
			// TODO: the below is different in Derin's code, ask why
			colStartZ = topLeftZ + relativeX * (topRightZ - topLeftZ);
			colEndZ = bottomLeftZ + relativeX * (bottomRightZ - bottomLeftZ);
			for (int yi = 0; yi < nPosY; yi++) {
				relativeY = (yi * stepSizeY) / (fieldSizemmY * 1000);
				zHere = colStartZ + relativeY * (colEndZ - colStartZ);
				// zHere = interpolate between colStartZ and colEndZ
				xPos.add(startXmm * 1000 + stepSizeX * xi);
				yPos.add(startYmm * 1000 + stepSizeY * yi);
				zPos.add(zHere);
			}
		}

		for (int i = 0; i < xPos.size(); i++) {
			double start = System.currentTimeMillis();
			try {
				// move x-axis
				core_.setFocusDevice("xAxis");
				core_.setPosition(xPos.get(i));
				core_.waitForDevice("xAxis");
				// move y-axis
				core_.setFocusDevice("yAxis");
				core_.setPosition(yPos.get(i));
				core_.waitForDevice("yAxis");
				// move z-axis
				if (level == true) {
					core_.setFocusDevice("zAxis");
					core_.setPosition(zPos.get(i));
					core_.waitForDevice("zAxis");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			double end = System.currentTimeMillis();

			double myXcoord = Math.ceil((i + 1) / nPosY); // first position is
															// (0,0)
			double myYcoord = (i % nPosY) + 1;

			str3 = str3 + "\n\n\nReached point (" + myXcoord + ", " + myYcoord
					+ ") at (" + xPos.get(i) + "," + yPos.get(i) + ")" + " in "
					+ (end - start) + " ms";

			String saveName = rootDirName + "chip" + chipNumber + "_"
					+ myXcoord + "_" + myYcoord + ".tif";

			try {
				x = core_.getPosition("xAxis");
				y = core_.getPosition("yAxis");
			} catch (Exception e) {
				e.printStackTrace();
			}

			fourColor(mirrorFile, rootDirName, saveName, (int) width,
					(int) height, exposureTime, numRepeats, numFrames);

		}

		double itTook = System.currentTimeMillis() - now;
		displayUpdate("Acq. took: " + FMT2.format(itTook)
				+ " ms; Images are stored at " + rootDirName);

		return str1 + str2 + str3 + str4;
	}

	public void fourColor(String mirrorFile, String rootDirName,
			String saveName, int width, int height, double exposureTime,
			int numRepeats, int numFrames) {

		try {
			app_.clearMessageWindow();
			core_.stopSequenceAcquisition();
			core_.clearCircularBuffer();
			app_.enableLiveMode(false);
			app_.closeAllAcquisitions();
		} catch (MMScriptException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String[] channels = new String[4];
		channels[0] = "Blue";
		channels[1] = "Green";
		channels[2] = "Orange";
		channels[3] = "Red";

		ImageStack colorStack = new ImageStack(width, height, channels.length);
		String miniStack = "";
		for (int c = 0; c < channels.length; c++) {
			try {
				core_.setConfig("LEDs", "_off");
				app_.sleep(10);
				core_.setConfig("LEDs", channels[c]);
			} catch (MMScriptException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			ImageStack metaStack = new ImageStack((int) width, (int) height,
					numRepeats);
			for (int k = 0; k < numRepeats; k++) {
				// Create a mini-stack
				miniStack = app_.getUniqueAcquisitionName("raw");
				try {
					app_.openAcquisition(miniStack, rootDirName, numFrames, 1,
							1, 1, true, false);
					core_.startSequenceAcquisition(numFrames, 200, true); // numImages,
																			// intervalMs,
																			// stopOnOverflow
				} catch (MMScriptException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				int frame = 0;
				while (frame < numFrames) {
					if (core_.getRemainingImageCount() > 0) {
						try {
							TaggedImage img = core_.popNextTaggedImage();
							app_.addImageToAcquisition(miniStack, frame, 0, 0,
									0, img);
							frame = frame + 1;

						} catch (MMScriptException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						core_.sleep(3);
					}
				}
				try {
					core_.stopSequenceAcquisition();
					core_.clearCircularBuffer();

				} catch (Exception e) {
					e.printStackTrace();
				}

				// Average the mini-stack...
				IJ.run("Z Project...", "start=0 stop=" + numFrames
						+ " projection=[Average Intensity]");
				ImagePlus miniAvg = IJ.getImage(); // this is an ImagePlus
													// object

				// ... and add it to the meta-stack
				metaStack.setPixels(miniAvg.getProcessor().getPixels(), k + 1);

				// close the mini-stack and mini-average
				try {
					app_.promptToSaveAcquisition(miniStack, false);
					// changed from (b/c deprecated):
					// app_.getAcquisition(miniStack).promptToSave(false);
					app_.closeAcquisitionWindow(miniStack);
				} catch (MMScriptException e) {
					e.printStackTrace();
				}
				miniAvg.close();
			}

			// average the meta-stack.
			ImagePlus metaSlices = new ImagePlus("repeatImages", metaStack);
			metaSlices.show();
			IJ.run("Z Project...", "start=0 stop=" + numRepeats
					+ " projection=[Average Intensity]");
			ImagePlus totalAvg = IJ.getImage();
			metaSlices.close();
			totalAvg.show();
			colorStack.setPixels(totalAvg.getProcessor().getPixels(), c + 1);
			totalAvg.close();
		}
		ImagePlus colorIms = new ImagePlus("colorImages", colorStack);
		colorIms.show();
		// save the image
		IJ.save(colorIms, saveName);
		colorIms.close();
		/*
		 * // perform mirror normalization ImagePlus mir =
		 * IJ.openImage(mirrorFile); ImageCalculator ic = new ImageCalculator();
		 * /
		 * /http://rsb.info.nih.gov/ij/developer/source/ij/plugin/ImageCalculator
		 * .java.html ImagePlus normedStack =
		 * ic.run("Divide create 32-bit stack", colorIms, mir); //
		 * normedStack.show(); IJ.run(normedStack, "Multiply...",
		 * "value=30000 stack"); converter = new ImageConverter(normedStack);
		 * converter.setDoScaling(false); converter.convertToGray16();
		 */

	}

	public double[] fourCorners(double startXmm, double startYmm,
			double fieldSizemmX, double fieldSizemmY) {
		Autofocus af = app_.getAutofocus();

		double topLeftZ = 0.0;
		double topRightZ = 0.0;
		double bottomLeftZ = 0.0;
		double bottomRightZ = 0.0;

		try {
			app_.message("Focusing in the four corners:");
			core_.setConfig("LEDs", "_off");
			app_.sleep(10);
			core_.setConfig("LEDs", "Green");

			core_.setFocusDevice("zAxis");

			app_.message("top-left");
			core_.setFocusDevice("xAxis");
			core_.setPosition(1000.0 * startXmm);
			core_.setFocusDevice("yAxis");
			core_.setPosition(1000.0 * startYmm);
			core_.setFocusDevice("zAxis");
			af.fullFocus();
			topLeftZ = core_.getPosition("zAxis");

			app_.message("top-right");
			core_.setFocusDevice("xAxis");
			core_.setPosition(1000.0 * (startXmm + fieldSizemmX));
			core_.setFocusDevice("yAxis");
			core_.setPosition(1000.0 * startXmm);
			core_.setFocusDevice("zAxis");
			af.fullFocus();
			topRightZ = core_.getPosition("zAxis");

			app_.message("bottom-left");
			core_.setFocusDevice("xAxis");
			core_.setPosition(1000.0 * startXmm);
			core_.setFocusDevice("yAxis");
			core_.setPosition(1000.0 * (startYmm - fieldSizemmY));
			core_.setFocusDevice("zAxis");
			af.fullFocus();
			bottomLeftZ = core_.getPosition("zAxis");

			app_.message("bottom-right");
			core_.setFocusDevice("xAxis");
			core_.setPosition(1000.0 * (startXmm + fieldSizemmX));
			core_.setFocusDevice("yAxis");
			core_.setPosition(1000.0 * (startYmm - fieldSizemmY));
			core_.setFocusDevice("zAxis");
			af.fullFocus();
			bottomRightZ = core_.getPosition("zAxis");
		} catch (MMScriptException e) {
			e.printStackTrace();
		} catch (MMException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * double topLeftZ = corners[0]; double topRightZ = corners[1]; double
		 * bottomLeftZ = corners[2]; double bottomRightZ = corners[3];
		 */
		double[] corners = { topLeftZ, topRightZ, bottomLeftZ, bottomRightZ };
		return corners;
	}

	protected void displayUpdate(String update) {
		updatePanel.removeAll();
		JLabel newUpdate = new JLabel(update);
		updatePanel.add(newUpdate);
		updatePanel.revalidate();
		updatePanel.repaint();
	}

}
