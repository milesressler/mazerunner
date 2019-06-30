package us.milessmiles.sparkmaze

class Coordinate {
    int row
    int col
    int value
    private Integer manhattanDistanceToEnd
    int mineCount = 0


    List<Feature> directionsFromStart = []
    Map<Integer, List<Feature>> eligiblePathsFromEnd = null

    def manhattanDistanceToEnd(Coordinate end) {
        return manhattanDistanceToEnd ?: Math.abs(row - end.row) + Math.abs(col - end.col)
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
    public String toString() {
        return "Coordinate{" +
                "row=" + row +
                ", col=" + col +
                ", directionsFromStart=" + directionsFromStart +
                '}';
    }
}
