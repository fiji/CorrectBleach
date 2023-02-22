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
/**
 * This sample code is made available as part of the book "Digital Image
 * Processing - An Algorithmic Introduction using Java" by Wilhelm Burger
 * and Mark J. Burge, Copyright (C) 2005-2008 Springer-Verlag Berlin,
 * Heidelberg, New York.
 * Note that this code comes with absolutely no warranty of any kind.
 * See http://www.imagingbook.com for details and licensing conditions.
 *
 * Date: 2007/11/10
 */

package histogram2;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;

public class HistogramPlot {
	static final int BACKGROUND = 255;
	// String title = "Histogram";
	int width = 256;
	int height = 128;
	int base = height - 1;
	int paintValue = 0;
	ImagePlus hist_img;
	ImageProcessor ip;
	int[] H = new int[256];

	public HistogramPlot(double[] nH, String title) {
		createHistogramImage(title);
		// nH mus be a normalized histogram of length 256
		for (int i = 0; i < nH.length; i++) {
			H[i] = (int) Math.round(height * nH[i]);
		}
		draw();
		// show();
	}

	public HistogramPlot(PiecewiseLinearCdf cdf, String title) {
		createHistogramImage(title);
		// nH mus be a normalized histogram of length 256
		for (int i = 0; i < 256; i++) {
			H[i] = (int) Math.round(height * cdf.getCdf(i));
		}
		draw();
		// show();
	}

	void createHistogramImage(String title) {
		if (title == null)
			title = "Histogram Plot";
		hist_img = NewImage.createByteImage(title, width, height, 1, 0);
		ip = hist_img.getProcessor();
		ip.setValue(BACKGROUND);
		ip.fill();
	}

	void draw() {
		ip.setValue(0);
		ip.drawLine(0, base, width - 1, base);
		ip.setValue(paintValue);
		int u = 0;
		for (int i = 0; i < H.length; i++) {
			int k = H[i];
			if (k > 0) {
				ip.drawLine(u, base - 1, u, base - k);
				// ip.drawLine(u+1,base-1,u+1,base-k);
			}
			u = u + 1;
		}
	}

	void update() {
		hist_img.updateAndDraw();
	}

	public void show() {
		hist_img.show();
		update();
	}

	void makeRamp() {
		for (int i = 0; i < H.length; i++) {
			H[i] = i;
		}
	}

	void makeRandom() {
		for (int i = 0; i < H.length; i++) {
			H[i] = (int) (Math.random() * height);
		}
	}

	// ----- static methods ----------------------

	public static void showHistogram(ImageProcessor ip, String title) {
		int[] Ha = ip.getHistogram();
		double[] nH = Util.normalizeHistogram(Ha);
		HistogramPlot hp = new HistogramPlot(nH, title);
		hp.show();
	}

	public static void showCumHistogram(ImageProcessor ip, String title) {
		int[] Ha = ip.getHistogram();
		// double[] nH = HistSpec.Pdf(Ha);
		double[] cH = Util.Cdf(Ha);
		HistogramPlot hp = new HistogramPlot(cH, title);
		hp.show();
	}

}
