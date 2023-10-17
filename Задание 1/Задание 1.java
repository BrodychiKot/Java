import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

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

        // Sequence calculation
        long timeSequence = System.currentTimeMillis();
        long memorySeq = Runtime.getRuntime().freeMemory();
        for (int value : array) {
            sum += value;
            Thread.sleep(1);
        }
        timeSequence = System.currentTimeMillis() - timeSequence;
        memorySeq = memorySeq - Runtime.getRuntime().freeMemory();


        // Thread calculation
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

        ForkJoinPool fjp = new ForkJoinPool();
        ArraySum task = new ArraySum(array, 0, array.length);

        // Fork calculation
        long timeFork = System.currentTimeMillis();
        long memoryFork = Runtime.getRuntime().freeMemory();
        sumFork = fjp.invoke(task);
        timeFork = System.currentTimeMillis() - timeFork;
        memoryFork = memoryFork - Runtime.getRuntime().freeMemory();

        System.out.println("Sum: " + sum + ". Time: " + timeSequence + ". Memory: " + memorySeq + " [Sequence]");
        System.out.println("Sum: " + sumThread + ". Time: " + timeThread+ ". Memory: " + memoryThr  + " [Thread]");
        System.out.println("Sum: " + sumFork + ". Time: " + timeFork + ". Memory: " + memoryFork + " [Fork]");
    }
}

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

class ArraySum extends RecursiveTask<Integer> {
    int[] array;
    int start, end;

    public ArraySum(int[] array, int start, int end) {
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

            ArraySum left = new ArraySum(array, start, mid);
            ArraySum right = new ArraySum(array, mid, end);

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
