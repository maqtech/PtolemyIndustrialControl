import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;

/** This sample ImageJ plug-in filter inverts 8-bit images.

A few things to note:
	1) Filter plug-ins must implement the PlugInFilter interface.
	2) Plug-ins located in the "plug-in" folder must not use
	the package statement;
	3) Plug-ins residing in the "plugins" folder and with at
	least one underscore in their name will be automatically
	installed in the PlugIns menu.
	4) Plug-ins can be installed in other menus be editing
	the ij.properties file.
	5) You must edit ij.properties to get you plug-in to appear
	in the Help->About PlugIns sub-menu.
	6) The class name and file name must be the same.
	7) This filter works with ROIs, including non-rectangular ROIs.
	5) It will be called repeatedly to process all the slices in a stack.
	6) This plug-in can't be named "Invert_" because this would
	conflict with the built-in command of the same name.
*/

public class Inverter_ implements PlugInFilter {

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
	}

	public void run(ImageProcessor ip) {
		byte[] pixels = (byte[])ip.getPixels();
		int width = ip.getWidth();
		Rectangle r = ip.getRoi();
		int offset, i;
		for (int y=r.y; y<(r.y+r.height); y++) {
			offset = y*width;
			for (int x=r.x; x<(r.x+r.width); x++) {
				i = offset + x;
				pixels[i] = (byte)(255-pixels[i]);
			}
		}
	}

	void showAbout() {
		IJ.showMessage("About Inverter_...",
			"This sample plug-in filter inverts 8-bit images. Look\n" +
			"at the 'Inverter_.java' source file to see how easy it is\n" +
			"in ImageJ to process non-rectangular ROIs, to process\n" +
			"all the slices in a stack, and to display an About box."
		);
	}
}

