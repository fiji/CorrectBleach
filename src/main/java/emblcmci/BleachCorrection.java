/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2012 - 2023 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package emblcmci;

/** Bleach Correction with three different methods, for 2D and 3D time series.
 * 	contact: Kota Miura (miura@embl.de)
 *
 * 	Simple Ratio Method:
 * 		Plugin version of Jens Rietdorf's macro, additionally with 3D time series
 * 			see http://www.embl.de/eamnet/html/bleach_correction.html
 *
 *  Exponential Fitting Method:
 *  	Similar to MBF-ImageJ method, additionally with 3D time series.
 *  		See http://www.macbiophotonics.ca/imagej/t.htm#t_bleach
 *  	MBF-ImageJ suggests to use "Exponential" equation for fitting,
 *  	whereas this plugin uses "Exponential with Offset"
 *
 *  HIstogram Matching Method:
 *  	This method does much better restoration of bleaching sequence
 *  	for segmentation but might not good for intensity quantification.
 *  	See documentation at http://cmci.embl.de
 *
 *
 * Copyright © 2010 Kota Miura
 * License: GPL 2
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import org.scijava.util.VersionUtils;

public class BleachCorrection implements PlugInFilter {

	public static final int SIMPLE_RATIO=0, EXPONENTIAL_FIT=1, HISTOGRAM_MATCHING=2;
	boolean doHeadLess = false;
	double simpleratio_baseline =0.0; //default value

	ImagePlus imp;
	// ImagePlus duplicate of the original, which will be applied with the correction. 
	ImagePlus impdup;

	String[] CorrectionMethods = { "Simple Ratio", "Exponential Fit", "Histogram Matching" };

	/**
	 * Correction Method 0: simple ratio 1: exponential fit 2: histogramMatch
	 */
	private static int CorrectionMethod = SIMPLE_RATIO;

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (!showDialog()) {
			return 0;
		}
		return DOES_8G + DOES_16 + STACK_REQUIRED;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImagePlus impdup = doCorrection(imp);
		impdup.show();
	}

	public ImagePlus doCorrection(ImagePlus imp){
		this.imp = imp;
		Roi curROI = imp.getRoi();
		// System.out.println("in the method");
		if (curROI != null) {
			java.awt.Rectangle rect = curROI.getBounds();
			System.out.println("(x,y)=(" + rect.x + "," + rect.y);
			System.out.println("Width=" + rect.width);
			System.out.println("Height=" + rect.height);
		} else {
			System.out.println("No ROI");
		}
		imp.killRoi();
		impdup = new Duplicator().run(imp);
		if (curROI != null)
			impdup.setRoi(curROI);
		if (CorrectionMethod == SIMPLE_RATIO) { // Simple Ratio Method
			BleachCorrection_SimpleRatio BCSR = null;
			if (curROI == null) {
				BCSR = new BleachCorrection_SimpleRatio(impdup);
			} else {
				BCSR = new BleachCorrection_SimpleRatio(impdup, curROI);
			}
			if (!doHeadLess)
				BCSR.showDialogAskBaseline();
			else
				BCSR.setSimpleRatioBaseline(simpleratio_baseline);
			BCSR.correctBleach();
		} else if (CorrectionMethod == EXPONENTIAL_FIT) { // Exponential Fitting Method
			BleachCorrection_ExpoFit BCEF;
			if (curROI == null) {
				BCEF = new BleachCorrection_ExpoFit(impdup);
			} else {
				BCEF = new BleachCorrection_ExpoFit(impdup, curROI);
			}
			BCEF.setHeadlessProcessing(doHeadLess);
			BCEF.core();
		} else if (CorrectionMethod == HISTOGRAM_MATCHING) { // HIstogram Matching Method
			BleachCorrection_MH BCMH = null;
			// if (curROI == null) {
			BCMH = new BleachCorrection_MH(impdup);
			// } else {
			// BCMH = new BleachCorrection_MH(impdup, curROI);
			// }
			BCMH.doCorrection();
		}
		return impdup;
	}

	/**
	 * Dialog to ask which method to be used for Bleach Correction
	 *
	 * @return
	 */
	public boolean showDialog() {
		GenericDialog gd = new GenericDialog("Bleach Correction");
		gd.addChoice("Correction Method :", CorrectionMethods, CorrectionMethods[CorrectionMethod]);
		gd.addMessage("version " + VersionUtils.getVersion(getClass()));
		gd.addMessage("Citation doi: 10.12688/f1000research.27171.1");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		BleachCorrection.setCorrectionMethod(gd.getNextChoiceIndex());
		return true;

	}

	public ImagePlus getCorrectedImagePlus(){
		return impdup;
	}
	public static int getCorrectionMethod() {
		return CorrectionMethod;
	}

	public static void setCorrectionMethod(int correctionMethod) {
		CorrectionMethod = correctionMethod;
	}
	public void setHeadlessProcessing(boolean headless){
		doHeadLess = headless;
	}
	public void setSimpleRatioBaseline(double baseline){
		simpleratio_baseline = baseline;
	}

}
