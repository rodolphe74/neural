package algo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.javatuples.Pair;

import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.pdf.PDFProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;

public class NetworkDrawer {

    static float NEURON_RADIUS = 5.0f;
    static float NEURON_GAP = 2.0f;
    static float WEIGHT_SHIFT = 2f;
    static float LAYER_GAP = 20.0f;
    static float START_X = 10.0f;
    static float START_Y = 10.0f;
    static float NEURON_FONT_FACTOR = 0.2f;
    static float WEIGHT_FONT_FACTOR = 0.1f;

    static void draw(Network network, String filename) throws IOException {
        float currentLayerColumn = 10.0f;
        float currentLayerRow = 10.0f;

        currentLayerColumn = START_X;
        currentLayerRow = START_Y;

        Graphics2D vg2d = new VectorGraphics2D();
        vg2d.setStroke(new BasicStroke(.3f));
        CommandSequence commands = ((VectorGraphics2D) vg2d).getCommands();

        Font currentFont = vg2d.getFont();
        Font neuronFont = currentFont.deriveFont(currentFont.getSize() * NEURON_FONT_FACTOR);
        Font weightFont = currentFont.deriveFont(currentFont.getSize() * WEIGHT_FONT_FACTOR);

        FontRenderContext fontRenderContext = vg2d.getFontRenderContext();

        Map<Neuron, Pair<Float, Float>> neuronsCoordinates = new HashMap<>();

        for (Layer l : network.getLayers()) {
            for (Neuron n : l.getNeurons()) {
                System.out.println("Drawing neuron " + n.getName());
                vg2d.draw(new Ellipse2D.Double(currentLayerColumn, currentLayerRow, NEURON_RADIUS * 2,
                        NEURON_RADIUS * 2));
                neuronsCoordinates.put(n, new Pair<Float, Float>(currentLayerColumn, currentLayerRow));
                Rectangle2D bounds = neuronFont.getStringBounds(n.getName(), fontRenderContext);

                vg2d.setFont(neuronFont);
                vg2d.drawString(n.getName(), currentLayerColumn + NEURON_RADIUS - (float) (bounds.getWidth() / 2),
                        currentLayerRow + NEURON_RADIUS);

                currentLayerRow += NEURON_RADIUS * 2 + NEURON_GAP;
            }
            currentLayerRow = START_Y;
            currentLayerColumn += NEURON_RADIUS * 2 + LAYER_GAP;
        }

        for (Layer l : network.getLayers()) {
            for (Neuron n : l.getNeurons()) {
                for (Synapse s : n.getOutputList()) {

                    System.out.println("Connecting neuron " + n.getName());

                    Pair<Float, Float> coordsLeft = neuronsCoordinates.get(n);
                    Pair<Float, Float> coordsRight = neuronsCoordinates.get(s.getRightNeuron());
                    double x1 = coordsLeft.getValue0();
                    double x2 = coordsRight.getValue0();
                    double y1 = coordsLeft.getValue1();
                    double y2 = coordsRight.getValue1();

                    double slope = (y2 - y1) / (x2 - x1);
                    // double angle = Math.atan(slope);
                    double angleLeft = Math.atan2(y2 - y1, x2 - x1);
                    double angleRight = Math.atan2(y1 - y2, x1 - x2);

                    double leftCircleIntersectionX = coordsLeft.getValue0() + NEURON_RADIUS
                            + Math.cos(angleLeft) * NEURON_RADIUS;
                    double leftCircleIntersectionY = coordsLeft.getValue1() + NEURON_RADIUS
                            + Math.sin(angleLeft) * NEURON_RADIUS;

                    vg2d.setPaint(Color.GRAY);
                    vg2d.draw(
                            new Line2D.Double(
                                    leftCircleIntersectionX,
                                    leftCircleIntersectionY,
                                    coordsRight.getValue0() + NEURON_RADIUS + Math.cos(angleRight) * NEURON_RADIUS,
                                    coordsRight.getValue1() + NEURON_RADIUS + Math.sin(angleRight) * NEURON_RADIUS));

                    vg2d.setPaint(Color.BLUE);
                    vg2d.setFont(weightFont);
                    vg2d.drawString(String.format("%.2f", s.getWeight()),
                            (float) (leftCircleIntersectionX + WEIGHT_SHIFT),
                            (float) (leftCircleIntersectionY + WEIGHT_SHIFT * slope));
                    vg2d.setPaint(Color.BLACK);

                }
            }
        }

        PDFProcessor pdfProcessor = new PDFProcessor(true);
        Document doc = pdfProcessor.getDocument(commands, PageSize.A4.getLandscape());
        doc.writeTo(new FileOutputStream(filename));
    }
}
