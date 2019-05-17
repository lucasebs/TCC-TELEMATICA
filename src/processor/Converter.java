package processor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Converter {
    public static Mat toMat(BufferedImage buffImg) {
        BufferedImage convertedImg = null;

        // Convert the image to TYPE_3BYTE_BGR, if necessary
        if (buffImg.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            convertedImg = buffImg;
        } else {
            convertedImg = new BufferedImage(buffImg.getWidth(), buffImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        }

        convertedImg.getGraphics().drawImage(buffImg, 0, 0, null);

        WritableRaster raster = convertedImg.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
        byte[] pixels = data.getData();

        Mat mat = new Mat(buffImg.getHeight(), buffImg.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }

    public static BufferedImage toBufferedImage(Mat image) {

        MatOfByte matOfByte = new MatOfByte();

        // encoding to png, so that your image does not lose information like with jpeg.
        Imgcodecs.imencode(".png", image, matOfByte);

        byte[] byteArray = matOfByte.toArray();
        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }
}
