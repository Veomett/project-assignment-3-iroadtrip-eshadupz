import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class IRoadTrip2 {
    private Map<String, List<String>> adjacencyList; //keeps track of neighboring countries
    private Map<String, String> stateNameMap; //keeps track of state name data
    private Map<String, Map<String, Integer>> distanceMap = new HashMap<>(); //keeps track of countries and distances


    public IRoadTrip2(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java IRoadTrip borders.txt capdist.csv state_name.tsv");
            System.exit(1);
        }

        // Initialize data structures
        adjacencyList = new HashMap<>();
        stateNameMap = new HashMap<>();

        // Read and associate data from files
        readStateNameFile(args[2]);
        readBordersFile(args[0]);
        readCapDistFile(args[1]);

//        debugMaps();

    }

    private void readStateNameFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            // Assuming the first line is a header, you may want to skip it
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\t");

                if (parts.length >= 3) {
                    String countryId = parts[1].trim();
                    String countryName = parts[2].trim();

                    populateStateNameMap(countryId, countryName);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading state_name.tsv file: " + e.getMessage());
            System.exit(1);
        }
    }

    private void populateStateNameMap(String countryId, String countryName) {
        stateNameMap.put(countryId, countryName);
    }

    private void readBordersFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("=");

                if (parts.length >= 1) {
                    String country = parts[0].trim();
                    String[] neighbors = parts[1].split(";");

                    List<String> neighborList = new ArrayList<>();
                    for (String neighbor : neighbors) {
                        String[] neighborParts = neighbor.trim().split(" ");
                        if (neighborParts.length >= 2) {
                            String neighborCountry = neighborParts[0];
                            neighborList.add(neighborCountry);
                        }
                    }

                    adjacencyList.put(country, neighborList);
                } else {
                    System.err.println("Invalid line in borders.txt: " + line);
                }
            }

//            // Print the contents of adjacencyList for debugging
//            System.out.println("adjacencyList: " + adjacencyList);
        } catch (FileNotFoundException e) {
            System.err.println("Error reading borders.txt: " + e.getMessage());
        }
    }



    private void readCapDistFile(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            // Skip the header line
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length == 6) {
                    String countryA = parts[1].trim();
                    String countryB = parts[3].trim();
                    int distance = Integer.parseInt(parts[4].trim());

                    // Assuming distance is unique for each pair of countries
                    // If not, you might need to handle duplicates based on your requirements

                    // Associate distance with countryA
                    associateDistance(countryA, countryB, distance);

                    // Associate distance with countryB (assuming the distance is symmetric)
                    associateDistance(countryB, countryA, distance);
                } else {
                    System.err.println("Invalid line in capdist.csv: " + line);
                }
            }
        } catch (FileNotFoundException | NumberFormatException e) {
            System.err.println("Error reading capdist.csv: " + e.getMessage());
        }
    }
    private void associateDistance(String countryA, String countryB, int distance) {
        // Check if the outer map already contains countryA
        if (!distanceMap.containsKey(countryA)) {
            distanceMap.put(countryA, new HashMap<>());
        }

        // Check if the outer map already contains countryB
        if (!distanceMap.containsKey(countryB)) {
            distanceMap.put(countryB, new HashMap<>());
        }

        distanceMap.get(countryA).put(countryB, distance);
        distanceMap.get(countryB).put(countryA, distance);
    }



    //    private void debugMaps() {
//        System.out.println("State Name Map jhg \n");
//        System.out.println(stateNameMap);
//        System.out.println("Adjacency List jhg \n");
//        System.out.println(adjacencyList);
//        System.out.println("Distance Map hgf \n");
//        System.out.println(distanceMap);
//    }
    public String getCountryIdByName(String countryName) {
        // Assuming stateNameMap is populated
        for (Map.Entry<String, String> entry : stateNameMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(countryName)) {
                return entry.getKey();
            }
        }
        // If no match is found, return the original name
        return countryName;
    }


    public int getDistance(String countryNameA, String countryNameB) {
        String countryA = getCountryIdByName(countryNameA).toUpperCase();
        String countryB = getCountryIdByName(countryNameB).toUpperCase();

        // Check if the distanceMap contains the specified country pair
        if (distanceMap.containsKey(countryA) && distanceMap.get(countryA).containsKey(countryB)) {
            return distanceMap.get(countryA).get(countryB);
        } else {
            return -1;
        }
    }


    class PathEntry {
        List<String> path;
        int distance;
    }

    public PathEntry findPath(String country1, String country2) {
        Queue<PathEntry> queue = new LinkedList<>();

        // Create initial entry
        PathEntry initialEntry = new PathEntry();
        initialEntry.path = new ArrayList<>();
        initialEntry.path.add(country1);
        initialEntry.distance = 0;
        queue.add(initialEntry);

        while (!queue.isEmpty()) {

            PathEntry currentEntry = queue.poll();
            String currentCountry = currentEntry.path.get(currentEntry.path.size() - 1);

            if (currentCountry.equals(country2)) {
                return currentEntry;
            }

            for (String neighbor : adjacencyList.getOrDefault(currentCountry, new ArrayList<>())) {

                // Create entry for neighbor
                PathEntry newEntry = new PathEntry();
                newEntry.path = new ArrayList<>(currentEntry.path);
                newEntry.path.add(neighbor);

                // Compute distance
                newEntry.distance = currentEntry.distance + getDistance(currentCountry, neighbor);

                // Add to queue
                queue.add(newEntry);
            }
        }

        return null;
    }


    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter first country: ");
            String country1 = scanner.nextLine();
            if (country1.equals("quit")) {
                break;
            }

            System.out.print("Enter second country: ");
            String country2 = scanner.nextLine();

            int distance = getDistance(country1, country2);
            if (distance != -1) {
                System.out.println("Distance: " + distance);

                PathEntry result = findPath(country1, country2);

                if (result != null) {
                    List<String> path = result.path;
                    int pathDistance = result.distance;

//                    System.out.println("Path Distance: " + pathDistance);

                    System.out.print("Path: ");
                    for (String country : path) {
                        System.out.print(stateNameMap.get(country) + " -> ");
                    }
                    System.out.println();
                } else {
                    System.out.println("No path found");
                }
            } else {
                System.out.println("Distance not found for the given countries.");
            }
        }
        scanner.close();
    }



    public static void main(String[] args) {
        IRoadTrip2 a3 = new IRoadTrip2(args);

        a3.acceptUserInput();
    }

}

