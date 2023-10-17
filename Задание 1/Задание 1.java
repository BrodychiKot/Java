import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
         // Создание массива и расчёт сумм, времени и памяти в классе Main
public class Main {
    public static void main(String[] args) throws Exception {
        int arrayLength = 10000;
        int threadCount = 10;
        int[] array = new int[arrayLength];
        int sum = 0;
        int sumThread = 0;
        int sumFork;
        Random rd = new Random();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        SumThread[] threads = new SumThread[threadCount];

        for (int i = 0; i < array.length; i++) {
            array[i] = rd.nextInt(10);
        }

        // Последовательный расчёт
        long timeSequence = System.currentTimeMillis();
        long memorySeq = Runtime.getRuntime().freeMemory();
        for (int value : array) {
            sum += value;
            Thread.sleep(1);
        }
        timeSequence = System.currentTimeMillis() - timeSequence;
        memorySeq = memorySeq - Runtime.getRuntime().freeMemory();


        // Расчёт с помошью Thread 
        long timeThread = System.currentTimeMillis();
        long memoryThr = Runtime.getRuntime().totalMemory();

        for (int i = 0; i < threadCount; i++){
            threads[i] = new SumThread(countDownLatch, Arrays
                .copyOfRange(
                    array,
                    i * arrayLength / threadCount,
                    (i + 1) * arrayLength / threadCount
                )
            );
        }

        for (int i = 0; i < threadCount; i++){
            threads[i].start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        for (int i = 0; i < threadCount; i++){
            sumThread += threads[i].getSum();
        }

        timeThread = System.currentTimeMillis() - timeThread;
        memoryThr = memoryThr - Runtime.getRuntime().freeMemory();
        
        // Расчёт с помошью ForkJoin
        ForkJoinPool fjp = new ForkJoinPool();
        SumFork task = new SumFork(array, 0, array.length);
        long timeFork = System.currentTimeMillis();
        long memoryFork = Runtime.getRuntime().freeMemory();
        sumFork = fjp.invoke(task);
        timeFork = System.currentTimeMillis() - timeFork;
        memoryFork = memoryFork - Runtime.getRuntime().freeMemory();

        System.out.println("Сумма= " + sum + ". Время: " + timeSequence + " мс. Задействовано памяти: " + memorySeq + " байт. [Последовательно]");
        System.out.println("Сумма= " + sumThread + ". Время: " + timeThread+ " мс. Задействовано памяти: " + memoryThr  + " байт. [Thread]");
        System.out.println("Сумма= " + sumFork + ". Время: " + timeFork + " мс. Задействовано памяти: " + memoryFork + " байт. [ForkJoin]");
    }
}
// Расчёт с помошью Thread cуммы 
class SumThread extends Thread{
    private final int[] array;
    private int sum = 0;
    private final CountDownLatch countDownLatch;

    SumThread(CountDownLatch countDownLatch, int[] array){
        super();
        this.array = array;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run(){
        for (int value : this.array) {
            this.sum += value;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        countDownLatch.countDown();
    }

    public int getSum(){
        return this.sum;
    }
}
// Расчёт с помошью Thread cуммы 
class SumFork extends RecursiveTask<Integer> {
    int[] array;
    int start, end;

    public SumFork(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if (end - start <= 1) {
            return array[start];
        } else {
            int mid = (start + end) / 2;

            SumFork left = new SumFork(array, start, mid);
            SumFork right = new SumFork(array, mid, end);

            left.fork();
            right.fork();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return left.join() + right.join();
        }
    }
}
