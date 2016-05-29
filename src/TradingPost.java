import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TradingPost {

    /**
     * Determines whether StdIn (Command line input redirection) is used or FileInputStream.
     */
    private static boolean USE_STDIN = false;

    public static void main(String[] args) {

        generateTestData();

        String[] smallFileNames = {"sample_input_size10.txt", "sample_input_size15.txt", "sample_input_size20.txt",
                                      "sample_input_size25.txt",};

        for (String fileName : smallFileNames) {

            BruteForce.testTime(fileName);
            System.out.println();
            DivideAndConquer.testTime(fileName);
            System.out.println();
        }

        String[] largeFileNames = {"sample_input_size100.txt", "sample_input_size200.txt", "sample_input_size400.txt",
                "sample_input_size600.txt", "sample_input_size800.txt"};

        for (String fileName : largeFileNames) {
            String inputStr = readInput(fileName);
            int[][] rowData = parseInput(inputStr);

            testDynProg(rowData);
            System.out.println();
        }
    }

    /**
     * Reads the input file into a string.
     *
     * @param fileName File name used for using FileInputStream.
     * @return A string with the port data to be parsed.
     */
    public static String readInput(String fileName) {
        StringBuilder sb = new StringBuilder();
        if (USE_STDIN) {
            InputStreamReader cin = new InputStreamReader(System.in);
            try {
                while (cin.ready()) {
                    sb.append((char) cin.read());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.setIn(new FileInputStream(fileName));
                while (System.in.available() > 0) {
                    sb.append((char) System.in.read());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    /**
     * Assumes the CRLF ('\r''\n') line endings are used and tabs ('\t') delimit values.
     *
     * @param inputStr The string to parse for port data.
     * @return 2-D array of port data.
     */
    public static int[][] parseInput(String inputStr) {
        int colCount = 1, rowCount = 0;

        // find number of columns
        for (int i = 0; i < inputStr.indexOf('\n'); i++)
            if (inputStr.charAt(i) == '\t') colCount++;

        int[][] rowData = new int[colCount][colCount];

        while (inputStr.length() > 0) {
            int i = 0;
            String row = inputStr.substring(0, inputStr.indexOf('\n')); // get next row to parse
            int[] rowCosts = new int[colCount];
            while (row.length() > 0) {
                // check for next tab char index, if not found, carriage return index returned.
                int charIndex = (row.indexOf('\t') > 0) ? row.indexOf('\t') : row.indexOf('\r');
                String value = row.substring(0, charIndex);

                // check for NA to add sentinel, otherwise, parse the int
                rowCosts[i] = (value.equals("NA")) ? Integer.MAX_VALUE : Integer.parseInt(value);

                i++;
                row = row.substring(charIndex + 1); // remove recently add cost section of the string
            }
            rowData[rowCount] = rowCosts;
            rowCount++;
            inputStr = inputStr.substring(inputStr.indexOf('\n') + 1); // remove parsed row from input Str
        }
        return rowData;
    }

    /**
     * Tests the Dynamic Programming approach with the rowData provided and prints the results.
     *
     * @param rowData Port data provided by parseInput.
     */
    private static void testDynProg(int[][] rowData) {
        long startTime = System.currentTimeMillis();

        int[][] costArray = dynamicProg(rowData);
        List<int[]> shortestPath = findDynProgPath(costArray);

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("Testing Dynamic Programming (Size " + costArray.length + "): ");
        System.out.println("  -  Elapsed Time (ms): " + elapsedTime);
        System.out.println("  -  Minimum Cost     : " + shortestPath.get(0)[2]);
    }

    /**
     * Calculates the minimum cost to get to each port.
     *
     * @param thePorts The port data.
     * @return A cost array the shows the cost to each port.
     */
    private static int[][] dynamicProg(int[][] thePorts) {
        int[][] costArray = new int[thePorts.length][thePorts[0].length];
        for (int k = 0; k < thePorts.length; k++) {
            costArray[k] = thePorts[k].clone();
        }

        for (int i = 0; i < costArray.length - 1; i++) {
            int currCost = 0;
            for (int j = i; j < costArray[i].length; j++) {
                if (j == i && i != 0) {
                    // find min originating port
                    int minCost = Integer.MAX_VALUE;
                    for (int k = i - 1; k >= 0; k--) {
                        if (costArray[k][j] < minCost) {
                            currCost = costArray[k][j];
                            minCost = currCost;
                        }
                    }
                }
                costArray[i][j] += currCost;
            }
        }
        return costArray;
    }

    /**
     * Finds the shortest path to the destination port given a cost array.
     *
     * @param costArray The cost array used to determine the minimum cost.
     * @return A list of coordinates and the cost at the coordinate.
     *          I.e. { 4, 6, 16 } would be interpreted as (4, 6) with cost 16.
     */
    private static List<int[]> findDynProgPath(int[][] costArray) {
        List<int[]> shortestPath = new ArrayList<>();
        int colPtr = costArray.length - 1;

        while (colPtr != 0) {
            int minOrigPort = 0;
            int minOrigCost = Integer.MAX_VALUE;

            // add path where canoe was rented if not on first iteration.
            if (colPtr < costArray.length - 1)
                shortestPath.add(new int[]{colPtr, colPtr, costArray[colPtr][colPtr]});

            for (int k = 0; k < colPtr; k++) {
                if (costArray[k][colPtr] <= minOrigCost) {
                    minOrigCost = costArray[k][colPtr];
                    minOrigPort = k;
                }
            }
            shortestPath.add(new int[]{minOrigPort, colPtr, costArray[minOrigPort][colPtr]}); // add port coordinates to path list.
            colPtr = minOrigPort; // set colPtr to new min originating port
        }

        shortestPath.add(new int[]{0, 0, 0}); // add starting port

        return shortestPath;
    }

    /**
     * Prints the cost array that was used to determine the minimum cost.
     *
     * @param costArray The cost array to print.
     */
    private static void printCostArray(int[][] costArray) {
        StringBuilder sb = new StringBuilder();
        for (int[] aCostArray : costArray) {
            sb.append("[ ");
            for (int k = 0; k < costArray[0].length; k++) {
                if (k < costArray[0].length - 1) {
                    if (aCostArray[k] != Integer.MAX_VALUE) sb.append(aCostArray[k]).append(", ");
                    else sb.append("X, ");
                } else {
                    sb.append(aCostArray[k]).append(" ");
                }
            }
            sb.append("]");
            System.out.println(sb.toString());
            sb.setLength(0);
        }
    }

    /**
     * Generates test input files using an array of sizes and array of step sizes to derive costs.
     */
    public static void generateTestData() {
        int[] testSizes = new int[]{ 10, 15, 17, 20, 25, 50, 100, 200, 400, 600, 800 };
        int[] steps = new int[]{ 1, 2, 3 };

        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        for (int inputSize : testSizes) {
            File testFile = new File("sample_input_size" + inputSize + ".txt");
            sb.setLength(0);
            int offset = 0,
                lastRandInt = 0,
                currRandInt;

            // insert test data in StringBuilder
            for (int i = 0; i < inputSize; i++) {

                for (int j = 0; j < inputSize; j++) {
                    if (j < offset) {
                        sb.append("NA");
                    } else if (j == offset){
                        sb.append(0);
                        lastRandInt = 0;
                    } else {
                        // increments by a random step size greater than the previous number.
                        currRandInt = lastRandInt + steps[rand.nextInt(steps.length)];
                        sb.append(currRandInt);
                        lastRandInt = currRandInt;
                    }
                    // use carriage return instead of tab before the line feed
                    if(j != inputSize - 1)
                        sb.append('\t');
                    else
                        sb.append('\r');
                }
                sb.append('\n');
                offset++;
            }
            // write test data to file
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile), "UTF-8"))) {
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void BruteForce(int[][] thePorts) {

    }
}