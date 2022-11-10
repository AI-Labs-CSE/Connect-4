import java.util.ArrayList;
import java.util.List;
public class State {

    long state;

    public State(long state) {
        this.state = state;
    }

    public String toString() {
        return Long.toString(state);
    }

    /***
     * @return 2 D array of the characters that represent the state of the board
     ***/
    public char[][] stateToMatrix(){
        char[][] matrix = new char[7][];
        for (int i = 0; i < 7; i++) {
            matrix[i] = getColumn(i);
        }
        return matrix;
    }
    public State[] getSuccessors(int player) {
        List<State> successors = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            State temp = new State(this.state);
            if (!temp.isColumnFull(i)) {
                temp.addToColumn(i, player);
                successors.add(temp);
            }
        }
        return successors.toArray(new State[0]);
    }

    /***
     * @param column - the column to add the piece to
     * @param value - the value to add 0 for yellow and 1 for red
     ***/
    public boolean addToColumn(int column, int value){
        if (isColumnFull(column))
            return false;
        int length = getColumnLength(column);
        long thisColumn = (state >> (column * 9 + 3));
        long newColumn = ((((long) value << length) | thisColumn) << 3) | (length + 1);

        state = state & ~(0b111111111L << (column*9));
        state = state | newColumn << (column * 9);
        return true;
    }

    /***
     * @param column - the column to get
     * @return 2d array with the start and end index on the column bits
     ***/
    public char[] getColumn(int column){
        int length = getColumnLength(column);
        long thisColumn = (state >> (column * 9L + 3)); // the plus 3 to remove the size bits
        char[] res = {'0', '0', '0', '0', '0', '0'};
        int i = 0;
        while (length>0){
            res[i] = (thisColumn & 0b1) == 0 ? 'y' : 'r';
            thisColumn = thisColumn >> 1;
            length--;
            i++;
        }
        return res;
    }

    /***
     * @param column - the column to get the number of pieces in
     * @return the number of pieces in the column
     ***/
    public int getColumnLength(int column){
        // the first 3 bits in every column represents the last place that a disk was put in
        // ex: 010011 101 here we have 5 disks the first one is red second is red third is yellow fourth is yellow firth is red
        // the last bit is zero but its an empty place because we knew from the first 3 bits that our size is 5
        // to get the size we just need to extract the first 3 bits of the column
        // to get a specific column we need to right shift by 9 * the column number we want
        return (int)(state >> (column * 9)) & 0b111;
    }

    /***
     * @param column - the column to check
     * @return true if the column is full and false if it is not
     ***/
    public boolean isColumnFull(int column){
        // since our board is 6*7 the max number of disk to be put is 6
        return getColumnLength(column) >= 0b110;
    }

    /***
     * @return true if the state is a game over state board is full
     ***/
    public boolean isFull(){
        for (int i=0; i<7; i++){
            if (!isColumnFull(i)){
                return false;
            }
        }
        return true;
    }
    public int[] getScore(){
        char[][] board = stateToMatrix();
        int[] horzScore = horizontalScore(board);
        int[] verticalScore = verticalScore(board);
        int[] positive = positiveDiagonalScore(board);
        int[] negative = negativeDiagonalScore(board);
        int yellow = horzScore[0] + verticalScore[0] + positive[0] + negative[0];
        int red = horzScore[1] + verticalScore[1] + positive[1] + negative[1];
        return new int[]{yellow, red};
    }

    private int[] negativeDiagonalScore(char[][] board){
        int yellowScore = 0;
        int redScore = 0;
        int redPieces = 0;
        int yellowPieces = 0;
        int colController = 3;
        boolean contCol = true;
        int contRow = 6;
        int i=6;
        int k=6;
        int j=2;
        int w=2;
        while (contCol || contRow >= 0) {
            yellowPieces = 0;
            redPieces = 0;
            while (i>=0 && k>=0 && j<60 && w<6) {
                if (board[i][j] == 'y') {
                    yellowPieces++;
                    if (yellowPieces == 4) {
                        yellowPieces = 3;
                        yellowScore++;
                    }
                } else {
                    yellowPieces = 0;
                }
                if (board[k][w] == 'r') {
                    redPieces++;
                    if (redPieces == 4) {
                        redPieces = 3;
                        redScore++;
                    }
                } else {
                    redPieces = 0;
                }
                i--;
                k--;
                j++;
                w++;
            }
            if (colController == 0){
                contCol = false;
                contRow--;
            }
            colController = colController > 0 ? colController - 1 : 0;
            i = contRow;
            k = contRow;
            j = colController;
            w = colController;
        }
        return new int[]{yellowScore, redScore};
    }
    private int[] positiveDiagonalScore(char[][] board){
        int yellowScore = 0;
        int redScore = 0;
        int redPieces = 0;
        int yellowPieces = 0;
        int rowController = 3;
        boolean contRow = true;
        int contCol = 0;
        int i=3;
        int k=3;
        int j=0;
        int w=0;
        while (contRow || contCol < 5) {
            yellowPieces = 0;
            redPieces = 0;
            while (i<7 && k<7 && j<6 && w<6) {
                if (board[i][j] == 'y') {
                    yellowPieces++;
                    if (yellowPieces == 4) {
                        yellowPieces = 3;
                        yellowScore++;
                    }
                } else {
                    yellowPieces = 0;
                }
                if (board[k][w] == 'r') {
                    redPieces++;
                    if (redPieces == 4) {
                        redPieces = 3;
                        redScore++;
                    }
                } else {
                    redPieces = 0;
                }
                i++;
                k++;
                j++;
                w++;
            }
            if (rowController == 0){
                contRow = false;
                contCol++;
            }
            rowController = rowController > 0 ? rowController - 1 : 0;
            i = rowController;
            k = i;
            j = contCol;
            w = contCol;
        }
        return new int[]{yellowScore, redScore};
    }
    private int[] verticalScore (char[][] board){
        int yellowScore = 0;
        int redScore = 0;
        int redPieces = 0;
        int yellowPieces = 0;
        for (int i=0, k=0; i<6 && k<6; i++, k++){
            yellowPieces = 0;
            redPieces = 0;
            for (int j=0, w=0; j<7 && w<7; j++, w++){
                if (board[j][i] == 'y'){
                    yellowPieces++;
                    if (yellowPieces == 4) {
                        yellowPieces = 3;
                        yellowScore++;
                    }
                }
                else {
                    yellowPieces = 0;
                }
                if (board[w][k] == 'r'){
                    redPieces++;
                    if (redPieces == 4){
                        redPieces = 3;
                        redScore++;
                    }
                }
                else{
                    redPieces = 0;
                }
            }
        }
        return new int[]{yellowScore, redScore};
    }
    private int[] horizontalScore (char[][] board) {
        int yellowScore = 0;
        int redScore = 0;
        int redPieces = 0;
        int yellowPieces = 0;
        for (int i=0, k=0; i<7 && k<7; i++, k++){
            yellowPieces = 0;
            redPieces = 0;
            for (int j=0, w=0; j<6 && w<6; j++, w++){
                if (board[i][j] == 'y'){
                    yellowPieces++;
                    if (yellowPieces == 4) {
                        yellowPieces = 3;
                        yellowScore++;
                    }
                }
                else {
                    yellowPieces = 0;
                }
                if (board[k][w] == 'r'){
                    redPieces++;
                    if (redPieces == 4){
                        redPieces = 3;
                        redScore++;
                    }
                }
                else{
                    redPieces = 0;
                }
            }
        }
        return new int[]{yellowScore, redScore};
    }
}
