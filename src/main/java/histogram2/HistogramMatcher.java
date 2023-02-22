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
 *
 * ------
 * the code taken from the trextbook website.
 *
 */

package histogram2;

public class HistogramMatcher {
	// hA ... histogram of target image I_A
	// hR ... reference histogram
	// returns the mapping function F() to be applied to image I_A

	public int[] matchHistograms(int[] hA, int[] hR) {
		int K = hA.length;
		double[] PA = Util.Cdf(hA); // get CDF of histogram hA
		double[] PR = Util.Cdf(hR); // get CDF of histogram hR
		int[] F = new int[K]; // pixel mapping function f()

		// compute pixel mapping function f():
		for (int a = 0; a < K; a++) {
			int j = K - 1;
			do {
				F[a] = j;
				j--;
			} while (j >= 0 && PA[a] <= PR[j]);
		}
		return F;
	}

	public int[] matchHistograms(int[] hA, PiecewiseLinearCdf PR) {
		int K = hA.length;
		double[] PA = Util.Cdf(hA); // get p.d.f. of histogram Ha
		int[] F = new int[K]; // pixel mapping function f()

		// compute pixel mapping function f():
		for (int a = 0; a < K; a++) {
			double b = PA[a];
			F[a] = PR.getInverseCdf(b);
		}
		return F;
	}
}
