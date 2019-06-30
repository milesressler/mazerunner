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
 * The algorithm runs on maze, then increases the cost of a mine until a solution where numberOfMines is less than the number of lives
 */
class MazeRunner {
    // Configuration
    Integer numberOfLives

    // Runtime parameter
    Integer mineCost = 0

    // Data stores
    Integer[][] maze
    Coordinate start
    Coordinate end

    PriorityQueue<Coordinate> coordinatePriorityQueue
    Set<Coordinate> solvedCoordinates = []

    static void main(String[] args) {
        // TODO consider threading for performance improvements?
        // TODO allow user to pass in file location, thread max, number of lives

        def fileLocation = null
        def numberOfLives = 3
        def mazeFile = fileLocation ? new File(fileLocation) : new File(getClass().getResource('/mazes-tests.txt').toURI())

        mazeFile.eachLine { content, lineNumber ->
            def mazeRunner = new MazeRunner(content, numberOfLives)

            println "\nDrawing Maze #${lineNumber}"
            mazeRunner.draw()

            println "Solving..."
            def solution = mazeRunner.solve()
            def solutionString = solution.collect { it.name().toLowerCase()  }.join("','")
            println ("['" + solutionString + "']")
        }

        println("\nDONE")
    }

    MazeRunner(String mazeString, Integer numberOfLives) {
        this.buildMaze(mazeString)
        this.numberOfLives = numberOfLives
        coordinatePriorityQueue = new PriorityQueue<Coordinate>( { Coordinate o1, Coordinate o2 ->
            int estimatedCost1 = o1.directionsFromStart.size() + o1.manhattanDistanceToEnd(end) + ((o1.value & MINE.val) ? mineCost : 0)
            int estimatedCost2 = o2.directionsFromStart.size() + o2.manhattanDistanceToEnd(end) + ((o2.value & MINE.val) ? mineCost : 0)
            return estimatedCost1 < estimatedCost2 ? -1 : estimatedCost1 > estimatedCost2 ? 1 : 0
        })
    }

    void buildMaze(String mazeString) {
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
        while (true) {
            coordinatePriorityQueue.clear()
            solvedCoordinates.clear()
            coordinatePriorityQueue.add(start)

            def result = executeSearch()
            if (result.mineCount >= numberOfLives) {
                mineCost += 1
            } else {
                println("Solution found with ${result.mineCount} mines, length is ${result.directionsFromStart.size()}")
                println("Checked ${solvedCoordinates.size()}/${maze.length * maze[0].length} cells")
                return result.directionsFromStart
            }
        }
    }

    private Coordinate executeSearch() {
        while (true) {
            def next = coordinatePriorityQueue.poll()
            def result = solveCoordinate(next)
            if (result != null) {
                return result
            }
        }
    }

    Coordinate solveCoordinate(Coordinate coordinate) {
        if (coordinate == end) {
            return coordinate
        } else if (coordinate.value & MINE.val) {
            coordinate.mineCount += 1
        }

        Set<Feature> openDirections = directionOptions().findAll { (coordinate.value & it.val) }
        openDirections.each {
            def newCoordinate = it.moveCoordinate(coordinate)
            newCoordinate.value = maze[newCoordinate.row][newCoordinate.col]
            newCoordinate.mineCount = coordinate.mineCount

            if (!solvedCoordinates.contains(newCoordinate)) {
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
