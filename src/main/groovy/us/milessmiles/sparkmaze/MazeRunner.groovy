package us.milessmiles.sparkmaze

import static us.milessmiles.sparkmaze.Feature.*

/**
 * Maze Runner
 *
 * Solves a maze by getting shortest route with no more than 2 mine explosions
 *
 * Assumes all data in txt is in correct format, provided size matches the list length, maze is square with all sides present, and there is a solution
 *
 * I chose to use the A* algorithm as a starting point because it is the industry standard for maze solving for imperfect mazes.
 * The algorithm runs on maze, then increases the cost of exploding a mine until a solution where numberOfMines crossed is less than the number of lives
 */
class MazeRunner {
    // Configuration
    static Map<String, String> parsedArgs = [:]
    static boolean LOG_ENABLED

    // Data stores
    Integer[][] maze
    Coordinate start
    Coordinate end
    Long mineAversionFactor = 0
    Integer numberOfLives

    PriorityQueue<Coordinate> coordinatePriorityQueue
    Set<Coordinate> solvedCoordinates = []

    static void main(String[] args) {
        // TODO consider threading for performance improvements?

        // Parse out the arguments passed in
        args.each {
            def arg = it.split("=")
            parsedArgs[arg[0]] = arg[1]
        }

        Integer numberOfLives = parsedArgs['lives']?.toInteger() ?: 3
        LOG_ENABLED = Boolean.valueOf(parsedArgs['log'])
        def mazeFile = parsedArgs['file'] ? new File(parsedArgs['file']) : new File(getClass().getResource('/mazes.txt').toURI())

        // Solve each maze (they are one per line in file)
        mazeFile.eachLine { content, lineNumber ->
            def mazeRunner = new MazeRunner(content, numberOfLives)

            if (LOG_ENABLED) {
                println "\nDrawing Maze #${lineNumber}"
                mazeRunner.draw()
            }

            def solution = mazeRunner.solve()
            def solutionString = solution.collect { it.name().toLowerCase() }.join("','")
            println("['" + solutionString + "']")
        }
    }

    MazeRunner(String mazeString, Integer numberOfLives) {
        this.buildMaze(mazeString)
        this.numberOfLives = numberOfLives
        coordinatePriorityQueue = new PriorityQueue<Coordinate>( { Coordinate o1, Coordinate o2 ->
            return o1.getCost(mineAversionFactor) <=> o2.getCost(mineAversionFactor)
        })
    }

    private void buildMaze(String mazeString) {
        def sizes = mazeString
                .substring(1, mazeString.indexOf("-") - 1)
                .split(",")
                .collect { it.toInteger() }

        maze = new Integer[sizes[0]][sizes[1]]

        // This takes the list of values, splits them by comma, then adds them into the maze data store
        mazeString
            .substring(mazeString.indexOf("-") + 2, mazeString.length() - 1)
            .split(",")
            .eachWithIndex { String entryString, int i ->
                def row = i.intdiv(sizes[1])
                def col = i % sizes[1]

                def entry = entryString.toInteger()
                maze[row][col] = entry
                if (entry & START.val) {
                    start = new Coordinate(row: row, col: col, value: entry)
                } else if (entry & END.val) {
                    end = new Coordinate(row: row, col: col, value: entry)
                }
            }
    }

    List<Feature> solve() {
        // The "aversionFactor" is a value that gives the algorithm a cost to associate with taking a path through a mine;
        // Too low will give a result that hits too many mines;  Too high means an optimum path can't be guaranteed -
        // so, we start at 0 increase/decrease until we find the first value that gives us a passing result

        Long lastFailedAversionFactor = 0
        while (true) {
            coordinatePriorityQueue.clear()
            solvedCoordinates.clear()
            coordinatePriorityQueue.add(start)

            def result = executeSearch()
            if (result.mineCount >= numberOfLives) {
                // We did not get a valid result - increase mine aversion
                lastFailedAversionFactor = mineAversionFactor
                mineAversionFactor = (mineAversionFactor == 0) ? 1 : mineAversionFactor * 2
                if (LOG_ENABLED) {
                    println "lastFailedAversionFactor=${lastFailedAversionFactor}; mineAversionFactor increased to ${mineAversionFactor}"
                }
            } else if (mineAversionFactor > (lastFailedAversionFactor + 1)) {
                // We got a result, but it may not be the optimal
                mineAversionFactor -= (mineAversionFactor - lastFailedAversionFactor) / 2
                if (LOG_ENABLED) {
                    println "lastFailedAversionFactor=${lastFailedAversionFactor}; mineAversionFactor decreased to ${mineAversionFactor}"
                }
            } else {
                // Valid, optimal result
                if (LOG_ENABLED) {
                    println("Solution found with ${result.mineCount} mines, length is ${result.directionsFromStart.size()}; mineAversionFactor=${mineAversionFactor}")
                    println("Checked ${solvedCoordinates.size()}/${maze.length * maze[0].length} cells")
                }
                return result.directionsFromStart
            }
        }
    }

    private Coordinate executeSearch() {
        while (true) {
            // Pull next item from queue, process, and check for a win
            def next = coordinatePriorityQueue.poll()
            def result = solveCoordinate(next)
            if (result != null) {
                return result
            }
        }
    }

    private Coordinate solveCoordinate(Coordinate coordinate) {
        if (coordinate == end) {
            // We've reached the end - return coordinate and meta data
            return coordinate
        } else if (coordinate.value & MINE.val) {
            // Coordinate is a mine - increase the count
            coordinate.mineCount += 1
        }

        // For each open side of the cell, add the instantiated coordinate to the queue if it is not already
        directionOptions()
            .findAll { (coordinate.value & it.val) }
            .each {
                def newCoordinate = it.moveCoordinate(coordinate)
                if (!solvedCoordinates.contains(newCoordinate)) {
                    newCoordinate.value = maze[newCoordinate.row][newCoordinate.col]
                    newCoordinate.mineCount = coordinate.mineCount
                    newCoordinate.manhattanDistanceToEnd = newCoordinate.manhattanDistance(end)
                    newCoordinate.directionsFromStart = coordinate.directionsFromStart.collect()
                    newCoordinate.directionsFromStart.add(it)
                    coordinatePriorityQueue.add(newCoordinate)
                }
        }

        solvedCoordinates.add(coordinate)
        return null
    }


    /**
     * Convenience method for seeing a visual representation of the maze
     */
    void draw() {
        // Print top row
        maze[0].length.times {
            printBottom(0)
        }
        println "+"

        // Go through each row
        maze.each {
            it.each {
                // Print lefts in one line
                printCell(it)
            }
            println "|"

            it.each {
                // Print bottoms on one line
                printBottom(it)
            }
            println "+"
        }

    }

    static void printBottom(int val) {
        print((DOWN.val & val) ? "+   " : "+---")
    }

    static void printCell(int val) {
        print((LEFT.val & val) ? " " : "|")
        print " "
        print((START.val & val) ? "S" : (END.val & val) ? "E" : (MINE.val & val) ? "*" : " ")
        print " "
    }
}
