package us.milessmiles.sparkmaze

class Coordinate {
    int row
    int col

    def manhattanDistanceTo(Coordinate compare) {
        return Math.abs(row - compare.row) + Math.abs(col - compare.col)
    }
}
