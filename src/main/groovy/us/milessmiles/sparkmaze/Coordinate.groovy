package us.milessmiles.sparkmaze

import static us.milessmiles.sparkmaze.Feature.MINE

class Coordinate {
    int row
    int col
    int value
    int manhattanDistanceToEnd
    int mineCount = 0

    List<Feature> directionsFromStart = []

    def manhattanDistance(Coordinate coord) {
        return Math.abs(row - coord.row) + Math.abs(col - coord.col)
    }

    def getCost(long mineAversionFactor) {
        return directionsFromStart.size() + manhattanDistanceToEnd + ((value & MINE.val) ? mineAversionFactor : 0)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Coordinate that = (Coordinate) o

        if (col != that.col) return false
        if (row != that.row) return false

        return true
    }

    int hashCode() {
        int result
        result = row
        result = 31 * result + col
        return result
    }


    @Override
    String toString() {
        return "Coordinate{" +
                "row=" + row +
                ", col=" + col +
                ", directionsFromStart=" + directionsFromStart +
                '}';
    }
}
