package distributed.client;



import distributed.server.ImageReceiverServer;
import org.opencv.core.*;
import processor.Converter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.opencv.imgproc.Imgproc.rectangle;

public class ImageReceiverClient implements Runnable {
    private DataInputStream input;
    private long begin;
    private String outputPath = "src/output/distributed/";
//    private ArrayList<Long> times = new ArrayList<Long>();
    private int l;
    private Socket socket;
    private Converter converter = new Converter();


    public ImageReceiverClient(Socket s, long begin, int l) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            this.input = new DataInputStream(s.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = s;
        this.begin = begin;
        this.l = l;
    }

    @Override
    public void run() {


        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputPath + "log/pti_tpi" + this.l + ".txt", true));

            while (true) {
                long stream_size = this.input.readLong();
//                System.out.println("Waiting to receive " + stream_size + " bytes");

                if (stream_size == 0) {
                    System.out.println("No more transmissions...");
                    break;
                }

                String name = this.input.readUTF();

//                System.out.println("Receiving file: " + name);

                long processing_time_per_image = this.input.readLong();
//                this.times.add(processing_time_per_image);

//                long numberOfFaces = this.input.readLong();

//                byte[] stream = new byte[16 * 1024];
//                int count;
//                int lidos = 0;
//                ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
//                while (lidos < stream_size) {
//                    count = this.input.read(stream, 0, 16 * 1024);
//                    byte_out.write(stream, 0, count);
//                    lidos += count;
//                }

                ObjectInputStream objectInput = new ObjectInputStream(this.socket.getInputStream()); //Error Line!
                Object object = objectInput.readObject();
                ArrayList<int[]> faces = (ArrayList<int[]>) object;


                //                System.out.println( "- Processing Time per Image: " +
                //                         processing_time_per_image + " Milliseconds / Milissegundos");

                //                String toWrite = '"' + name + '"' + ';'+ String.valueOf(numberOfFaces)
                //                                    + ';'+ String.valueOf(processing_time_per_image);


                String toWrite = '"' + name + '"' + ';' + String.valueOf(processing_time_per_image);
                System.out.println(toWrite);
                writer.write(toWrite);
                writer.newLine();

                //criando a imagem a partir do array de stream de bytes

                File f1 = new File("src/input/samples/" + name + ".jpg");
                InputStream in = new FileInputStream(f1);
                BufferedImage img = ImageIO.read(in);
                Mat imgMat = this.converter.toMat(img);

                for (int[] face : faces) {
                    int x = face[0];
                    int y = face[1];
                    int width = face[2];
                    int height = face[3];
                    rectangle(imgMat, new Point(x, y), new Point(x + width, y + height), new Scalar(0, 255, 0));
                }

                File f2 = new File(this.outputPath + "images/" + name + "_p.jpg");
                ImageIO.write(converter.toBufferedImage(imgMat), "jpg", f2);
//                ImageIO.write(img, "jpg", f2);

            }

            long end = System.currentTimeMillis();
            long total_processing_time = (end - this.begin);
            System.out.println("\n- Total Processing Time / Tempo de Processamento Total");
            System.out.println("- " + (total_processing_time) + " Milliseconds / Milissegundos");

            writer.close();

            BufferedWriter writer_tpt = new BufferedWriter(new FileWriter(this.outputPath + "log/tpt" + this.l + ".txt", true));
            writer_tpt.write(String.valueOf(total_processing_time));
            writer_tpt.newLine();
            writer_tpt.close();

        } catch (IOException e) {
            Logger.getLogger(ImageReceiverServer.class.getName()).log(Level.SEVERE, null, l);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
