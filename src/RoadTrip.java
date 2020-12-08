import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.PriorityQueue;

public class RoadTrip {

    private class Attraction implements Comparable<Attraction> {
        String name;
        int priority; // distance from source

        public Attraction(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        // need to write this compare to function becuase PriorityQueue requires this to compare priorities
        // and order the attractions by priority (which is distance from the source in this case)
        public int compareTo(Attraction o) {
            if (this.priority == o.priority) {
                return 0;
            }
            if (this.priority < o.priority) {
                return 1;
            }
            return -1;
        }

    }

    private class City {
        String currentName;
        City previous;

        public City(String currentName, City previous) {
            this.currentName = currentName;
            this.previous = previous;
        }
    }

    public HashMap<String, ArrayList<String[]>> graph = new HashMap<>();
    public HashMap<String, String> attractionsMap = new HashMap<>();
    HashMap<String, Integer> dist = new HashMap<>();
    HashSet<String> cities = new HashSet<>();
    HashSet<String> visited = new HashSet<>();
    Set<City> pathTrackers = new HashSet<>();
    LinkedList<String> finalRoute = new LinkedList<>();

    public RoadTrip() throws FileNotFoundException {
        buildMap();
        buildAttractionsMap();
        // start with all distances as infinite
        for (String c : cities) {
            dist.put(c, Integer.MAX_VALUE);
        }
    }

    public void buildMap() throws FileNotFoundException {
        File file = new File("src/roads.csv");
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {

            //Abilene TX,Austin TX,217,215
            String s = scanner.nextLine();
            // line = [Abilene TX, Austin Tx, 217, 215]
            String[] line = s.split(",");
            // Abeline -> {(Austin, 217, 215).....}
            // Austin -> {(Abeline, 217, 215).....}
            if (!graph.containsKey(line[0])) {
                graph.put(line[0], new ArrayList<>());
            }
            if (!graph.containsKey(line[1])) {
                graph.put(line[1], new ArrayList<>());
            }
            graph.get(line[0]).add(new String[]{line[1], line[2], line[3]});
            graph.get(line[1]).add(new String[]{line[0], line[2], line[3]});
            cities.add(line[0]);
            cities.add(line[1]);
        }

    }

    public void buildAttractionsMap() throws FileNotFoundException {
        //Use for loop to loop through csv for every row in csv, create a key value pair in the dictionary. (Strings to strings)

        File file = new File("src/attractions.csv");
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {

            //Louis Lunch,New Haven CT
            String s = scanner.nextLine();
            String[] line = s.split(",");
            // line = [Louis Lunch,New Haven CT]
            attractionsMap.put(line[0], line[1]);
            // {Louis Lunch -> New Haven CT}
        }

    }

    public String getLocationFromAttractionName(String attractionName) {
        // will return null if the attractionName isn't in the map
        return attractionsMap.get(attractionName);
    }

    // helper function to find the city with the lowest distance from the source
    public String getClosestUnvisitedNeighbor() {

        int minDistance = Integer.MAX_VALUE;
        String closestNeighbor = "";

        for (String n: cities) {
            if (!visited.contains(n) && dist.get(n) <= minDistance) {
                minDistance = dist.get(n);
                closestNeighbor = n;
            }
        }
        //mark it as visited so we don't ever return the same city twice
        visited.add(closestNeighbor);
        return closestNeighbor;
    }

    public List<String> route(String starting_city, String ending_city, List<String> attractions) {
        HashMap<String, String> paths = new HashMap<>();
        List<String> attractionLocations = new LinkedList<>();
        ArrayList<String[]> neighbors = graph.get(starting_city);

        // to keep track of paths


        // we want to calculate every single objects distance from the destination
        // we'll use these distances to prioritize which landmarks to visit first
        // we should visit the landmarks fathest away from our dest first (not closest)
        dist.put(ending_city, 0);
        City start = new City(starting_city, null);
        pathTrackers.add(start);


        // MAX VALU, abelline = 0, austin 217, dalls = 183, lubbok = 183, fresno = 10340183918324980123409182
        // visited = []
        //visit every single city, calcluate the shortest path from source to every single city in the graph
        for (String c : cities) {
            while (!visited.contains(c)) {
                //mark it as visited
                String closestUnvisitedNeighbor = getClosestUnvisitedNeighbor(); // fresno
                for (String[] neighbor : graph.get(closestUnvisitedNeighbor)) {
                    //123 + 183
                    //neighbor = dallas
                    // neighbor = ['lubbock', 'fresno, '123', 120]
                    // 123 + 183 = 206
                    // calculate distance from starting city
                    int distanceFromStart = Integer.parseInt(neighbor[1]) + dist.get(closestUnvisitedNeighbor);
                    if ( !closestUnvisitedNeighbor.equals(neighbor) && distanceFromStart < dist.get(neighbor[0])) {

                        dist.put(neighbor[0], distanceFromStart);
                        //need to keep track of path
                        City city = new City(neighbor[0], getPreviousCity(closestUnvisitedNeighbor));
                        pathTrackers.add(city);

                    }
                }
            }
        }

        // get the list of attractions prioritized by distance from the start (higher priority = closer to the source and higher in our pq)
        PriorityQueue<Attraction> pq = orderLandmarks(attractions);


//        City node = null;
//        for (City c: pathTrackers) {
//            if (c.currentName.equals(city)) {
//                node = c;
//            }
//        }
//
//        while(node.previous != null) {
//            finalRoute.addFirst(node.previous.currentName);
//            node = node.previous;
//        }
        // get path from each landmark to next landmark in order or priority
        String city = starting_city;
        boolean first = true;

        while (pq.peek() != null) {
            String city1 = pq.poll().name;
            getShortestPath(city, city1, first);
            first = false;
            city = city1;

        }
        getShortestPath(city, ending_city, first);

        finalRoute.add(ending_city);
        System.out.println(finalRoute);
        return finalRoute;
    }

    public void getShortestPath(String source, String dest, boolean first) {
        visited = new HashSet<>();
        pathTrackers = new HashSet<>();
        for (String c : cities) {
            dist.put(c, Integer.MAX_VALUE);
        }
        dist.put(source, 0);
        City start = new City(source, null);
        pathTrackers.add(start);

        for (String c : cities) {
            while (!visited.contains(c)) {
                //mark it as visited
                String closestUnvisitedNeighbor = getClosestUnvisitedNeighbor(); // fresno
                for (String[] neighbor : graph.get(closestUnvisitedNeighbor)) {
                    //123 + 183
                    //neighbor = dallas
                    // neighbor = ['lubbock', 'fresno, '123', 120]
                    // 123 + 183 = 206
                    // calculate distance from starting city
                    int distanceFromStart = Integer.parseInt(neighbor[1]) + dist.get(closestUnvisitedNeighbor);
                    if ( !closestUnvisitedNeighbor.equals(neighbor) && distanceFromStart < dist.get(neighbor[0])) {

                        dist.put(neighbor[0], distanceFromStart);
                        //need to keep track of path
                        //lubbock
                        City city = new City(neighbor[0], getPreviousCity(closestUnvisitedNeighbor));
                        pathTrackers.add(city);

                    }
                }
            }
        }

        City node = null;
        for (City c: pathTrackers) {
            if (c.currentName.equals(dest)) {
                node = c;
            }
        }
        LinkedList<String> route = new LinkedList<>();

        while(node.previous != null) {
            // if first is true, then its the first city and we need to reverse it because the path is backwards
            if (first) {
                route.addFirst(node.previous.currentName);
            } else {
                route.addFirst(node.previous.currentName);
            }
            node = node.previous;
        }
        for (String s : route) {
            finalRoute.add(s);
        }
    }

    // helper function to find City object for prev city name
    public City getPreviousCity(String cityName) {
        for (City c: pathTrackers) {
            if (c.currentName.equals(cityName)) {
                return c;
            }
        }
        return null;
    }

    //find the shortest distance from the start to every single landmark(store that, sort it by distance, then we'll know what closests landmark is)
    //make a priority queue where priority is distance from start
    public PriorityQueue<Attraction> orderLandmarks(List<String> attractions) {
        PriorityQueue<Attraction> pq = new PriorityQueue<Attraction>();
        for (String a : attractions) {
            String name = getLocationFromAttractionName(a);
            pq.add(new Attraction(name, dist.get(name)));
        }
        return pq;
    }


    public static void main(String [] args ) throws FileNotFoundException {
        RoadTrip r = new RoadTrip();
        LinkedList<String> a =  new LinkedList<String>();
        a.add("The Alamo Mission");
        a.add("Alcatraz");
        r.route("Abilene TX", "New York NY", a);
    }

}
//List<?> route(String starting_city, String ending_city,
//List<String> attractions)
//Run through direct paths given on roads.csv, fill in those direct paths in a 2 dimensional array used as a graph.



//2 dimensional array - apply floyd’s algorithm to determine the distance between i and j




