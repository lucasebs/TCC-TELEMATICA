package distributed.client;



import distributed.server.ImageReceiverServer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageReceiverClient implements Runnable {
    private DataInputStream input;
    private long begin;
    private String outputPath = "src/output/distributed/";
    private ArrayList<Long> times = new ArrayList<Long>();


    public ImageReceiverClient(DataInputStream input, long begin)
    {
        this.input = input;
        this.begin = begin;
    }

    @Override
    public void run() {
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputPath + "log/pti_tpi3.txt", true));
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
                this.times.add(processing_time_per_image);

//                long numberOfFaces = this.input.readLong();

                byte[] stream = new byte[16 * 1024];
                int count;
                int lidos = 0;
                ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
                while (lidos < stream_size) {
                    count = this.input.read(stream, 0, 16 * 1024);
                    byte_out.write(stream, 0, count);
                    lidos += count;
                }
//                System.out.println( "- Processing Time per Image: " +
//                         processing_time_per_image + " Milliseconds / Milissegundos");

//                String toWrite = '"' + name + '"' + ';'+ String.valueOf(numberOfFaces)
//                                    + ';'+ String.valueOf(processing_time_per_image);
                String toWrite = '"' + name + '"' + ';' + String.valueOf(processing_time_per_image);
                System.out.println(toWrite);
                writer.write(toWrite);
                writer.newLine();

                //criando a imagem a partir do array de stream de bytes
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(byte_out.toByteArray()));

                File f2 = new File(this.outputPath + "images/" + name);
                ImageIO.write(img, "jpg", f2);
            }
            long end = System.currentTimeMillis();
            long total_processing_time = (end-this.begin);
            System.out.println("\n- Total Processing Time / Tempo de Processamento Total");
            System.out.println("- " + (total_processing_time) + " Milliseconds / Milissegundos");

            writer.close();

            BufferedWriter writer_tpt = new BufferedWriter(new FileWriter(this.outputPath + "log/tpt3.txt", true));
            writer_tpt.write(String.valueOf(total_processing_time));
            writer_tpt.newLine();
            writer_tpt.close();

        } catch (IOException ex) {
            Logger.getLogger(ImageReceiverServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
