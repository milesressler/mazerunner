package us.milessmiles.sparkmaze

import static us.milessmiles.sparkmaze.Feature.*

/**
 * Maze Runner
 *
 * Solves a maze by getting shortest route with no more than 2 mine explosions
 *
 * Assumes all data in txt is in correct format, and provided size matches the list length, and maze is square
 *
 * I chose to use the A* algorithm as a starting point because it is the industry standard for maze solving for imperfect mazes.
 * The basic approach is to use a depth-first search, get the best paths that allow for 0, 1, and 2 mines, and pass that back up the stack
 */
class MazeRunner {

    Integer[][] maze
    Coordinate start
    Coordinate end
    Integer numberOfLives

    static void main(String[] args) {
        // TODO consider threading for performance
        // TODO allow user to pass in file location and thread max

        def fileLocation = null
        def numberOfLives = 3
        def mazeFile = fileLocation ? new File(fileLocation) : new File(getClass().getResource('/mazes.txt').toURI())

        mazeFile.eachLine { content, lineNumber ->
            def mazeRunner = new MazeRunner(content, numberOfLives)

            println "\nDrawing Maze #${lineNumber}"
            mazeRunner.draw()

            print "Solving..."
            def solution = mazeRunner.solve(mazeRunner.start, null)
            def solutionString = solution.collect { it.name().toLowerCase()  }.join("','")
            println "Complete"
            println ("['" + solutionString + "']")
        }

        println("\nDONE")
    }

    MazeRunner(String mazeString, Integer numberOfLives) {
        this.buildMaze(mazeString)
        this.numberOfLives = numberOfLives
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
                        start = new Coordinate(row: row, col: col)
                    } else if (entry & END.val) {
                        end = new Coordinate(row: row, col: col)
                    }
                }
    }



    List<Feature> solve(Coordinate from, Feature enteredFrom) {
        def value = maze[from.row][from.col]

        Set<Feature> openDirections = directionOptions().findAll { (value & it.val) }
        if (enteredFrom) {
            openDirections.remove(enteredFrom)
        }

        List<Coordinate> coordinates = openDirections.collect { it.moveCoordinate(from) }
        coordinates.sort { -1 * it.manhattanDistanceTo(end) }

        return [UP, DOWN]
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
