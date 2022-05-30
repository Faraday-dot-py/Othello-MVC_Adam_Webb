package com.mrjaffesclass.apcs.mvc;

import com.mrjaffesclass.apcs.messenger.*;

/**
 * The model represents the data that the app uses.
 * @author Roger Jaffe
 * @version 1.0
 */
public class Model implements MessageHandler {

    // Messaging system for the MVC
    private final Messenger mvcMessaging;

    private boolean whoseTurn = true;
    
    private Square[][] squares = new Square[Constants.SIZE][Constants.SIZE];

    /**
     * Model constructor: Create the data representation of the program
     * @param messages Messaging class instantiated by the Controller for 
     *   local messages between Model, View, and controller
     */
    public Model(Messenger messages) {
      mvcMessaging = messages;
      
      squares = this.initBoard(squares);
    }

    /**
     * Initialize the model here and subscribe to any required messages
     */
    public void init() {
        mvcMessaging.subscribe("MouseClicked", this);
    }
    
    @Override
    public void messageHandler(String messageName, Object messagePayload) {
      if (messagePayload != null) {
        System.out.println("MSG: received by model: "+messageName+" | "+messagePayload.toString());
      } else {
        System.out.println("MSG: received by model: "+messageName+" | No data sent");
      }

      if (messageName.equals("MouseClicked")){
//          mvcMessaging.notify("BoardUpdate", squares);
          String message = messagePayload.toString();
          int x = Integer.parseInt(message.substring(0, 1));
          int y = Integer.parseInt(message.substring(2));

          Position pos = new Position(x, y);
          Player player;
          if (this.whoseTurn){
              player = new Player(Constants.WHITE);
          }else{
              player = new Player(Constants.BLACK);
          }

          if (isLegalMove(player, pos)){
              makeMove(player, pos);
              this.whoseTurn = !this.whoseTurn;
              System.out.println("Sent Message");
              mvcMessaging.notify("BoardUpdate", squares);
          }
          
          boolean noMovesWhite = noMovesAvailable(new Player(Constants.WHITE));
          boolean noMovesBlack = noMovesAvailable(new Player(Constants.BLACK));
          
          if (noMovesWhite && noMovesBlack){
              mvcMessaging.notify("GameOver", squares);
          }else if (noMovesAvailable(new Player(!this.whoseTurn  ? 1 : -1))){
              this.whoseTurn = !this.whoseTurn;
              mvcMessaging.notify("NoMoves", squares);
          }
      }
    }
    
    private Square[][] initBoard(Square[][] squares)
  {
    for (int row = 0; row < Constants.SIZE; row++) {
      for (int col = 0; col < Constants.SIZE; col++) {
        squares[row][col] = new Square(Constants.EMPTY);
      }
    }
    squares[3][3].setStatus(Constants.WHITE);
    squares[4][4].setStatus(Constants.WHITE);
    squares[3][4].setStatus(Constants.BLACK);
    squares[4][3].setStatus(Constants.BLACK);
    return squares;
  }

    protected boolean indexInRange(int idx) {
      return 0 <= idx && idx < Constants.SIZE;
    }

    protected void setSquare(Player player, Position position) {
      this.squares[position.getRow()][position.getCol()].setStatus(player.getColor());
    }

    /**
     * Get the status of a square using row and col
     * @param player Player asking for the square information
     * @param row Row to retrieve
     * @param col Column to retrieve
     * @return Square object representing the square requested
     */
    public Square getSquare(Player player, int row, int col) {
      if (this.indexInRange(row) && this.indexInRange(col)) {
        Position pos = new Position(row, col);
        return this.getSquare(pos);
      } else {
        System.out.println("**** INDEX INTO THE BOARD IS OUT OF RANGE ["+row+","+col+"]");
        System.out.println(player.toString()+" MESSED UP. YOU LOSE");
        System.exit(0);
        return null;
      }
    }

    /**
     * Get the status of a square
     * @param position Position of requested square
     * @return Square object representing the square requested
     */
    public Square getSquare(Position position) {
      return this.squares[position.getRow()][position.getCol()];
    }

    /**
     * Is this a legal move?
     * @param player Player asking
     * @param positionToCheck Position of the move being checked
     * @return True if this space is a legal move
     */
    public boolean isLegalMove(Player player, Position positionToCheck) {
      // If the space isn't empty, it's not a legal move
      if (getSquare(positionToCheck).getStatus() != Constants.EMPTY)
        return false;
      // Check all directions to see if the move is legal
      for (String direction : Directions.getDirections()) {
        Position directionVector = Directions.getVector(direction);
        if (step(player, positionToCheck, directionVector, 0)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Are there any available for this player?
     * @param player Player asking
     * @return True if moves are available
     */
    public boolean noMovesAvailable(Player player) {
      for (int row = 0; row < Constants.SIZE; row++) {
        for (int col = 0; col < Constants.SIZE; col++) {
          Position pos = new Position(row, col);
          if (isLegalMove(player, pos)) {
            return false;
          }
        }
      }
      return true;
    }

    /**
     * Traverses the board in the provided direction. Checks the status of
     * each space: 
     * a. If it's the opposing player then we'll move to the next
     *    space to see if there's a blank space
     * b. If it's the same player then this direction doesn't represent
     *    a legal move
     * c. If it's a blank AND if it's not the adjacent square then this
     *    direction is a legal move. Otherwise, it's not.
     * 
     * @param player  Player making the request
     * @param position Position being checked
     * @param direction Direction to move
     * @param count Number of steps we've made so far
     * @return True if we find a legal move
     */
    protected boolean step(Player player, Position position, Position direction, int count) {
        Position newPosition = position.translate(direction);
        
        while (!newPosition.isOffBoard()) {
            
            if (this.getSquare(newPosition).getStatus() == Constants.EMPTY){
                return false;
            }else if (!player.isThisPlayer(this.getSquare(newPosition).getStatus()) && this.getSquare(newPosition).getStatus() != Constants.EMPTY) {
             // If space has opposing player then move to next space in same direction
                return step(player, newPosition, direction, count+1);
            }else if (player.isThisPlayer(this.getSquare(newPosition).getStatus())){
                return count > 0;
            }
            
            newPosition = position.translate(direction);
        }
        return false;      
//            int color = player.getColor();
//        
//            if ((this.getSquare(newPosition).getStatus() == Constants.EMPTY) && (count == 0)) {
//             // If empty space AND adjacent to position then not legal
//                return false;
//            } else if (!player.isThisPlayer(this.getSquare(newPosition).getStatus()) && this.getSquare(newPosition).getStatus() != Constants.EMPTY) {
//             // If space has opposing player then move to next space in same direction
//                return step(player, newPosition, direction, count+1);
//            } else if (player.isThisPlayer(this.getSquare(newPosition).getStatus())) {
//             // If space has this player and we've moved more than one space then it's legal,
//             // otherwise it's not legal
//                return count > 0;
//            } else {
//             // Didn't pass any other test, not legal move
//                return false;
//            }
        
        
    }

    /**
     * Traverses the board in the provided direction. Checks the status of
     * each space: 
     * a. If it's the opposing player then we'll move to the next
     *    space to see if there's a blank space
     * b. If it's the same player then this direction doesn't represent
     *    a legal move
     * c. If it's a blank AND if it's not the adjacent square then this
     *    direction is a legal move. Otherwise, it's not.
     * If the move is legal, then this changes the pieces based on the 
     * the move
     * 
     * @param player  Player making the request
     * @param position Position being checked
     * @param direction Direction to move
     * @param count Number of steps we've made so far
     * @return True if we find a legal move
     */
    private boolean makeMoveStep(Player player, Position position, Position direction, int count) {
      Position newPosition = position.translate(direction);
      int color = player.getColor();
      if (newPosition.isOffBoard()) {
        return false;
      } else if (this.getSquare(newPosition).getStatus() == -color) {
        boolean valid = makeMoveStep(player, newPosition, direction, count+1);
        if (valid) {
          this.setSquare(player, newPosition);
        }
        return valid;
      } else if (this.getSquare(newPosition).getStatus() == color) {
        return count > 0;
      } else {
        return false;
      }    
    }

    /**
     * Make the move.  Scan all directions and switch the piece colors
     * of the ones as appropriate
     * @param playerToMove Player asking
     * @param positionToMove Position of the new move
     */
    public void makeMove(Player playerToMove, Position positionToMove) {
      for (String direction : Directions.getDirections()) {
        Position directionVector = Directions.getVector(direction);
        if (makeMoveStep(playerToMove, positionToMove, directionVector, 0)) {
          this.setSquare(playerToMove, positionToMove);      
  //      } else {
  //        System.out.println("**** THIS SPACE IS NOT A VALID MOVE. YOU LOSE!");
        }
      }
    }

}
