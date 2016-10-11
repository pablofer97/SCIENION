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

package org.micromanager.testpanelBC03;

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

import java.util.ArrayList;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class testpanelBC03Plugin implements MMPlugin, ActionListener {
	// Window label/plugin description
	public static final String menuName = "Test Panel (BC03)";
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
		frame = new JFrame("testpanelBC03");
		panel = frame.getContentPane();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		// add content

		// make and add tip label
		JPanel startPosPanel = new JPanel();
		startPosPanel.add(new JLabel("Enter starting positions (um?):"));
		startPosPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(startPosPanel);

		// nXField, nYField, nAreaXField, nAreaYField
		nXField = new JTextField("5", 3);
		nYField = new JTextField("5", 3);
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
		nAreaXField = new JTextField("15", 3);
		nAreaYField = new JTextField("15", 3);
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
				double exposureTime = 20.0; // TODO: should be a field input?
				int chipNumber = Integer.parseInt(nChipNumField.getText());
				String today_date = new SimpleDateFormat("yyyy-MM-dd")
						.format(new Date());

				// I don't think I have permissions to write to the U:\ drive
				// So this might be limited in functionality right now
				// I can definitely write to specific folders in C:\
				String defaultSavePath = "C:\\User_Scratch\\SCIENION\\Data\\testing-txt-files\\";
				String dirName = nFilenameField.getText(); // "C:\\Users\\mcampana\\Downloads\\test\\";

				String fileName = today_date + "_fast_acquisition_test.txt";
				File dir = new File(dirName);
				if (!dir.isDirectory()) {
					dirName = defaultSavePath;
					dir = new File(dirName);
				}
				File file = new File(dir, fileName);
				FileWriter fileWriter = new FileWriter(file);

				String message = fourColor(numX, numY, numAreaX, numAreaY,
						exposureTime, dirName, defaultSavePath, chipNumber);

				fileWriter.write(message);
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException errorMessage) {
				errorMessage.printStackTrace();
			}

		} else {
			try {
				File file = new File(
						"C:\\Users\\mcampana\\Downloads\\test\\testing-junktest_fail.txt");
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

	public String fourColor(double startX, double startY, double fieldSizemmX,
			double fieldSizemmY, double exposureTime, String mirrorFile,
			String rootDirName, int chipNumber) {

		DecimalFormat FMT2 = new DecimalFormat("#0.0");

		double now = System.currentTimeMillis();

		double width = core_.getImageWidth();
		double height = core_.getImageHeight();
		double bitDepth = core_.getImageBitDepth();

		String str1 = "height: " + height + " width: " + width + " bitDepth: "
				+ bitDepth;

		// home XY Stage
		try {
			core_.stopSequenceAcquisition();
			core_.clearCircularBuffer();

			// core_.home(xyStage);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String str2 = "\n\n\nInitial position (x,y) [um]: ( " + FMT2.format(x)
				+ ", " + FMT2.format(y) + " )";
		String str3[] = new String[3];
		String str4 = "";

		// define test points in um
		ArrayList<Double> xPos = new ArrayList<Double>();
		ArrayList<Double> yPos = new ArrayList<Double>();

		xPos.add(0.0);
		yPos.add(0.0);

		xPos.add(2000.0);
		yPos.add(2000.0);

		xPos.add(4000.0);
		yPos.add(4000.0);

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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double end = System.currentTimeMillis();
			str3[i] = "\n\n\nReached point " + i + " at (" + xPos.get(i) + ","
					+ yPos.get(i) + ")" + " in " + (end - start) + " ms";

			try {
				x = core_.getPosition("xAxis");
				y = core_.getPosition("yAxis");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			str4 = "\nCurrent position [um]: " + FMT2.format(x) + ", "
					+ FMT2.format(y);
		}

		try {
			app_.sleep(10);

			double itTook = System.currentTimeMillis() - now;

			displayUpdate("Acq. took: " + FMT2.format(itTook)
					+ " ms; Images are stored at " + rootDirName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return str1 + str2 + str3[0] + str3[1] + str3[2] + str4;
	}

	protected void displayUpdate(String update) {
		updatePanel.removeAll();
		JLabel newUpdate = new JLabel(update);
		updatePanel.add(newUpdate);
		updatePanel.revalidate();
		updatePanel.repaint();
	}

}
