import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

// Let n be the number of portals and m be the length of the track, the following is an algorithm that solves the Telepanting problem in O(m + n),
// which can be simplified to O(m) since m > 2n. Original problem found at : https://codeforces.com/problemset/problem/1552/F.
// You might see a few operations being made modulo 998 244 353. This is part of the challenge's specifications and to prevent integer overflows.

public class Telepanting {
    public static void main(String[] args) {
        int time = telepanting("input.txt");
        System.out.println("The ant will take " + time + " units of time (modulo 998 244 353) to pass the last portal.");
    }

    private static int[][] fileParser(String fileName) throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(fileName));
        int n = Integer.parseInt(reader.readLine());

        String[] sPortal;
        int[][] iPortal = new int[n][3]; // 32-bit integer is barely enough to use without too much thinking for a range of 1 - 1 000 000 000 due to its range being -2 147 483 648 - 2 147 483 647
        //maximum space taken in memory : n = 200 000 * 4bytes = 800 kB. Negligible compared to the 256 MB allowed

        for (int i = 0; i < n; i++) {
            sPortal = reader.readLine().split(" ");
            //let's spare a loop, length 3 isn't that bad...
            iPortal[i][0] = Integer.parseInt(sPortal[0]);
            iPortal[i][1] = Integer.parseInt(sPortal[1]);
            iPortal[i][2] = Integer.parseInt(sPortal[2]);
        }
        return iPortal;
    }

    private static int[] sortExits(int[][] portals) {
        byte[] flags = new byte[((portals[portals.length - 1][0]) >> 3) + 1]; //bits array the size of last portal
        // last portal can go up to m = 1 000 000 000, divided by 8 is an array of max size 125 000 000, so at most it will use 125 MB of RAM.
        // We could fit another one like that and still meet the requirements !

        //basically a compressed counting sort, so it fits in our array. Only works because each value is unique or nonexistent (fits in a single bit)
        for (int[] portal : portals) {
            //flagging the bit in the same position in the array as the exit in the world
            flags[portal[1] >> 3] |= (1 << (portal[1] & 0b00000111));
        }

        int[] sortedExits = new int[portals.length];
        //rebuilding the portals array, sorted
        int k = 0;
        for (int i = 0; i < flags.length; i++) {
            if ((flags[i] & 0b00000001) != 0) {
                sortedExits[k++] = i << 3 | 0b000;
            }
            if ((flags[i] & 0b00000010) != 0) {
                sortedExits[k++] = i << 3 | 0b001;
            }
            if ((flags[i] & 0b00000100) != 0) {
                sortedExits[k++] = i << 3 | 0b010;
            }
            if ((flags[i] & 0b00001000) != 0) {
                sortedExits[k++] = i << 3 | 0b011;
            }
            if ((flags[i] & 0b00010000) != 0) {
                sortedExits[k++] = i << 3 | 0b100;
            }
            if ((flags[i] & 0b00100000) != 0) {
                sortedExits[k++] = i << 3 | 0b101;
            }
            if ((flags[i] & 0b01000000) != 0) {
                sortedExits[k++] = i << 3 | 0b110;
            }
            if ((flags[i] & 0b10000000) != 0) {
                sortedExits[k++] = i << 3 | 0b111;
            }
            //I think it is now apparent I'm having too much fun with bitwise operations
        }
        return sortedExits;
    }

    private static HashMap<Integer, Integer> mapExitsToEntrances(int[][] portals, int[] exits) {
        HashMap<Integer, Integer> map = new HashMap<>(portals.length);
        int entrance = 0;

        // Map the position of each portal exit to the index of the next entrance
        // In practice, it is mapped to the index corresponding to the cumulative cost of the previous portal entrance
        for (int exit = 0; exit < portals.length; exit++) {
            while(portals[entrance][0] < exits[exit]) entrance++; // search next portal
            map.put(exits[exit], entrance); // record the mapping
        }
        return map;
    }

    private static int[]computeCosts(int[][] portals, HashMap<Integer, Integer> mapToNext) {
        int[] cumulativeCosts = new int[portals.length + 1];
        int[] portalCosts = new int [portals.length];
        cumulativeCosts[0] = 0;

        for (int i = 0; i < portals.length; i++){
            portalCosts[i] = portals[i][0] - portals[i][1]; //distance between entrance and exit
            portalCosts[i] = (portalCosts[i] + cumulativeCosts[i] - cumulativeCosts[mapToNext.get(portals[i][1])]) % 998244353; //total cost of portals with their entrance between this portal's exit and entrance
            cumulativeCosts[i + 1] = (cumulativeCosts[i] + portalCosts[i]) % 998244353;
        }
        return portalCosts;
    }

    private static int telepanting(String fileName) {
        int[][] portals;
        try {
            portals = fileParser(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        long start = System.nanoTime(); // start measuring time after data is parsed. Parsing isn't very interesting now, is it?
        // If you do however care for file parsing, feel free to move this line to the top of the method.

        int[] exits = sortExits(portals); // sort exits in order to map them to the closest entrance faster

        HashMap<Integer, Integer> mapToNext = mapExitsToEntrances(portals, exits); // map exits position to the next entrance's index

        int[] portalCosts = computeCosts(portals, mapToNext); // compute the individual costs of traversing each portal

        int actualCost = portals[portals.length - 1][0] + 1; // base time is the total distance to cover ignoring portals
        for (int i = 0; i < portals.length; i++) {
            if (portals[i][2] == 1) actualCost = (actualCost + portalCosts[i]) % 998244353; // add the time of portals active at the start
        }

        long end = System.nanoTime();
        DecimalFormat formatter = new DecimalFormat("#,###,###,###");
        System.out.println("elapsed time : " + formatter.format(end - start) + "ns");

        return actualCost;
    }
}
