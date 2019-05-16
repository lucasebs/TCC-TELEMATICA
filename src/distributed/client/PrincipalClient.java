package distributed.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrincipalClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<Socket> sockets = new ArrayList<Socket>();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        BufferedReader f = new BufferedReader(new FileReader("src/input/servers.txt"));


        ArrayList<String> result = new ArrayList<String>();

        File folder = new File("src/input/video/");
        File[] listOfFiles = folder.listFiles();

        for (int j = 0; j < listOfFiles.length; j++) {
            result.add(listOfFiles[j].getName());
        }

        int i = 0;
        String line;
        long begin = System.currentTimeMillis();
        while ((line = f.readLine()) != null) {
            System.out.println(line);
            String[] info = line.split(",");

            Socket s = new Socket(InetAddress.getByName(info[0]), Integer.valueOf(info[1]));
            sockets.add(s);

            DataInputStream input = new DataInputStream(s.getInputStream());
//            System.out.println(listOfFiles.length);
            ImageReceiverClient imageReceiverClient = new ImageReceiverClient(input, begin);
            Thread t = new Thread(imageReceiverClient);
            t.start();
            i++;
        }
        System.out.println("Sending images...");
        int cont = 0;
//        for (int j = 0; j<=5; j++) {
        for (String r : result) {
            System.out.println("- " + r);
//            System.out.println("Enviando Imagem "+j+" para socket "+cont % sockets.size()+"...");
            Thread.sleep(1000);
            cont++;
            DataOutputStream output = new DataOutputStream(sockets.get(cont % sockets.size()).getOutputStream());

//            File f1 = new File("src/input/video/3840_RIGHT_0212"+j+".jpg");
            File f1 = new File("src/input/samples/" + r);

            InputStream in = new FileInputStream(f1);

            long length = f1.length();

            byte[] bytes = new byte[16 * 1024]; //criando o array de bytes usado para enviar os dados da imgagem

            //enviando a quantidade de bytes do arquivo
            output.writeLong(length);
            //enviando o nome do arquivo
            output.writeUTF(r.split("\\.")[0]);
            //enviando o brilho a ser aplicado
            output.writeInt(100);
            //enviando o arquivo pela rede
            int count = 0;
            //enquanto houver bytes para enviar, obtém do arquivo e manda pela rede
            while ((count = in.read(bytes, 0, bytes.length)) > 0) { //count recebe a qtd de bytes lidos do arquivo para serem enviados
//                System.out.println("Enviou " + count + " bytes");
                try {
                    output.write(bytes, 0, count); //envia count bytes pela rede
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        long end = System.currentTimeMillis();
        cont = 0;
        for (int k = 0; k<=sockets.size(); k++) {
            DataOutputStream output = new DataOutputStream(sockets.get(cont % sockets.size()).getOutputStream());
            cont++;
            output.writeLong(0);
        }

        f.close();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(PrincipalClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}

