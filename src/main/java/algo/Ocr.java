package algo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.javatuples.Pair;

public class Ocr {

	final static int WIDTH = 32;
	final static int HEIGHT = 32;
	final static int SYMBOL_SIZE = WIDTH * HEIGHT;
	final static double THRESHOLD = 128;

	static double[] readSymbol(String filename) throws IOException {
		double[] array = new double[SYMBOL_SIZE];
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
		BufferedImage rgbImage = ImageIO.read(bis);

		byte[] pixels = ((DataBufferByte) rgbImage.getRaster().getDataBuffer()).getData();

		int comps = 3;
		if (rgbImage.getColorModel().hasAlpha()) {
			comps = 4;
		}

		for (int i = 0; i < SYMBOL_SIZE; i++) {
			int red = (int) pixels[i * comps] & 0xff;
			int blue = (int) pixels[i * comps + 1] & 0xff;
			int green = (int) pixels[i * comps + 2] & 0xff;
//			double gray = 0.299 * red + 0.587 * green + 0.114 * blue;
//			array[i] = 1 - gray;
			if (red < THRESHOLD || green < THRESHOLD || blue < THRESHOLD) {
				array[i] = 1.0;
			}

		}
		return array;
	}

	static void debugSymbol(double[] symbol) {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				if (symbol[i * WIDTH + j] == 1.0) {
					System.out.print("@");
				} else {
					System.out.print(".");
				}
			}
			System.out.println();
		}
	}

	static void recurseFiles(String path, List<Pair<String, File>> filesList) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		for (File f : list) {
			if (f.isDirectory()) {
				recurseFiles(f.getAbsolutePath(), filesList);
				System.out.println("Dir:" + f.getAbsoluteFile());
			} else {
				// System.out.println("File:" + f.getAbsoluteFile() + " - " +
				// Paths.get(f.getParentFile().toString()).getFileName() );
				filesList.add(new Pair<String, File>(Paths.get(f.getParentFile().toString()).getFileName().toString(),
						f.getAbsoluteFile()));
			}
		}
	}

	public static void main(String[] args) throws IOException {
		List<Pair<String, File>> filesList = new ArrayList<>();
		recurseFiles(
				"C:\\\\Users\\\\rodoc\\\\HOME\\\\developpement\\\\eclipse-workspace\\\\neural\\\\src\\\\main\\\\resources\\\\mathdataset32",
				filesList);

		Map<String, List<double[]>> inputsMap = new HashMap<>();
		for (Pair<String, File> p : filesList) {
			System.out.println(p.getValue0() + "->" + p.getValue1());
			double[] input = Ocr.readSymbol(p.getValue1().getAbsolutePath());

			List<double[]> list = inputsMap.get(p.getValue0());
			if (list == null) {
				List<double[]> newList = new ArrayList<>();
				inputsMap.put(p.getValue0(), newList);
			}
			list = inputsMap.get(p.getValue0());
			list.add(input);
		}

		for (String k : inputsMap.keySet()) {
			System.out.println(k + "->" + inputsMap.get(k).size());
		}

		// prepare inputs & outputs
		double batchRatio = 0.75;
		int symbolsSize = inputsMap.keySet().size();
		int i = 0;
		for (String k : inputsMap.keySet()) {
			// take batchratio input from dataset
			int batchSize = (int) (inputsMap.get(k).size() * batchRatio);
			List<double[]> inputsList = inputsMap.get(k);
			double[][] inputs = new double[symbolsSize][batchSize];
			for (int j = 0; j < batchSize; j++) {
//				inputs[i][j] = inp
			}
			i++;
		}
	}
}
