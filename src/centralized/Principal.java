package centralized;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Principal {

    public static void main(String[] args) {
        ArrayList<Buffer> buffers = new ArrayList<Buffer>();
        ArrayList<Semaphore> semaphoresFree = new ArrayList<Semaphore>();
        ArrayList<Semaphore> semaphoresBlocked = new ArrayList<Semaphore>();
        ArrayList<Consumer> consumers = new ArrayList<Consumer>();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        Integer threadsQuantity = 3;
        String outputPath = "src/output/centralized/";

        for (int i=0;i<threadsQuantity;i++) {
            Semaphore free = new Semaphore(2);
            Semaphore block = new Semaphore(0);
            Buffer buffer = new Buffer();
            Consumer c = new Consumer(buffer,free,block);
            Thread tc = new Thread(c);

            semaphoresFree.add(free);
            semaphoresBlocked.add(block);
            buffers.add(buffer);
            consumers.add(c);
            threads.add(tc);
        }

        Producer p = new Producer(buffers,semaphoresFree, semaphoresBlocked);
        Thread tp = new Thread(p);

        long begin = System.currentTimeMillis();
        tp.start();
        
        for (int i=0;i<threadsQuantity;i++) {
            threads.get(i).start();
        }

        for (int i = 0; i < threadsQuantity ; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        long total_processing_time = end-begin;

        BufferedWriter writer = null;
        
        try {
            writer = new BufferedWriter(new FileWriter(outputPath + "log/tpt.txt"));
            writer.write(String.valueOf(total_processing_time));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("- Total Processing Time " + (total_processing_time) + " Milliseconds");
    }
}
