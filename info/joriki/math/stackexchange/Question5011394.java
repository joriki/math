package info.joriki.math.stackexchange;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.Random;

public class Question5011394 {
    final static int CONE = 0;
    final static int CYLINDER = 1;
    
    final static String [] names = {"cone","cylinder"};
    
    final static long ntrials = 100000000000L;
    final static int nthreads = 14;
    final static int nbins = 1000;
    
    final static double [] bmin = {4.9,5};
    final static double [] bmax = {5,5.15};
    
    final static double binMin = 4.7;
    final static double binMax = 5.3;
    
    static class CalculationWorker implements Callable<Result> {
        private final long startTrial;
        private final long endTrial;
        private final AtomicLong globalProgress;
        private final long totalTrials;
        
        public CalculationWorker(long startTrial, long endTrial, AtomicLong globalProgress, long totalTrials) {
            this.startTrial = startTrial;
            this.endTrial = endTrial;
            this.globalProgress = globalProgress;
            this.totalTrials = totalTrials;
        }
        
        public Result call() {
            Random rng = ThreadLocalRandom.current ();
            long [] totals = new long [2];
            long [] counts = new long [2];
            long [] [] minBins = new long [2] [nbins];
            long [] [] maxBins = new long [2] [nbins];
            double [] [] t = new double [2] [3];
            
            for (long n = startTrial; n < endTrial; n++) {
                if (n % 1000000 == 0) {
                    long progress = globalProgress.addAndGet(1000000);
                    if (progress % (totalTrials / 100) < 1000000) {
                        int percentage = (int)(progress * 100 / totalTrials);
                        System.out.println(percentage + "%");
                    }
                }
                
                double theta1 = rng.nextDouble() * 2 * Math.PI;
                double theta2 = rng.nextDouble() * 2 * Math.PI;
                
                for (int i = 0;i < 3;i++)
                    t [CONE] [i] = Math.sqrt (t [CYLINDER] [i] = rng.nextDouble());

                for (int j = 0;j < 2;j++) {
                    double t0 = t [j] [0];
                    double t1 = t [j] [1];
                    double t2 = t [j] [2];
                    
                    double s = (t0 - t1) * (t0 - t2);
                    double r = 
                            j == CONE
                                    ? (t0 - t1 * Math.cos(theta1)) * (t0 - t2 * Math.cos(theta2)) + t1 * t2 * Math.sin(theta1) * Math.sin(theta2)
                                    : (1 - Math.cos(theta1)) * (1 - Math.cos(theta2)) + Math.sin(theta1) * Math.sin(theta2);
                    double b = -r / s;
                    
                    int index = (int)((b - binMin) / (binMax - binMin) * nbins);
                    if (index < 0)
                        index = 0;
                    else if (index >= nbins)
                        index = nbins - 1;
                    (s < 0 ? minBins : maxBins) [j] [index]++;
                
                    if ((bmin [j] - b) * (bmax [j] - b) < 0) {
                        totals [j]++;
                        if (s > 0)
                            counts [j]++;
                    }
                }
            }
            return new Result(totals, counts, minBins, maxBins);
        }
    }
    
    static class Result {
        long [] totals;
        long [] counts;
        long [] [] minBins;
        long [] [] maxBins;
        
        Result () {
            this (new long [2], new long [2], new long [2] [nbins], new long [2] [nbins]);
        }
        
        Result(long [] totals, long [] counts, long [] [] minBins,long [] [] maxBins) {
            this.totals = totals;
            this.counts = counts;
            this.minBins = minBins;
            this.maxBins = maxBins;
        }
        
        public void add(Result other) {
            for (int i = 0;i < 2;i++) {
                totals [i] += other.totals [i];
                counts [i] += other.counts [i];
                for (int j = 0;j < nbins;j++) {
                    minBins [i] [j] += other.minBins [i] [j];
                    maxBins [i] [j] += other.maxBins [i] [j];
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        ExecutorService executor = Executors.newFixedThreadPool(nthreads);
        List<Future<Result>> futures = new ArrayList<>();
        AtomicLong globalProgress = new AtomicLong(0);
        
        long trialsPerThread = ntrials / nthreads;
        
        for (int i = 0; i < nthreads; i++) {
            long startTrial = i * trialsPerThread;
            long endTrial = (i == nthreads - 1) ? ntrials : (i + 1) * trialsPerThread;
            
            futures.add(executor.submit(new CalculationWorker(startTrial, endTrial, globalProgress, ntrials)));
        }
        
        Result result = new Result();
        
        for (Future<Result> future : futures)
            result.add (future.get());
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        for (int j = 0;j < 2;j++)
            try (PrintStream out = new PrintStream (names [j] + ".dat")) {
                System.out.println(names [j] + ":");
                System.out.println("proportion: " + result.counts [j] / (double) ntrials);
                System.out.println(result.counts [j] / (double) result.totals [j] + " ± " + 2 / Math.sqrt(result.totals [j]));

                long nobtuse = LongStream.of (result.maxBins [j]).sum ();
                for (int i = 0;i < nbins - 1;i++) {
                    nobtuse += result.minBins [j] [i] - result.maxBins [j] [i];
                    double b = binMin + (binMax - binMin) * i / nbins;
                    out.println(b + " " + (1 - 3 * nobtuse / (double) ntrials));
                }
            }
    }
}