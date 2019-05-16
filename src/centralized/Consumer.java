package centralized;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import processor.DetectFace;

public class Consumer implements  Runnable {
    private Buffer buffer;
    private Semaphore free;
    private Semaphore block;
    private Image img;
    private ArrayList<Long> times = new ArrayList<Long>();
    private String outputPath = "src/output/centralized/";

    public Consumer(Buffer buffer, Semaphore free, Semaphore block) {
//        System.out.println("{ Consumer - Builded }");
        this.buffer = buffer;
        this.free = free;
        this.block = block;
    }

    private static Mat convertToMat(BufferedImage buffImg) {
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

    private static BufferedImage convertoToBufferedImage(Mat image) {

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


    @Override
    public void run() {
//        System.out.println("{ Consumer - Running }");

        DetectFace proc = new DetectFace();

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(this.outputPath + "log/pti_tpi.txt", true));

            while(true) {
                try {
    //                System.out.println("{ Consumer : Block }");
                    block.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
                }

                this.img = buffer.remove();
                if (this.img.isEnd()){
//                    System.out.println("{ Consumer - Done }");
                    break;
                }

                free.release();
    //            System.out.println("{ Consumer : Free }");

//                System.out.println(" - Image '" + this.img.getFile_name() + "'...");
                Mat imgMat = this.convertToMat(this.img.getImg());
                long begin = System.currentTimeMillis();
                Mat imgOut = proc.detection(imgMat);
//                System.out.println(String.format("Detected %s faces in %s", proc.getNumberOfFaces(), this.img.getFile_name()));
//
//                System.out.println( "Processing Time per Image" );
//                System.out.println( this.times.get(this.times.size() - 1) + " Milliseconds");
                long end = System.currentTimeMillis();
                this.times.add(end-begin);

                String toWrite = '"' + this.img.getFile_name() + '"' + ';' + proc.getNumberOfFaces() + ';' +
                        String.valueOf(this.times.get(this.times.size() - 1));
                System.out.println(toWrite);
                writer.write(toWrite);
                writer.newLine();

                try {
                    File f = new File(this.outputPath + "images/" + this.img.getFile_name() + ".jpg");
                    ImageIO.write(this.convertoToBufferedImage(imgOut), "jpg", f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
