package us.milessmiles.sparkmaze

enum Feature {
    UP(1),
    RIGHT(2),
    DOWN(4),
    LEFT(8),
    START(16),
    END(32),
    MINE(64)

    Feature(int value) {
        this.val = value
    }

    static def directionOptions() {
        return [UP, RIGHT, DOWN, LEFT]
    }

    Coordinate moveCoordinate(Coordinate source) {
        switch (val) {
            case UP.val:
                return new Coordinate(row: source.row - 1, col: source.col)
            case DOWN.val:
                return new Coordinate(row: source.row + 1, col: source.col)
            case RIGHT.val:
                return new Coordinate(row: source.row, col: source.col + 1)
            case LEFT.val:
                return new Coordinate(row: source.row, col: source.col - 1)
            default:
                return source
        }
    }

    final int val
}
