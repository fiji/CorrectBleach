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

/** Bleach Correction by Fitting Exponential Decay function.
 *  Kota Miura (miura@embl.de)
 *
 * 2D and 3D time series corrected by fitting exponential decay function.
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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.plugin.frame.Fitter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

public class BleachCorrection_ExpoFit {
	ImagePlus imp;
	boolean is3DT = false;
	Roi curROI = null;
	boolean doHeadLess = false;
	boolean verbose = false;

	/**
	 * @param imp
	 */
	public BleachCorrection_ExpoFit(ImagePlus imp) {
		super();
		this.imp = imp;
	}

	public BleachCorrection_ExpoFit(ImagePlus imp, Roi curROI) {
		super();
		this.imp = imp;
		this.curROI = curROI;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setHeadlessProcessing(boolean headless){
		this.doHeadLess = headless;
	}

	/**
	 * Fit the mean intensity time series of given ImagePlus in this class.
	 * fit equation is 11, parameter from
	 * http://rsb.info.nih.gov/ij/developer/api/constant-values.html#ij.measure.CurveFitter.STRAIGHT_LINE
	 * 
	 * @return an instance of CurveFitter
	 */
	public CurveFitter dcayFitting() {
		ImageProcessor curip;
		ImageStatistics imgstat;
		double[] xA = new double[imp.getStackSize()];
		double[] yA = new double[imp.getStackSize()];

		if (curROI == null)
			curROI = new Roi(0, 0, imp.getWidth(), imp.getHeight());

		for (int i = 0; i < imp.getStackSize(); i++) {
			curip = imp.getImageStack().getProcessor(i + 1);
			curip.setRoi(curROI);
			imgstat = curip.getStatistics();
			xA[i] = i;
			yA[i] = imgstat.mean;
		}
		CurveFitter cf = new CurveFitter(xA, yA);
		double firstframeint = yA[0];
		double lastframeint = yA[yA.length - 1];
		double guess_a = firstframeint - lastframeint;
		if (guess_a <= 0) {
			IJ.error("This sequence seems to be not decaying");
			return null;
		}
		double guess_c = lastframeint;
		double maxiteration = 2000;
		double NumRestarts = 2;
		double errotTol = 10;
		double[] fitparam = { -1 * guess_a, -0.0001, guess_c, maxiteration, NumRestarts, errotTol };

		cf.setInitialParameters(fitparam);
		cf.doFit(11); //

		if (verbose) IJ.log("without GUI:" + GraphicsEnvironment.isHeadless());
		if (verbose) IJ.log("headless settings:" +  doHeadLess);		
		if ((!GraphicsEnvironment.isHeadless()) && (doHeadLess != true)){
				Fitter.plot(cf);
		}
		IJ.log(cf.getResultString());
		return cf;
	}

	/**
	 * Curve fitting for 3D time series is done with average intensity value for
	 * wach time point (stack intensity mean is used, so the fitted points = time
	 * point, not slice number)
	 *
	 * @param zframes
	 * @param tframes
	 * @return
	 */
	public CurveFitter decayFitting3D(int zframes, int tframes) {
		ImageProcessor curip;
		ImageStatistics imgstat;
		double[] xA = new double[tframes];
		double[] yA = new double[tframes];
		double curStackMean = 0.0;
		if (curROI == null)
			curROI = new Roi(0, 0, imp.getWidth(), imp.getHeight());
		for (int i = 0; i < tframes; i++) {
			curStackMean = 0.0;
			for (int j = 0; j < zframes; j++) {
				curip = imp.getImageStack().getProcessor(i * zframes + j + 1);
				curip.setRoi(curROI);
				imgstat = curip.getStatistics();
				curStackMean += imgstat.mean;
			}
			curStackMean /= zframes;
			xA[i] = i;
			yA[i] = curStackMean;
		}
		CurveFitter cf = new CurveFitter(xA, yA);
		double firstframeint = yA[0];
		double lastframeint = yA[yA.length - 1];
		double guess_a = firstframeint - lastframeint;
		if (guess_a <= 0) {
			IJ.error("This sequence seems to be not decaying");
			return null;
		}
		double guess_c = lastframeint;
		double maxiteration = 2000;
		double NumRestarts = 2;
		double errotTol = 10;
		double[] fitparam = { -1 * guess_a, -0.0001, guess_c, maxiteration, NumRestarts, errotTol };

		cf.setInitialParameters(fitparam);

		cf.doFit(11); //
		if ((!GraphicsEnvironment.isHeadless()) || (doHeadLess != true)) {
			Fitter.plot(cf);
		}
		IJ.log(cf.getResultString());
		return cf;
	}

	/**
	 * calculate estimated value from fitted "Exponential with Offset" equation
	 *
	 * @param a magnitude (difference between max and min of curve)
	 * @param b exponent, defines degree of decay
	 * @param c offset.
	 * @param x timepoints (or time frame number)
	 * @return estimate of intensity at x
	 */
	public double calcExponentialOffset(double a, double b, double c, double x) {
		return (a * Math.exp(-b * x) + c);
	}

	/**
	 * does both decay fitting and bleach correction.
	 *
	 */
	public void core() {
		int[] impdimA = imp.getDimensions();
		IJ.log("slices" + Integer.toString(impdimA[3]) + "  -- frames" + Integer.toString(impdimA[4]));
		// IJ.log(Integer.toString(imp.getNChannels())+":"+Integer.toString(imp.getNSlices())+":"+
		// Integer.toString(imp.getNFrames()));
		int zframes = impdimA[3];
		int tframes = impdimA[4];
		if (impdimA[3] > 1 && impdimA[4] > 1) { // if slices and frames are both more than 1
			is3DT = true;
			if ((impdimA[3] * impdimA[4]) != imp.getStackSize()) {
				IJ.showMessage("slice and time frames do not match with the length of the stack. Please correct!");
				return;
			}
		}
		CurveFitter cf;
		if (is3DT)
			cf = decayFitting3D(zframes, tframes);
		else
			cf = dcayFitting();
		double[] respara = cf.getParams();
		double res_a = respara[0];
		double res_b = respara[1];
		double res_c = respara[2];
		double ratio = 0.0;
		ImageProcessor curip;
		System.out.println(res_a + "," + res_b + "," + res_c);
		if (is3DT) {
			for (int i = 0; i < tframes; i++) {
				for (int j = 0; j < zframes; j++) {
					curip = imp.getImageStack().getProcessor(i * zframes + j + 1);
					ratio = calcExponentialOffset(res_a, res_b, res_c, 0.0)
							/ calcExponentialOffset(res_a, res_b, res_c, (double) ( i ));
					curip.multiply(ratio);
				}
			}
		} else {
			if (verbose)
				IJ.log("Original Int" + "\t" + "Corrected Int"+ "\t" + "Ratio");

			for (int i = 0; i < imp.getStackSize(); i++) {
				curip = imp.getImageStack().getProcessor(i + 1);

				double orgint = curip.getStatistics().mean;
				
				ratio = calcExponentialOffset(res_a, res_b, res_c, 0.0)
						/ calcExponentialOffset(res_a, res_b, res_c, (double) ( i ));
				curip.multiply(ratio);

				double corint = curip.getStatistics().mean;

				//for testing
				if (verbose) {
					String monitor = Double.toString(orgint) + "\t" + Double.toString(corint) + "\t" +
							Double.toString(ratio);
					IJ.log(monitor);
				}
			}
		}
	}

	public static void main(String[] args) {
		// test using synthetic bleaching
		int frames = 50;
		ImagePlus imp = NewImage.createByteImage("testBleach",
				25, 25, frames, NewImage.FILL_BLACK);
		BleachCorrection_ExpoFit bce = new BleachCorrection_ExpoFit(imp);
		int offset = 10;
		int amp = 150;
		double exp = 0.05;
		ArrayList<Integer> orgintA = new ArrayList<Integer>();
		for (int f = 0; f < frames; f++) {
			double intensity = bce.calcExponentialOffset(amp, exp, offset, f);
			ImageProcessor ip = imp.getStack().getProcessor(f + 1);
			int grayscale = (int) intensity;
			ip.setColor(grayscale);
			ip.fill();
			orgintA.add(grayscale);
		}
		ImagePlus dupimp = imp.duplicate();
		bce = new BleachCorrection_ExpoFit(dupimp);
		bce.setVerbose(true);		
		bce.core();
		dupimp.show();
		IJ.log("Original Int" + "\t" + "Corrected Int");
		for (int f = 0; f < frames; f++) {
			double orgstatMean = imp.getStack().getProcessor(f + 1).getStatistics().mean;
			double corstatMean = dupimp.getStack().getProcessor(f + 1).getStatistics().mean;
			IJ.log(String.valueOf(orgstatMean) + "\t" + String.valueOf(corstatMean));
		}

	}
}
