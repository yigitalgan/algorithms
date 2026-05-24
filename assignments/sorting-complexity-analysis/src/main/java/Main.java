import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import java.io.*;
import java.util.*;

public class Main {

    
    static final int[] SIZES = {500, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 250000};

    
    static final int NUM_RUNS = 10;

    public static void main(String[] args) throws IOException {

        String datasetPath = args.length > 0 ? args[0] : "data/all_stocks_5yr.csv";
        System.out.println("Loading dataset from: " + datasetPath);
        int[] rawData = readVolumeColumn(datasetPath, 250000);
        System.out.println("Loaded record count: " + rawData.length);

        double[][][] timings = new double[5][SIZES.length][3];
        String[] names = {"QuickSort", "InsertionSort", "MergeSort", "ShellSort", "RadixSort"};

        System.out.println("\n>>> Rastgele veri uzerinde testler");
        for (int si = 0; si < SIZES.length; si++) {
            int sz = SIZES[si];
            int[] baseArr = Arrays.copyOf(rawData, sz);

            for (int ai = 0; ai < 5; ai++) {
                double elapsed = 0;
                for (int run = 0; run < NUM_RUNS; run++) {
                    int[] workArr = baseArr.clone();
                    long t0 = System.currentTimeMillis();
                    dispatch(ai, workArr);
                    long t1 = System.currentTimeMillis();
                    elapsed += (t1 - t0);
                }
                timings[ai][si][0] = elapsed / NUM_RUNS;
                System.out.printf("  %-14s n=%-7d avg=%.1f ms%n",
                        names[ai], sz, timings[ai][si][0]);
            }
        }

        System.out.println("\n>>> Sirali veri uzerinde testler");
        for (int si = 0; si < SIZES.length; si++) {
            int sz = SIZES[si];
            int[] ascArr = Arrays.copyOf(rawData, sz);
            Arrays.sort(ascArr);

            for (int ai = 0; ai < 5; ai++) {
                double elapsed = 0;
                for (int run = 0; run < NUM_RUNS; run++) {
                    int[] workArr = ascArr.clone();
                    long t0 = System.currentTimeMillis();
                    dispatch(ai, workArr);
                    long t1 = System.currentTimeMillis();
                    elapsed += (t1 - t0);
                }
                timings[ai][si][1] = elapsed / NUM_RUNS;
                System.out.printf("  %-14s n=%-7d avg=%.1f ms%n",
                        names[ai], sz, timings[ai][si][1]);
            }
        }

        System.out.println("\n>>> Ters sirali veri uzerinde testler");
        for (int si = 0; si < SIZES.length; si++) {
            int sz = SIZES[si];
            int[] descArr = Arrays.copyOf(rawData, sz);
            Arrays.sort(descArr);
            reverseArray(descArr);

            for (int ai = 0; ai < 5; ai++) {
                double elapsed = 0;
                for (int run = 0; run < NUM_RUNS; run++) {
                    int[] workArr = descArr.clone();
                    long t0 = System.currentTimeMillis();
                    dispatch(ai, workArr);
                    long t1 = System.currentTimeMillis();
                    elapsed += (t1 - t0);
                }
                timings[ai][si][2] = elapsed / NUM_RUNS;
                System.out.printf("  %-14s n=%-7d avg=%.1f ms%n",
                        names[ai], sz, timings[ai][si][2]);
            }
        }

        printTable(names, timings);
        generateCharts(names, timings);
        System.out.println("\nBitti. PNG dosyalari olusturuldu.");
    }

    static void dispatch(int idx, int[] arr) {
        if      (idx == 0) quickSort(arr, 0, arr.length - 1);
        else if (idx == 1) insertionSort(arr);
        else if (idx == 2) mergeSort(arr);
        else if (idx == 3) shellSort(arr);
        else               radixSort(arr);
    }

    static void reverseArray(int[] arr) {
        int lo = 0, hi = arr.length - 1;
        while (lo < hi) {
            int tmp = arr[lo];
            arr[lo] = arr[hi];
            arr[hi] = tmp;
            lo++;
            hi--;
        }
    }

    public static void quickSort(int[] arr, int lo, int hi) {
        if (lo >= hi) return;

        int[] stk = new int[hi - lo + 1];
        int top = -1;

        stk[++top] = lo;
        stk[++top] = hi;

        while (top >= 0) {
            hi = stk[top--];
            lo = stk[top--];

            int pIdx = partitionArr(arr, lo, hi);

            if (pIdx - 1 > lo) {
                stk[++top] = lo;
                stk[++top] = pIdx - 1;
            }

            if (pIdx + 1 < hi) {
                stk[++top] = pIdx + 1;
                stk[++top] = hi;
            }
        }
    }

    private static int partitionArr(int[] arr, int lo, int hi) {
        int pivotVal = arr[hi];
        int wall = lo - 1;

        for (int cur = lo; cur < hi; cur++) {
            if (arr[cur] <= pivotVal) {
                wall++;
                int tmp = arr[wall]; arr[wall] = arr[cur]; arr[cur] = tmp;
            }
        }

        int tmp = arr[wall + 1]; arr[wall + 1] = arr[hi]; arr[hi] = tmp;
        return wall + 1;
    }


    public static void insertionSort(int[] arr) {
        for (int pos = 1; pos < arr.length; pos++) {
            int cur = arr[pos];
            int back = pos - 1;

            while (back >= 0 && arr[back] > cur) {
                arr[back + 1] = arr[back];
                back--;
            }
            arr[back + 1] = cur;
        }
    }

    public static void mergeSort(int[] arr) {
        int len = arr.length;
        int[] buf = new int[len]; 

        for (int width = 1; width < len; width *= 2) {
            for (int lft = 0; lft < len - 1; lft += 2 * width) {
                int mid = Math.min(lft + width - 1, len - 1);
                int rgt = Math.min(lft + 2 * width - 1, len - 1);
                mergeParts(arr, buf, lft, mid, rgt);
            }
        }
    }

    private static void mergeParts(int[] arr, int[] buf, int lft, int mid, int rgt) {
        int p = lft;    
        int q = mid + 1;  
        int w = lft;     

        while (p <= mid && q <= rgt) {
            if (arr[p] <= arr[q]) buf[w++] = arr[p++];
            else                  buf[w++] = arr[q++];
        }
        while (p <= mid)  buf[w++] = arr[p++];
        while (q <= rgt)  buf[w++] = arr[q++];

        for (int x = lft; x <= rgt; x++) arr[x] = buf[x];
    }

    public static void shellSort(int[] arr) {
        int len = arr.length;

        int gap = 1;
        while (gap < len / 3) gap = 3 * gap + 1;

        while (gap >= 1) {

            for (int i = gap; i < len; i++) {
                int j = i;
                while (j >= gap && arr[j] < arr[j - gap]) {
                    int tmp = arr[j]; arr[j] = arr[j - gap]; arr[j - gap] = tmp;
                    j -= gap;
                }
            }
            gap /= 3;
        }
    }

    public static void radixSort(int[] arr) {
        int biggest = arr[0];
        for (int v : arr) if (v > biggest) biggest = v;

        int numDigits = String.valueOf(biggest).length();

        for (int d = 1; d <= numDigits; d++) {
            countSortByDigit(arr, d);
        }
    }

    private static void countSortByDigit(int[] arr, int digitPos) {
        int n = arr.length;
        int[] out = new int[n];
        int[] freq = new int[10]; 

        int div = (int) Math.pow(10, digitPos - 1);

        for (int i = 0; i < n; i++) {
            int d = (arr[i] / div) % 10;
            freq[d]++;
        }

        for (int i = 1; i < 10; i++) freq[i] += freq[i - 1];

        for (int i = n - 1; i >= 0; i--) {
            int d = (arr[i] / div) % 10;
            out[--freq[d]] = arr[i];
        }
        System.arraycopy(out, 0, arr, 0, n);
    }

    static int[] readVolumeColumn(String path, int limit) throws IOException {
        List<Integer> vals = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            reader.readLine(); 
            String row;
            while ((row = reader.readLine()) != null && vals.size() < limit) {
                String[] cols = row.split(",");
                if (cols.length < 6) continue;
                try {
                    vals.add(Integer.parseInt(cols[5].trim()));
                } catch (NumberFormatException e) {
                    
                }
            }
        }
        int[] result = new int[vals.size()];
        for (int i = 0; i < result.length; i++) result[i] = vals.get(i);
        return result;
    }

    static void printTable(String[] names, double[][][] timings) {
        String[] labels = {"Random", "Sorted", "Reverse"};
        for (int t = 0; t < 3; t++) {
            System.out.println("\n--- " + labels[t] + " Input ---");
            System.out.printf("%-16s", "Algorithm");
            for (int s : SIZES) System.out.printf("%9d", s);
            System.out.println();
            for (int a = 0; a < 5; a++) {
                System.out.printf("%-16s", names[a]);
                for (int s = 0; s < SIZES.length; s++)
                    System.out.printf("%9.1f", timings[a][s][t]);
                System.out.println();
            }
        }
    }

    static void generateCharts(String[] names, double[][][] timings) throws IOException {
        String[] labels = {"Random", "Sorted", "ReverseSorted"};

        for (int t = 0; t < 3; t++) {
            XYChart chart = new XYChartBuilder()
                    .width(900).height(600)
                    .title("Tests on " + labels[t] + " Data")
                    .xAxisTitle("Input Size")
                    .yAxisTitle("Time in Milliseconds")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
            chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

            double[] xVals = new double[SIZES.length];
            for (int i = 0; i < SIZES.length; i++) xVals[i] = SIZES[i];

            for (int a = 0; a < 5; a++) {
                double[] yVals = new double[SIZES.length];
                for (int s = 0; s < SIZES.length; s++) yVals[s] = timings[a][s][t];
                chart.addSeries(names[a], xVals, yVals);
            }

            File outputDir = new File("outputs");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            String fname = "outputs/Tests_on_" + labels[t] + "_Data";
            BitmapEncoder.saveBitmap(chart, fname, BitmapEncoder.BitmapFormat.PNG);
            System.out.println("Chart saved: " + fname + ".png");
        }
    }
}
