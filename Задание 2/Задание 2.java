import java.util.Scanner;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(2);
        Scanner scan = new Scanner(System.in);

        int input1 = Integer.parseInt(scan.nextLine());
        Future<?> ftr = es.submit(new MyRunnable(input1));

        while (true) {
            String input2 = scan.nextLine();
            if (!ftr.isDone()) {
                es.submit(new MyRunnable(Integer.parseInt(input2)));
            }
             break;
        }

        es.shutdown();
    }
}

class MyRunnable implements Runnable {
    Integer number;
    public MyRunnable(int number) {
        this.number = number;
    }

    @Override
    public void run() {
        try {
            String threadName = Thread.currentThread().getName();

            System.out.println("["+threadName+"] Waiting for response... [Math.pow("+number+",2)]");

            long random = (long)(Math.random() * 4000) + 1000;
            double result = Math.pow(number, 2);

            Thread.sleep(random);

            System.out.println(
                "["+threadName+"]" +
                " Square of the number " + number + " is: " + result +
                " [response time: " + random + "ms]"
            );
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
}
