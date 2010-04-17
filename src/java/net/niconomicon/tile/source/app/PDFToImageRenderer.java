package net.niconomicon.tile.source.app;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * 
 */

/**
 * @author niko
 * 
 */
public class PDFToImageRenderer {

	public static BufferedImage getImageFromPDF(PDFFile pdf, int pageNum, int rez) {

		int numpages = pdf.getNumPages();
//		System.out.println("Number of pages = " + numpages);
		int pagenum = 1;
		if (pagenum > numpages) pagenum = numpages;

		PDFPage page = pdf.getPage(pagenum);

		Rectangle2D r2d = page.getBBox();

		double width = r2d.getWidth();
		double height = r2d.getHeight();
		width /= 72.0;
		height /= 72.0;
		width *= rez;
		height *= rez;
//		System.out.println("Rendering with resolution  : " + rez + " final size = " + width + " x " + height);

		Image image = page.getImage((int) width, (int) height, r2d, null, true, true);
//		System.out.println("image class : "+ image.getClass());
		return (BufferedImage) image;
	}

	public static PDFFile getPDFFile(String filePath) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r");
		FileChannel fc = raf.getChannel();
		ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		return new PDFFile(buf);
	}

	public static BufferedImage getImageFromPDFFile(String filePath, int resolution) throws IOException {
		PDFFile pdfFile = getPDFFile(filePath);
		return getImageFromPDF(pdfFile, 1, resolution);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String filePath = "/Users/niko/tileSources/Prevessin_A3_Paysage.pdf";
		try {
			PDFFile pdfFile = getPDFFile(filePath);
			RenderedImage img = getImageFromPDF(pdfFile, 1, 300);

			FileOutputStream out = new FileOutputStream("test.png");
			ImageIO.write(img, "png", out);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("done");

	}

}
