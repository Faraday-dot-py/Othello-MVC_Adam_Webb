package com.mrjaffesclass.apcs.mvc;
import com.mrjaffesclass.apcs.messenger.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;  


public class View extends JComponent implements MessageHandler {

    private final Messenger mvcMessaging;

    private int width;

    private int height;

    private Board board;
    
    private final int squareWidth = 80;
    
    private Graphics2D g2d;
    
    private Square[][] squares = new Square[Constants.SIZE][Constants.SIZE];        
    
    private int blackScore = 2;
    
    private int whiteScore = 2;
    
    private Font font = new Font("Serif", Font.PLAIN, 30);
    
    private boolean whoseTurn = true;
    
    private boolean gameOver = false;

    /**
     * Creates a new view
     * @param messages mvcMessaging object
     */
    public View(int x, int y, Messenger messages) {
      mvcMessaging = messages;   // Save the calling controller instance

      width = x - 15;
      height = y - 14;
      
      initBoard(squares);
      
      board = new Board(squares);
    }

    /**
     * Initialize the model here and subscribe
     * to any required messages
     */
    public void init() {
      // Subscribe to messages here
      mvcMessaging.subscribe("BoardUpdate", this);
      mvcMessaging.subscribe("NoMoves", this);
      mvcMessaging.subscribe("GameOver", this);
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

    protected void paintComponent(Graphics g){
            g2d = (Graphics2D) g;
            drawGrid(g2d);
  //          updateBoard();
            System.out.println("PAINT_COMPOENT");
            updateBoard();
            g2d.setFont(font);
            g2d.setColor(new Color(0, 0, 0));
            drawScores();
            displayTurn();
            if (this.gameOver){
                g2d.setColor(new Color(0, 0, 0));
                g2d.drawString("Game Over!", width/2 - 50, height/2);
                
            }
    }

    @Override
    public void messageHandler(String messageName, Object messagePayload) {
        if (messagePayload != null) {
          System.out.println("MSG: received by view: "+messageName+" | "+messagePayload.toString());
        } else {
          System.out.println("MSG: received by view: "+messageName+" | No data sent");
        }
        
        if (!this.gameOver){
            if (messageName.equals("BoardUpdate")){
                System.out.println("BOARD UPDATE");
                squares = (Square[][])messagePayload;
                displayTurn();
                mvcMessaging.notify("repaint");
                this.whoseTurn = !this.whoseTurn;

           } else if (messageName.equals("NoMoves")){
                System.out.println("NoMoves");
                squares = (Square[][])messagePayload;
                displayTurn();
                mvcMessaging.notify("repaint");
                this.whoseTurn = !this.whoseTurn;

           } else if (messageName.equals("GameOver")){
               this.gameOver = true;
           }
        }
    }

    private void drawGrid(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        
        Rectangle2D.Double background = new Rectangle2D.Double(0, 0, this.width, this.height);
        g2.setColor(new Color(0, 150, 0));
        g2.fill(background);
        
        
        g2.setColor(new Color(0, 0, 0));
        for (int x = squareWidth; x < width; x += squareWidth){
            g2.drawLine(x, 0, x, height);
        }
        
        for (int y = squareWidth; y < width; y += squareWidth){
            g2.drawLine(0, y, height, y);
        }
        
        putPiece(3, 3, true);
        putPiece(3, 4, false);
        putPiece(4, 3, false);
        putPiece(4, 4, true);
    }
    
    private void putPiece(double x, double y, boolean whoseTurn){
        double trueX = x * 80.0 + 15;
        double trueY = y * 80.0 + 15;
        
        Ellipse2D.Double piece = new Ellipse2D.Double(trueX, trueY, 50, 50);
        
        if (whoseTurn) {
            g2d.setColor(Color.WHITE);
        }
        else{
            g2d.setColor(Color.BLACK);
        }
        
        g2d.fill(piece);        
    }
    
    private void updateBoard(){
        System.out.println("Called Update Board");
        for (int row = 0; row < Constants.SIZE; row++){
            for (int col = 0; col < Constants.SIZE; col++){
                System.out.print(squares[row][col]);
                if (squares[row][col].getStatus() == -1){
                    putPiece(row, col, false);
                }else if (squares[row][col].getStatus() == 1){
                    putPiece(row, col, true);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
    
    private void drawScores(){
        Board board = new Board(squares);
        
        this.blackScore = board.countSquares(Constants.BLACK);
        this.whiteScore = board.countSquares(Constants.WHITE);
        
        g2d.drawString(String.format("Black: %d, White: %d", whiteScore, blackScore), width/2 - 150, height + 30);
    }
    
    private void displayTurn(){
        g2d.setColor(new Color(0, 0, 0));
        Shape outer = new Ellipse2D.Double(width/2 + 99, height, 51, 51);
        g2d.draw(outer);
        if (this.whoseTurn){
            g2d.setColor(new Color(0, 0, 0));
        } else{
            g2d.setColor(new Color(255, 255, 255));
        }
        Shape inner = new Ellipse2D.Double(width/2 + 100, height + 1, 49, 49);
        g2d.fill(inner);
    }
}
