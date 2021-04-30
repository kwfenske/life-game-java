/*
  Java 1.1 AWT Applet - John Conway's Game of Life
  Written by: Keith Fenske, http://www.psc-consulting.ca/fenske/
  Monday, 5 January 2004
  Java class name: Life3
  Copyright (c) 2004 by Keith Fenske.  Released under GNU Public License.

  This is a graphical Java 1.1 AWT (GUI) applet to play John Conway's Game of
  Life.  For reference, see articles in "Scientific American" (October 1970 and
  February 1971) and "Time" magazine (21 January 1974).  Life is not your
  average two-player game.  You and the computer work together, with you making
  decisions, and the computer doing calculations.  You define a board pattern
  and then watch as the computer projects future generations of this pattern
  based on rules for growth and decay.  Even with some of the most trivial
  initial patterns, you can obtain results that are both beautiful and
  intriguing.  You may run this program as a stand-alone application, or as an
  applet on the following web page:

      John Conway's Game of Life - by: Keith Fenske
      http://www.psc-consulting.ca/fenske/life3a.htm

  Cells (squares) on a rectangular grid are either empty or full (occupied).
  The next generation is calculated by counting the number of occupied
  neighbors, up to a maximum of eight.  Fewer than two neighbors, or more than
  three, results in death: the cell will be empty in the next generation.  No
  change is made if there are exactly two neighbors.  Exactly three neighbors
  results in a birth: the cell will be occupied.  This updating is
  "instantaneous" and does not change squares before everybody's fate has been
  determined.  Try the 8-generation Cheshire cat pattern ("." are empty; "O"
  are full):

      . . . . . . . . . .
      . . . . . . . . . .
      . . . O . . O . . .
      . . . O O O O . . .
      . . O . . . . O . .
      . . O . O O . O . .
      . . O . . . . O . .
      . . . O O O O . . .
      . . . . . . . . . .
      . . . . . . . . . .

  Click the mouse to change a cell.  If the cell is currently empty, then it
  will be filled.  If the cell is currently full, then it will be emptied.
  Cells are shown in color.  A cell with the background color is empty, and has
  been empty for a while.  A blue cell is a death: newly empty.  A green cell
  is a birth: newly occupied.  A white cell has been occupied for a while.  The
  number of cells in the game board is determined by how many cells fit in the
  current window size.  There are "Bigger" and "Smaller" buttons to change the
  size of the cells.  If the window size changes, the new game board is
  centered on the old game board.

  Internally, the program adds a border of empty cells around the game board,
  because this makes calculating the updates easier (no special cases for the
  number of neighbors).

  GNU General Public License (GPL)
  --------------------------------
  Life3 is free software: you can redistribute it and/or modify it under the
  terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License or (at your option) any later
  version.  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
  more details.

  You should have received a copy of the GNU General Public License along with
  this program.  If not, see the http://www.gnu.org/licenses/ web page.

  -----------------------------------------------------------------------------

  Java Applet Notes:

  The recommended way of writing applets is to use Java Swing, according to Sun
  Microsystems, the creators and sponsors of Java.  Unfortunately, most web
  browsers don't support Swing unless you download a recent copy of the Java
  run-time environment from Sun.  This leaves a Java programmer with two
  choices:

  (1) Write applets using only old features found in the AWT interface.  The
      advantage, if you can see it this way, is that the programmer gets a
      detailed opportunity to interact with the graphical interface.  (Joke.)

  (2) Force users to visit http://java.sun.com/downloads/ to download and
      install a newer version of Java.  However, forcing anyone to download
      something before they can visit your web page is a poor idea.

  A worse idea is new browsers that don't have any Java support at all, unless
  the user first downloads Sun Java.  Microsoft stopped distributing their
  version of Java in 2003 starting with Windows XP SP1a (February), then
  Windows 2000 SP4 (June).  Until Microsoft and Sun resolve their various
  lawsuits -- or until Microsoft agrees to distribute an unaltered version of
  Sun Java -- there will be an increasing number of internet users that have
  *no* version of Java installed on their machines!

  The design considerations for this applet are as follows:

  (1) The applet should run on older browsers as-is, without needing any
      additional downloads and/or features.  The minimum target is JDK1.1 which
      is Microsoft Internet Explorer 5.0 (Windows 98) and Netscape 4.7/4.8 (JDK
      1.1.5 from 1997).

  (2) The applet should contain only a single class, with no external images,
      so that it can be downloaded as one file with an HTML web page.

  (3) The default background in the Sun Java applet viewer is white, but most
      web browsers use light grey.  To get the background color that you want,
      you must setBackground() on components or fillRect() with the color of
      your choice.

  (4) A small main() method is included with a WindowAdapter subclass, so that
      this program can be run as an application.  The default window size and
      position won't please everyone.
*/

import java.applet.*;             // older Java applet support
import java.awt.*;                // older Java GUI support
import java.awt.event.*;          // older Java GUI event support

public class Life3
       extends Applet
       implements ActionListener, MouseListener, Runnable
{
  /* constants */

  static final String beginMessage = "Click the mouse to fill or empty board cells.";
  static final int canvasBorder = 10; // empty pixels around game board
  static final int[] cellSizeList = {10, 13, 16, 20, 25, 32, 40, 50, 63, 80, 100};
                                  // list of cell sizes for "Bigger" / "Smaller"
                                  // Any photographers recognize this sequence?
  static final String noMessage = " "; // message text when nothing to say

  static final int CellNEWEMPTY = 1;  // cell is newly empty
  static final int CellNEWFULL = 2;   // cell is newly full
  static final int CellOLDEMPTY = 3;  // cell continues to be empty
  static final int CellOLDFULL = 4;   // cell continues to be full

  static final Color BACKGROUND = new Color(255, 204, 204); // light pink
  static final Color ColorGRIDLINE = new Color(204, 153, 153); // darker pink
  static final Color ColorNEWEMPTY = new Color(153, 153, 204); // off blue
  static final Color ColorNEWFULL = new Color(102, 204, 102); // off green
  static final Color ColorOLDEMPTY = BACKGROUND;
  static final Color ColorOLDFULL = new Color(204, 255, 255); // light cyan

  /* class variables */

  /* instance variables, including shared GUI components */

  Button biggerButton;            // "Bigger Cells" button
  Canvas boardCanvas;             // where we draw the game board
  int[][] boardCounts;            // count the neighbors for <boardData>
  int[][] boardData;              // internal game board (CellXXX)
  int boardGridLine;              // width of board grid lines (in pixels), as
                                  // ... set by most recent update()
  int boardGridStep;              // calculated size of each board cell, as set
                                  // ... by most recent update().  Includes
                                  // ... inner borders and one set of grid
                                  // ... lines.
  int boardInnerBorder;           // pixels in each cell's inner border, as set
                                  // ... by most recent update()
  int boardLeftMargin;            // adjusted left margin to center game board,
                                  // ... as set by most recent update()
  int boardSymbolSize;            // pixels for each cell's symbol, as set by
                                  // ... most recent update()
  int boardTopMargin;             // adjusted top margin to center game board,
                                  // ... as set by most recent update()
  int cellSizeIndex;              // current entry in <cellSizeList>
  Button clearButton;             // "Clear Board" button
  Thread clockThread;             // clock thread for running automatic updates
  boolean eraseFlag;              // true if background should be erased on
                                  // ... next call to update()
  Label messageText;              // information or status message for user
  int numCols;                    // number of visible columns in game board
  int numRows;                    // number of visible rows in game board
  Button randomButton;            // "Random Fill" button
  Button runButton;               // "Run" and "Stop" button
  boolean runFlag;                // true if running automatic updates
  Button smallerButton;           // "Smaller Cells" button
  Button updateButton;            // "Update Once" button


/*
  init() method

  Initialize this applet (equivalent to the main() method in an application).
  Please note the following about writing applets:

  (1) An Applet is an AWT Component just like a Button, Frame, or Panel.  It
      has a width, a height, and you can draw on it (given a proper graphical
      context, as in the paint() method).

  (2) Applets shouldn't attempt to exit, such as by calling the System.exit()
      method, because this isn't allowed on a web page.
*/
  public void init()
  {
    /* Intialize our own data before creating the GUI interface. */

    cellSizeIndex = cellSizeList.length / 2;
                                  // initial cell size is near middle of list
    clockThread = null;           // no thread for automatic updates yet
    eraseFlag = true;             // erase background on next paint/update
    numRows =  5;                 // visible rows: will get resized by update()
    numCols =  8;                 // visible columns: will also get resized
    runFlag = false;              // not running automatic updates

    clearBoard();                 // create and clear the game board

    /* Create the GUI interface as a series of little panels inside bigger
    panels.  The intermediate panel names (panel1, panel2, etc) are of no
    importance and hence are only numbered. */

    /* Make a horizontal panel to hold six equally-spaced buttons. */

    Panel panel1 = new Panel(new GridLayout(1, 6, 25, 0));

    biggerButton = new Button("Bigger");
    biggerButton.addActionListener((ActionListener) this);
    panel1.add(biggerButton);

    smallerButton = new Button("Smaller");
    smallerButton.addActionListener((ActionListener) this);
    panel1.add(smallerButton);

    clearButton = new Button("Clear");
    clearButton.addActionListener((ActionListener) this);
    panel1.add(clearButton);

    randomButton = new Button("Random");
    randomButton.addActionListener((ActionListener) this);
    panel1.add(randomButton);

    updateButton = new Button("Update");
    updateButton.addActionListener((ActionListener) this);
    panel1.add(updateButton);

    runButton = new Button("Run");
    runButton.addActionListener((ActionListener) this);
    panel1.add(runButton);

    /* Put the message field under the counters/buttons. */

    Panel panel2 = new Panel(new GridLayout(2, 1, 0, 5));
    panel2.add(panel1);
    messageText = new Label("Conway's Game of Life (Java applet).  Copyright (c) 2004 by Keith Fenske.  GNU Public License.", Label.CENTER);
    // JDK1.1 note: replace Font(null,...) with Font("Default",...)
    messageText.setFont(new Font("Default", Font.PLAIN, 14));
    messageText.setBackground(BACKGROUND);
    panel2.add(messageText);

    /* Now put all that on top of a canvas for the game board, giving the game
    board the remaining window space.  Since we don't set a size for the board
    canvas, it is assigned some arbitrary width and zero height.  The update()
    method will calculate the correct placement later. */

    Panel panel3 = new Panel(new BorderLayout(10, 10));
    panel3.add(panel2, BorderLayout.NORTH);
    boardCanvas = new Canvas();
    panel3.add(boardCanvas, BorderLayout.CENTER);
    panel3.setBackground(BACKGROUND); // for Netscape 4.7/4.8 (JDK1.1)

    /* Create this applet's window as a single combined panel. */

    this.add(panel3);
    this.addMouseListener((MouseListener) this);
    this.setBackground(BACKGROUND);
    this.validate();              // do the window layout

    /* Now let the GUI interface run the game. */

  } // end of init() method


/*
  main() method

  Applets only need an init() method to start execution.  This main() method is
  a wrapper that allows the same applet code to run as an application.
*/
  public static void main(String[] args)
  {
    Applet appletPanel;           // the target applet's window
    Frame mainFrame;              // this application's window

    mainFrame = new Frame("John Conway's Game of Life - by: Keith Fenske");
    mainFrame.addWindowListener(new Life3Window());
    mainFrame.setLayout(new BorderLayout(5, 5));
    mainFrame.setLocation(new Point(50, 50)); // top-left corner of app window
    mainFrame.setSize(700, 500);  // initial size of application window
    appletPanel = new Life3();    // create instance of target applet
    mainFrame.add(appletPanel, BorderLayout.CENTER); // give applet full frame
    mainFrame.validate();         // do the application window layout
    appletPanel.init();           // initialize applet
    mainFrame.setVisible(true);   // show the application window

  } // end of main() method

// ------------------------------------------------------------------------- //

/*
  actionPerformed() method

  This method is called when the user clicks on one of the buttons.
*/
  public void actionPerformed(ActionEvent event)
  {
    messageText.setText(noMessage); // clear message to user
    Object source = event.getSource(); // where the event came from
    if (source == biggerButton)   // "Bigger Cells" button
    {
      if (cellSizeIndex < (cellSizeList.length - 1))
        cellSizeIndex ++;         // go to a bigger size, if there is one
      eraseFlag = true;           // erase background on next paint/update
    }
    else if (source == clearButton) // "Clear Board" button
    {
      stopRunning();              // stop automatic updates, if active
      clearBoard();               // clear the game board to empty all squares
      messageText.setText(beginMessage); // prompt user to click on board
      eraseFlag = true;           // erase background on next paint/update
    }
    else if (source == randomButton) // "Random Fill" button
    {
      randomFill();               // randomly fill empty squares
    }
    else if (source == runButton) // "Run" or "Stop" button
    {
      if (runFlag)                // are we running automatic updates?
        stopRunning();            // yes, then stop running
      else
        startRunning();           // no, then start running
    }
    else if (source == smallerButton) // "Smaller Cells" button
    {
      if (cellSizeIndex > 0)
        cellSizeIndex --;         // go to a smaller size, if there is one
      eraseFlag = true;           // erase background on next paint/update
    }
    else if (source == updateButton) // "Update Once" button
    {
      updateBoard();              // update game board once
    }
    else
    {
      System.out.println("error in actionPerformed(): ActionEvent not recognized: "
        + event);
    }
    repaint();                    // redraw game board

  } // end of actionPerformed() method


/*
  clearBoard() method

  Create a new game board, or clear the current game board to all empty
  cells.  Note that the internal game board has an empty border of one cell
  around the outside that we don't show the user.

  This method should not do any GUI calls such as repaint(), because there are
  several methods that make changes to GUI objects after calling clearBoard()
  and before they are ready to repaint.  If clearBoard() forced a repaint, then
  too many unnecessary paint operations would be performed.
*/
  void clearBoard()
  {
    if (boardData == null)        // if no previous game board
    {
      /* There is no current game board, so we must allocate arrays for both
      the game board and for counting the number of neighbors. */

      boardCounts = new int[numRows + 2][numCols + 2]; // count neighbors
      boardData = new int[numRows + 2][numCols + 2]; // game board + border
    }

    for (int row = 0; row < boardData.length; row ++)
      for (int col = 0; col < boardData[0].length; col ++)
      {
        boardCounts[row][col] = 0; // force counter array to initialize
        boardData[row][col] = CellOLDEMPTY; // make all cells empty
      }
  } // end of clearBoard() method


/*
  mouseClicked() method

  This method is called when the user clicks the mouse on the game board.  To
  be consistent with the (x,y) coordinates in update(), this mouse listener is
  for the whole applet window -- not just the dummy board canvas.

  Our calculations make use of several global variables set by the update()
  method.
*/
  public void mouseClicked(MouseEvent event)
  {
    int col;                      // calculate column number
    int colExtra;                 // remainder from column calculation
    int pixels;                   // temporary number of pixels
    int row;                      // calculate row number
    int rowExtra;                 // remainder from row calculation

    /* Convert the (x,y) coordinates into row and column numbers, with a little
    extra information to tell us if the user was clicking on a board position,
    or if the clicks are on an inner or outer border.  Note that we must adjust
    the visible row and column numbers by +1 to get the internal row and column
    numbers. */

    pixels = event.getX() - boardLeftMargin - boardGridLine;
    col = (pixels / boardGridStep) + 1;
    colExtra = pixels % boardGridStep;

    pixels = event.getY() - boardTopMargin - boardGridLine;
    row = (pixels / boardGridStep) + 1;
    rowExtra = pixels % boardGridStep;

    /* If the mouse click is on a board cell, then reverse the state of that
    cell.  If the mouse click is on a grid line or a border, then ignore the
    click. */

    if ((col < 1)                 // if click is outside game board
      || (col > numCols)
      || (colExtra < boardInnerBorder) // or on inner border
      || (colExtra > (boardInnerBorder + boardSymbolSize))
      || (row < 1)                // if click is outside game board
      || (row > numRows)
      || (rowExtra < boardInnerBorder) // or on inner border
      || (rowExtra > (boardInnerBorder + boardSymbolSize)))
    {
      messageText.setText("Please click on a board position.");
    }
    else
    {
      messageText.setText(noMessage); // clear message to user
      switch (boardData[row][col]) // does click empty or fill this cell?
      {
        case CellNEWEMPTY:        // empty becomes full
        case CellOLDEMPTY:
          boardData[row][col] = CellOLDFULL;
          break;

        case CellNEWFULL:         // full becomes empty
        case CellOLDFULL:
          boardData[row][col] = CellOLDEMPTY;
          break;

        default:
          System.out.println("error in mouseClicked(): bad boardData[" + row
            + "][" + col + "] = " + boardData[row][col]);
      }
    }
    repaint();                    // redraw the game board

  } // end of mouseClicked() method

  public void mouseEntered(MouseEvent event) { }
  public void mouseExited(MouseEvent event) { }
  public void mousePressed(MouseEvent event) { }
  public void mouseReleased(MouseEvent event) { }


/*
  paint() method

  Simple applets only have a paint() method, which erases the window and
  redraws all components each time.  However, this applet updates a dynamic
  game board.  If the background is erased each time, then the display will
  "flicker" because of a short period of time after the old game board
  disappears before the new game board is drawn.  Since the new game board goes
  in exactly the same place as the old board, erasing the background is not
  necessary.  To avoid flicker, this applet separates the paint() and update()
  methods.
*/
  public void paint(Graphics gr)
  {
    eraseFlag = true;             // force update() to erase background
    update(gr);                   // all work is done in update() so that the
                                  // ... background is only erased and redrawn
                                  // ... when necessary (to prevent "flicker")
  } // end of paint() method


/*
  randomFill() method

  Randomly fill some of the board squares in the *visible* game board.
*/
  void randomFill()
  {
    for (int row = 1; row <= numRows; row ++)
      for (int col = 1; col <= numCols; col ++)
      {
        switch (boardData[row][col])
        {
          case CellNEWEMPTY:      // empty squares have a random chance
          case CellOLDEMPTY:
            if (Math.random() < 0.10) // about 10% chance
              boardData[row][col] = CellNEWFULL;
            break;

          default:                // do nothing for occupied squares
            break;
        }
      }
  } // end of fillRandom() method


/*
  resizeBoard() method

  The caller gives us a number of *visible* rows and *visible* columns.  If
  this differs from the current game board, then create a new game board and
  copy the old data to/from the center.  There must already be a game board;
  see the createBoard() method.
*/
  void resizeBoard(
    int newRows,                  // new number of visible rows
    int newCols)                  // new number of visible columns
  {
    int copyCols;                 // number of columns to copy
    int copyRows;                 // number of rows to copy
    int fromCol;                  // starting visible column to copy from
    int fromRow;                  // starting visible row to copy from
    int[][] oldBoard;             // save old board during copying
    int oldCols;                  // visible columns in old board
    int oldRows;                  // visible rows in old board
    int toCol;                    // starting visible column to copy to
    int toRow;                    // starting visible row to copy to

    if ((newRows != numRows) || (newCols != numCols)) // only if different size
    {
      oldBoard = boardData;       // copy reference to current board data
      oldCols = numCols;          // save current number of board columns
      oldRows = numRows;          // save current number of board rows
      numCols = newCols;          // set new number of visible columns
      numRows = newRows;          // set new number of visible rows
      boardData = null;           // drop reference on current board
      clearBoard();               // get someone else to make a new board

      /* Calculate how many rows we will copy, where they will be copied from,
      and where they will be copied to. */

      if (oldRows < newRows)
      {
        copyRows = oldRows;       // copy all old rows
        fromRow = 1;              // from first visible row in old board
        toRow = 1 + ((newRows - oldRows) / 2); // offset row in new board
      }
      else // (oldRows >= newRows)
      {
        copyRows = newRows;       // only copy what will fit
        fromRow = 1 + ((oldRows - newRows) / 2); // offset row in old board
        toRow = 1;                // to first visible row in new board
      }

      /* Calculate how many columns we will copy, where they will be copied
      from, and where they will be copied to. */

      if (oldCols < newCols)
      {
        copyCols = oldCols;       // copy all old columns
        fromCol = 1;              // from first visible column in old board
        toCol = 1 + ((newCols - oldCols) / 2); // offset column in new board
      }
      else // (oldCols >= newCols)
      {
        copyCols = newCols;       // only copy what will fit
        fromCol = 1 + ((oldCols - newCols) / 2); // offset column in old board
        toCol = 1;                // to first visible column in new board
      }

      /* Copy from old game board to new game board. */

      for (int row = 0; row < copyRows; row ++)
        for (int col = 0; col < copyCols; col ++)
        {
          boardData[toRow + row][toCol + col]
            = oldBoard[fromRow + row][fromCol + col];
        }
    }

    /* Note: array reference <oldBoard> to the old game board is a local
    variable, and will be released when this method returns. */

  } // end of resizeBoard() method


/*
  run() method

  This method is a separate thread that executes in the same context as the
  applet.  It automatically updates the game board every second, until told to
  stop.

  Note:  Please don't confuse the thread run() method with the applet start()
  and stop() methods.
*/
  public void run()
  {
    while (runFlag)               // run until we are told to stop
    {
      updateBoard();              // update game board once
      repaint();                  // redraw game board
      try { Thread.sleep(1000); } // sleep for 1000 milliseconds
        catch (InterruptedException e) { /* do nothing */ }
    }
  } // end of run() method


/*
  start() method

  This method is called to start the applet's execution, such as when the
  applet is loaded or when the user revisits a page that contains the applet.
  We don't really need a start() method, because we don't restart automatic
  updates even if they were stopped by the stop() method.
*/
  public void start()
  {
    /* Do nothing. */

  } // end of start() method


/*
  startRunning() method

  Start running automatic updates at one-second intervals.  Start our own
  thread, since the java.util.Timer class is JDK1.3 and the javax.swing.Timer
  class (JDK1.2) is obviously part of the Java Swing package, where we need
  something that runs in JDK1.1.
*/
  void startRunning()
  {
    messageText.setText("Automatic updates are running, until you click the \"Stop\" button.");
    runButton.setLabel("Stop");   // change label on "Run"/"Stop" button
    runFlag = true;               // must be true while clock thread is running
    clockThread = new Thread(this); // act as our own timer object
    clockThread.start();          // start the run() clock thread

  } // end of startRunning() method


/*
  stop() method

  This method is called to stop the applet's execution, such as when the user
  leaves the applet's page or quits the browser.  We need to stop automatic
  updates if they are active.
*/
  public void stop()
  {
    stopRunning();                // stop automatic updates, if active

  } // end of stop() method


/*
  stopRunning() method

  Stop running automatic updates at one-second intervals.
*/
  void stopRunning()
  {
    runFlag = false;              // clock thread will notice this and stop
    runButton.setLabel("Run");    // change label on "Run"/"Stop" button

  } // end of stopRunning() method


/*
  update() method

  Draw the game board directly onto the lower part of the applet window.  After
  this method returns, other AWT components (buttons, counters, etc) will draw
  themselves on *top* of whatever we draw here.  We estimate the starting
  position of the game board from the <boardCanvas> component.  Several global
  variables are set for later use by the mouse listener to determine where
  board cells are located.

  When an applet runs on a web page, the initial window size is chosen by the
  web page's HTML code and can't be changed by the applet.  Applets running
  outside of a web page (such as with Sun's applet viewer) can change their
  window size at any time.  The user may enlarge or reduce the window to make
  it fit better on his/her display.  Hence, while this applet doesn't attempt
  to change the window size, it must accept that the window size may be
  different each time the paint() method is called.  A good applet redraws its
  components to fit the window size.
*/
  public void update(Graphics gr)
  {
    int boardHeight;              // height (in pixels) of actual game board
    int boardWidth;               // width (in pixels) of actual game board
    int col;                      // temporary column number (index)
    int corner;                   // how much we round corners on rectangle
    int hz;                       // temporary number of horizontal pixels
    int row;                      // temporary row number (index)
    int vt;                       // temporary number of vertical pixels

    /* Clear the entire applet window to our own background color, not the
    default (which is sometimes white and sometimes grey). */

    if (eraseFlag)                // only if requested
    {
      gr.setColor(BACKGROUND);
      // JDK1.1 note: replace this.getWidth() with this.getSize().width
      // JDK1.1 note: replace this.getHeight() with this.getSize().height
      gr.fillRect(0, 0, this.getSize().width, this.getSize().height);
      eraseFlag = false;          // background has been erased
    }

    /* The <boardCanvas> component gets positioned below our buttons and the
    message field.  The assigned size of <boardCanvas> is not important; we
    only need to know the starting y coordinate, so that we can draw the board
    below all the other AWT components.  We calculate the size of the board (in
    pixels) using the applet's window size minus the starting y coordinate of
    <boardCanvas>, minus a border.  The border serves two purposes: to prevent
    the game board from touching the window's edge, and as a safety zone above
    the board where the other AWT components will appear along with a lower
    border that is *not* accounted for in the starting y coordinate for
    <boardCanvas>. */

    boardLeftMargin = canvasBorder; // first estimate for board's left margin
    // JDK1.1 note: replace boardCanvas.getY() with boardCanvas.getLocation().y
    boardTopMargin = boardCanvas.getLocation().y + canvasBorder;
                                  // first estimate for board's top margin
    // JDK1.1 note: replace this.getWidth() with this.getSize().width
    boardWidth = this.getSize().width - boardLeftMargin - canvasBorder;
    boardWidth = (boardWidth > 0) ? boardWidth : 0;
    // JDK1.1 note: replace this.getHeight() with this.getSize().height
    boardHeight = this.getSize().height - boardTopMargin - canvasBorder;
    boardHeight = (boardHeight > 0) ? boardHeight : 0;

    /* The size in pixels of each board cell is determined by the user and is
    the current entry in the cellSizeList[cellSizeIndex] array.  Around this,
    we add a small border proportional to the size of the board cell, and a
    grid line that is always one pixel wide.  Since the cells are of a fixed
    size, the number of rows or columns that can be displayed in the current
    applet window may not be the same as the current board size.  In this game,
    we resize the game board to fit in the current window, whereas most games
    would resize the board cells.

    Note here and elsewhere that the number of grid lines is one more than the
    number of rows or columns, because the grid lines surround the entire game
    board. */

    boardSymbolSize = cellSizeList[cellSizeIndex]; // decided by user (fixed)
    boardInnerBorder = (int) (boardSymbolSize * 0.06);
                                  // pixels for inner borders
    boardInnerBorder = (boardInnerBorder > 2) ? boardInnerBorder : 2;
                                  // minimum of two pixels
    boardGridLine = 1;            // fixed width of board grid lines
    boardGridStep = boardSymbolSize + (2 * boardInnerBorder) + boardGridLine;
    hz = (boardWidth - boardGridLine) / boardGridStep;
                                  // number of columns that we can display
    hz = (hz > 2) ? hz : 2;       // minimum of 2 columns
    vt = (boardHeight - boardGridLine) / boardGridStep;
                                  // number of rows that we can display
    vt = (vt > 2) ? vt : 2;       // minimum of 2 rows
    resizeBoard(vt, hz);          // resize game board, if necessary

    /* Compute a new left margin and top margin so that our game board will be
    centered on the panel.  Note that resizeBoard() may have changed <numCols>
    and <numRows>. */

    hz = (boardWidth - boardGridLine - (numCols * boardGridStep)) / 2;
    hz = (hz > 0) ? hz : 0;       // can't be negative
    boardLeftMargin += hz;        // plus defined left border

    vt = (boardHeight - boardGridLine - (numRows * boardGridStep)) / 2;
    vt = (vt > 0) ? vt : 0;       // can't be negative
    boardTopMargin += vt;         // plus defined top border

    /* Draw vertical grid lines between columns. */

    gr.setColor(ColorGRIDLINE);
    hz = boardLeftMargin;         // x coordinate of first grid line
    vt = (numRows * boardGridStep) + boardGridLine; // height in pixels (constant)
    for (col = 0; col <= numCols; col ++)
    {
      gr.fillRect(hz, boardTopMargin, boardGridLine, vt);
      hz += boardGridStep;        // x coordinate for next column
    }

    /* Draw horizontal grid lines between rows. */

    vt = boardTopMargin;          // y coordinate of first grid line
    hz = (numCols * boardGridStep) + boardGridLine; // width in pixels (constant)
    for (row = 0; row <= numRows; row ++)
    {
      gr.fillRect(boardLeftMargin, vt, hz, boardGridLine);
      vt += boardGridStep;        // y coordinate for next row
    }

    /* Draw the board.  All cells are drawn as circles; only the color changes.
    Old empty cells are also drawn as circles ... with the background color. */

    corner = (int) (boardGridStep * 0.65); // rounded corners on rectangle
    vt = boardTopMargin + boardGridLine + boardInnerBorder;
                                  // y coordinate of first cell symbol in first
                                  // ... column in first row
    for (row = 1; row <= numRows; row ++)
    {
      hz = boardLeftMargin + boardGridLine + boardInnerBorder;
                                  // x coordinate of first cell symbol in first
                                  // ... column in this row
      for (col = 1; col <= numCols; col ++)
      {
        switch (boardData[row][col]) // select correct color
        {
          case CellNEWEMPTY:
            gr.setColor(ColorNEWEMPTY);
            break;

          case CellNEWFULL:
            gr.setColor(ColorNEWFULL);
            break;

          case CellOLDEMPTY:
            gr.setColor(ColorOLDEMPTY);
            break;

          case CellOLDFULL:
            gr.setColor(ColorOLDFULL);
            break;

          default:
            System.out.println("error in update(): bad boardData[" + row
              + "][" + col + "] = " + boardData[row][col]);
        }
//      gr.fillOval(hz, vt, boardSymbolSize, boardSymbolSize); // draw circle
        gr.fillRoundRect(hz, vt, boardSymbolSize, boardSymbolSize, corner,
          corner);                // draw positions as rounded rectangles
        hz += boardGridStep;      // x coordinate for next column
      }
      vt += boardGridStep;        // y coordinate for next row
    }
  } // end of update() method


/*
  updateBoard() method

  Update the game board for one generation.  This is the method that counts and
  decides each cell's fate.
*/
  void updateBoard()
  {
    boolean changeFlag;           // true if something changes on this board
    int col;                      // temporary column number (index)
    int row;                      // temporary row number (index)

    changeFlag = false;           // assume that nothing will change

    /* Count the number of neighbors.  Since we put an empty border outside the
    visible board, it is safe to access all eight neighbors. */

    for (row = 1; row <= numRows; row ++)
      for (col = 1; col <= numCols; col ++)
      {
        boardCounts[row][col] =
            updateBoardFull(row - 1, col - 1)
          + updateBoardFull(row - 1, col    )
          + updateBoardFull(row - 1, col + 1)
          + updateBoardFull(row    , col - 1)
          + updateBoardFull(row    , col + 1)
          + updateBoardFull(row + 1, col - 1)
          + updateBoardFull(row + 1, col    )
          + updateBoardFull(row + 1, col + 1);
      }

    /* Update the game board using the number of neighbors.  In all of the
    following switch statements, the default case is omitted, since nothing
    needs to be done. */

    for (row = 1; row <= numRows; row ++)
      for (col = 1; col <= numCols; col ++)
      {
        if ((boardCounts[row][col] < 2) || (boardCounts[row][col] > 3))
        {
          /* Too few or too many neighbors means death. */

          switch (boardData[row][col])
          {
            case CellNEWEMPTY:    // what was once new is now old
              boardData[row][col] = CellOLDEMPTY;
              changeFlag = true;  // visible color changes
              break;

            case CellNEWFULL:     // cell was previously full
            case CellOLDFULL:
              boardData[row][col] = CellNEWEMPTY;
              changeFlag = true;
              break;
          }
        }
        else if (boardCounts[row][col] == 2)
        {
          /* Exactly two neighbors means no change. */

          switch (boardData[row][col])
          {
            case CellNEWEMPTY:    // what was once new is now old
              boardData[row][col] = CellOLDEMPTY;
              changeFlag = true;  // visible color changes
              break;

            case CellNEWFULL:     // what was once new is now old
              boardData[row][col] = CellOLDFULL;
              changeFlag = true;  // visible color changes
              break;
          }
        }
        else // (boardCounts[row][col] == 3)
        {
          /* Exactly three neighbors means birth. */

          switch (boardData[row][col])
          {
            case CellNEWEMPTY:    // cell was previously empty
            case CellOLDEMPTY:
              boardData[row][col] = CellNEWFULL;
              changeFlag = true;
              break;

            case CellNEWFULL:     // what was once new is now old
              boardData[row][col] = CellOLDFULL;
              changeFlag = true;  // visible color changes
              break;
          }
        }
      }

    /* If nothing changed on this board, then set a message for the user, and
    cancel any running update timer. */

    if (changeFlag == false)
    {
      if (runFlag)                // are automatic updates running?
      {
        messageText.setText("The board is not changing.  \"Run\" button now stopped.");
        stopRunning();            // stop running automatic updates
      }
      else
        messageText.setText("The board is not changing.");
    }
  } // end of updateBoard() method


/*
  updateBoardFull() method

  This is a helper method for updateBoard().  It returns zero if the indicated
  board square is empty and one if it's full.
*/
  int updateBoardFull(
    int row,                      // visible row number: 1 to <numRows>
    int col)                      // visible column number: 1 to <numCols>
  {
    if ((boardData[row][col] == CellNEWFULL)
      || (boardData[row][col] == CellOLDFULL))
      return 1;
    else
      return 0;

  } // end of updateBoardFull() method

} // end of Life3 class

// ------------------------------------------------------------------------- //

/*
  Life3Window class

  This applet can also be run as an application by calling the main() method
  instead of the init() method.  As an application, it must exit when its main
  window is closed.  A window listener is necessary because EXIT_ON_CLOSE is a
  JFrame option in Java Swing, not a basic AWT Frame option.  It is easier to
  extend WindowAdapter here with one method than to implement all methods of
  WindowListener in the main applet.
*/

class Life3Window extends WindowAdapter
{
  public void windowClosing(WindowEvent event)
  {
    System.exit(0);               // exit from this application
  }
} // end of Life3Window class

/* Copyright (c) 2004 by Keith Fenske.  Released under GNU Public License. */
