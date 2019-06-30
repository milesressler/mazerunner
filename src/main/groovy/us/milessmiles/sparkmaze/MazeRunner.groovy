package us.milessmiles.sparkmaze

/**
 * Maze Runner
 *
 * Solves a maze by getting shortest route with no more than 2 mine explosions
 *
 * Assumes all data in txt is in correct format, and provided size matches the list length, and maze is square
 */
class MazeRunner {

    Integer[][] maze

    static final UP = 1
    static final RIGHT = 2
    static final DOWN = 4
    static final LEFT = 8
    static final START = 16
    static final END = 32
    static final MINE = 64

    static void main(String[] args) {
        // TODO consider threading for performance
        // TODO allow user to pass in file location and thread max

        def fileLocation = null
        def mazeFile = fileLocation ? new File(fileLocation) : new File(getClass().getResource('/mazes.txt').toURI())

        mazeFile.eachLine { content, lineNumber ->
            def mazeRunner = new MazeRunner(content)

            println "\nDrawing Maze #${lineNumber}"
            mazeRunner.draw()
        }
    }

    MazeRunner(String mazeString) {
        this.buildMaze(mazeString)
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
                .eachWithIndex { String entry, int i ->
                    def row = i.intdiv(sizes[1])
                    def col = i % sizes[1]
                    maze[row][col] = entry.toInteger()
                }
    }

    String solve() {
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
        print((DOWN & val) ? "+   " : "+---")
    }

    static void printCell(int val) {
        print((LEFT & val) ? " " : "|")
        print " "
        print((START & val) ? "S" : (END & val) ? "E" : (MINE & val) ? "*" : " ")
        print " "
    }
}
