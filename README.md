# Bleach Correction
[![](https://github.com/fiji/CorrectBleach/actions/workflows/build-main.yml/badge.svg)](https://github.com/fiji/CorrectBleach/actions/workflows/build-main.yml)

## Author

Kota Miura.  
Bioimage Analysis & Research (BIAR).  
Heidelberg.  

<!-- Video: Christophe Leterrier(@chrislet) [[https://twitter.com/christlet/status/1445658436276867081|See here for the twitter presenting this video]]-->

## Citing the Plugin

  -  Miura K. Bleach correction ImageJ plugin for compensating the photobleaching of time-lapse sequences [version 1]. F1000Research 2020, 9:1494
     -  [doi: 10.12688/f1000research.27171.1](https://doi.org/10.12688/f1000research.27171.1)
  -  Code: Kota Miura et al. (2014). ImageJ Plugin CorrectBleach V2.0.2. Zenodo.
     - [doi: 10.5281/zenodo.30769|10.5281/zenodo.30769](http://dx.doi.org/10.5281/zenodo.30769|10.5281/zenodo.30769)

## License

Released under the [GNU General Public License v2](http://www.gnu.org/licenses/gpl-2.0.html). 

## Requires

ImageJ ver 1.34j or higher ([ImageJ, upgrade page](http://rsb.info.nih.gov/ij/upgrade/)). 

## Installation

This plugin is included as a part of Fiji since 2012. Please find it under the menu tree [Image > Adjust >]. 

If you want to use it with ImageJ1, please download the jar file from here:
  - [https://github.com/miura/CorrectBleach/releases](https://github.com/miura/CorrectBleach/releases)
  
Place the jar file under plugin folder of ImageJ and restart ImageJ. You will find the plugin in the menu [Plugins -> emblTool -> Bleach Corrector]. 

## Description

This plugin contains three different methods for correcting the intensity decay due to photobleaching. They all work with either 2D or 3D time series. In case of 3D time series, image properties should be appropriately set. If you are not sure, check your image header by [Image -> Properties].

  - **Simple Ratio Method:**
     - This method is a plugin version of Jens Rietdorf's macro (the macro used to be online but currently not available) and extended with a capability for correcting 3D time series. This method is [similar to the double normalization method](http://wiki.cmci.info/dls/FRAPmanual.htm#NormalizationWith2_3_1) explained in Phair et al. (2004), except that we do not normalize the curve. 
        - [Phair, R. D., Gorski, S. A. and Misteli, T. (2004). Measurement of dynamic protein binding to chromatin in vivo, using photobleaching microscopy. Methods Enzymol 375, 393-414.](http://www.ncbi.nlm.nih.gov/pubmed/14870680)
        - Please estimate the base line intensity before using this method. Measure the mean intensity of the region outside the target signal and use that value.
  - **Exponential Fitting Method:**
     - This method is similar to the description in the manual of [MBF-ImageJ](https://imagej.net/mbf/t.htm#t_bleach). Additionally, this plugin also works with 3D time series.
     - MBF-ImageJ uses "Exponential" equation for fitting, whereas this plugin uses "Exponential with Offset"
     - The figure below is an example of fitting exponential decay equation to the intensity changes over time. Note that this is rather an ideal case example. If you see that the fit quality is not good enough, do not use this method. Beside the evaluation of the fitting quality by eyes, use R^2 (residual) as an indicator of the quality of fit. 
     - ![image](https://user-images.githubusercontent.com/272781/216937574-469e4167-9274-4b5a-9216-ec56cd8c338e.png)
  - **Histogram Matching Method:**
     - A brand-new method for bleach correction.
     - This algorithm first samples the histogram of initial frame, and for the successive frames, [|histograms are matched](http://en.wikipedia.org/wiki/Histogram_matching) to the first frame. This avoids the increase in noise in the latter part of the sequence which is a problem in the above two methods.
     - This method does much better restoration of bleaching sequence for segmentation but not appropriate for intensity quantification.
     - See the blog entry, [for more details on this issue](http://wiki.cmci.info/blogtng/2010-05-04/photobleaching_correction_3d_time_series) and [some more notes](http://wiki.cmci.info/blogtng/2010-05-06/bleach_correction_2).

## Headless Usage
[This script](https://gist.github.com/miura/9080feb52eb74079ae393dd9320cb6ed) demonstrates the headless usage.

## Q & A

> One of our users is making timelapse experiments to track a GFP marker in cell cultures. GFP signal is very dim and background is quite strong (so SNR very poor). Over the time, background intensity decreases while specific signal keeps more or less the same so it becomes gradually more visible. He really expects the GFP to increase over the time, and he would like to quantify this increase in GFP signal over time. To compensate background bleaching he is using your bleach_corrector plugin in FIJI. He obtains the best visualization of what he expects with the Histogram Matching Method. The thing is that, as you mention in your blog's entry (http://wiki.cmci.info/downloads/bleach_corrector,  http://wiki.cmci.info/blogtng/2010-05-06/bleach_correction_2 ), with this method you cannot quantify intensities. 
>
> Why? 
> 
>Can you recommend us an alternative method to be able to quantify changes in the GFP signal over time?
>
>Other thing is at this moment it is difficult to know is wherther everything is bleached (so GFP signal kept constant reflects an increase) or wherther bleaching affects only the medium (so GFP is really constant and is not increasing, which is not what he expects...). We will make test to address this issue...
>
>Xavier Sanjuan (ALMU, Parc de Recerca Biomèdica de Barcelona),   
.on behalf of Diego Barcena (Mark Isalan group, CRG, Barcelona)

The reason that the histogram matching method cannot be used for the some type of samples, to explain in your case, is because the histogram matching algorithm assumes that the histogram shape is always constant over time. This also means that the average intensity is constant over time. However, in your case, you already know that the signal should increase assuming that the background intensity is constant. This means that you expect that histogram shape does change over time, contradicting with the assumption that the histogram matching algorithm is based on. 

One way that I can suggest to do the correction is as follows: 

  - Estimate the baseline intensity level. 
     - select a region (ROI) in the background, and do [Image > Stack > Plot z-axis profile...]. This will create a plot, expected to be decaying. Click "List" button which will then create a table with frame number and average intensity. Select All and Copy them all. 
     - Do [Analyze > Tools > Curve Fitting]. Curve fitting interface appears, so paste the copied value in the field (you probably need to delete the default values). Then fit the values using "Exponential with offset" 
     - You will then see a plot, fitted by a curve. the value "C" will be the estimate of baseline. Keep the value. 
  - Go back to your image stack, select a background region (ROI) again. This will be a region where the decay is measured. 
  - DO [Bleach correction], and select "Simple ratio"
     - you will be asked for a background value. In put the value you got in 1.3. Click OK
     - you will see the stack corrected by simple ratio method.  

