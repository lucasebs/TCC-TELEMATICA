package distributed.server;

import processor.ProcessadorImagens;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ImageReceiverServer implements Runnable {

    private Socket sock;

    public ImageReceiverServer(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {

        ProcessadorImagens proc = new ProcessadorImagens();

        try {

            while (true) {
                DataInputStream input = new DataInputStream(sock.getInputStream());

                long stream_size = input.readLong();

                if (stream_size == 0) {
                    System.out.println("No more transmissions...");
                    break;
                }
//                System.out.println("Esperando receber imagem de " + stream_size + " bytes");

                String nome = input.readUTF();

                System.out.println("Receiving file: " + nome);

                int brilho = input.readInt();

//                System.out.println("Brilho a ser aplicado: " + brilho);

                nome = nome + "_processed";

                byte[] stream = new byte[16 * 1024];
                int count = 0;
                int lidos = 0;
                ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
                while (lidos < stream_size) {
                    if (stream_size - lidos >= 16 * 1024) {
                        count = input.read(stream, 0, 16 * 1024);
                    } else {
                        count = input.read(stream, 0, (int) stream_size - lidos);
                    }
                    byte_out.write(stream, 0, count);
//                    System.out.println("Recebeu " + count + " bytes");
                    lidos += count;
                }

                //criando a imagem a partir do array de stream de bytes
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(byte_out.toByteArray()));

                long begin = System.currentTimeMillis();
                proc.brilho(img, brilho);
//                System.out.println("Imagem processada...");
                long end = System.currentTimeMillis();
                long processing_time = end - begin;
                File f2 = new File("temp.jpg");  //output file path
                ImageIO.write(img, "jpg", f2);
//                System.out.println("Imagem temp salva...");

                File f3 = new File("temp.jpg");

                //criando um input stream para ler os bytes do arquivo (não decodifica a imagem)
                InputStream in = new FileInputStream(f3);

                long length = f3.length();
                byte[] bytes = new byte[16 * 1024]; //criando o array de bytes usado para enviar os dados da imgagem
                DataOutputStream output = new DataOutputStream(this.sock.getOutputStream());
                //enviando a quantidade de bytes do arquivo
                output.writeLong(length);
                //enviadno nome do arquivo
                output.writeUTF(nome + ".jpg");
                //enviadno
                output.writeLong(processing_time);
                //enviando o arquivo pela rede
                count = 0;
                //enquanto houver bytes para enviar, obtém do arquivo e manda pela rede
                while ((count = in.read(bytes, 0, bytes.length)) > 0) { //count recebe a qtd de bytes lidos do arquivo para serem enviados
                    //  System.out.println("Enviou " + count + " bytes");
                    try {
                        output.write(bytes, 0, count); //envia count bytes pela rede
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Thread.sleep(2000);
            DataOutputStream output = new DataOutputStream(this.sock.getOutputStream());
            output.writeLong(0);

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ImageReceiverServer.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
