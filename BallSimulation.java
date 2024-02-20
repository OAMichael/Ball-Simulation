import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Graphics2D.*;

import java.util.LinkedList;

import static java.awt.geom.AffineTransform.*;
import java.awt.geom.AffineTransform;

public class BallSimulation extends Applet implements MouseListener, KeyListener {
    
    /*
     *  Rendering parameters
     */
    private final int mWindowWidth = 1200;
    private final int mWindowHeight = 800;
    
    private RenderingHints mRenderHints;
    
    /* SwapBuffer is used for double buffering */
    private BufferedImage mSwapBuffer;
    private Graphics mSwapBufferGraphics;


    /*
     *  Visual parameters
     */
    private final int mSquareMargin = 24;
    private final int mSquareLineWidth = 6;
    private final int mSquareSize = mWindowHeight - 2 * mSquareMargin;
    private final int mSquareXStart = mWindowWidth - 1 - mSquareSize - mSquareMargin;
    private final int mSquareYStart = mSquareMargin;

    private boolean mShowArrows = false;
    private boolean mTimeStopped = true;
    private int mSimulationSpeed = 1;
    private final int mMaxSimulationSpeed = 20;

    private final String mSimulationName = "Ball simulation";


    /*
     *  Simulation parameters
     */
    private final float mTimeTickValue = 0.001f;
    private int mTickCount = 0;
    private final int mBallDiameter = 24;
    private float mAlpha = 0.2f;
    
    private float mBallPositionX = 0.6f;
    private float mBallPositionY = 0.3f;

    private float mBallVelocityX = 0.2f;
    private float mBallVelocityY = 0.7f;

    private float mBallAccelerationX = 0.0f;
    private float mBallAccelerationY = 0.0f;


    /*
     *  UI parameters
     */
    private Label mLabelX;
    private Label mLabelY;
    private Label mLabelVX;
    private Label mLabelVY;
    private Label mLabelAlpha;
    private TextField mTextX;
    private TextField mTextY;
    private TextField mTextVX;
    private TextField mTextVY;
    private TextField mTextAlpha;
    private Checkbox mCheckboxShowArrows;
    private Checkbox mCheckboxTimeStopped;
    private Checkbox mCheckboxShowTail;
    private Scrollbar mSimulationSpeedSlider;


    class Position {
        public float x;
        public float y;

        Position(float posX, float posY) {
            x = posX;
            y = posY;
        }
    };

    private LinkedList<Position> mTail;
    private final int mMaxTailLength = 5;
    private boolean mShowTail = false;


    /*
     *  Overriden init() method of Applet class
     */
    public void init() {
        setSize(mWindowWidth, mWindowHeight);

        addMouseListener(this);
        addKeyListener(this); 

        /* Improve rendering quality by enabling antialiasing */
        mRenderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        mRenderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        mRenderHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        mRenderHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        mRenderHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        mRenderHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        mRenderHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        mRenderHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        initUI();

        /* Set main window to be focused initially */
        requestFocusInWindow();

        mTail = new LinkedList<Position>();
    }

    /*
     *  Overriden update() method of Applet class
     */
    public void update(Graphics g) {
        if (mSwapBuffer == null) {
            mSwapBuffer = new BufferedImage(mWindowWidth, mWindowHeight, BufferedImage.TYPE_INT_ARGB);
        }
        mSwapBufferGraphics = mSwapBuffer.getGraphics();
        
        mSwapBufferGraphics.setColor(getBackground());
        mSwapBufferGraphics.fillRect(0, 0, mWindowWidth, mWindowHeight);
        mSwapBufferGraphics.setColor(getForeground());
                
        if (!mTimeStopped) {
            for (int i = 0; i < mSimulationSpeed; i++) {
                if (mShowTail && mTickCount % 100 == 0) {
                    if (mTail.size() >= mMaxTailLength) {
                        mTail.remove();
                    }
                    mTail.add(new Position(mBallPositionX, mBallPositionY));
                }
                makeTick();
            }
        }

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(mRenderHints);
        paint(mSwapBufferGraphics);
        g2.drawImage(mSwapBuffer, 0, 0, this);
    }

    /*
     *  Overriden paint() method of Applet class
     */
    public void paint(Graphics g) {        
        drawSimulation(g);
        drawUI(g);
        repaint();
    }

    private void updateAcceleration() {
        mBallAccelerationX = mAlpha * (2.0f * mBallPositionX - 1.0f) / (mBallPositionX * (1 - mBallPositionX));
        mBallAccelerationY = mAlpha * (2.0f * mBallPositionY - 1.0f) / (mBallPositionY * (1 - mBallPositionY));
    }

    private void makeTick() {
        mBallPositionX += mBallVelocityX * mTimeTickValue;
        mBallPositionY += mBallVelocityY * mTimeTickValue;

        mBallVelocityX += mBallAccelerationX * mTimeTickValue;
        mBallVelocityY += mBallAccelerationY * mTimeTickValue;

        updateAcceleration();

        float rx = (float)mBallDiameter / (float)mSquareSize / 2.0f;
        float ry = (float)mBallDiameter / (float)mSquareSize / 2.0f;

        if (mBallPositionX <= rx || mBallPositionX >= 1 - rx) {
            mBallVelocityX *= -1;
        }

        if (mBallPositionY <= ry || mBallPositionY >= 1 - ry) {
            mBallVelocityY *= -1;
        }

        mTickCount++;
    }

    void drawArrow(Graphics g, int x1, int y1, int x2, int y2, Color color, int linewidth) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHints(mRenderHints);
        g2.setStroke(new BasicStroke(linewidth));
        g2.setColor(color);

        double angle = Math.atan2(y2 - y1, x2 - x1);
        int len = 50;
        int arLen = 4 * linewidth;

        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g2.transform(at);
        
        g2.drawLine(0, 0, len, 0);

        g2.fillPolygon(new int[] {len, len - arLen, len - arLen, len},
                       new int[] {  0,      -arLen,       arLen,   0}, 
                       4);
    }

    public void drawSimulation(Graphics g) {
        setBackground(Color.cyan);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(mRenderHints);
        g2.setStroke(new BasicStroke(mSquareLineWidth));

        if (mShowTail) {
            int diameter = 5;
            for (Position pos : mTail) {
                int col = diameter * 15;
                if (col > 128) {
                    col = 128;
                }
                g.setColor(new Color(col, col, col));
                g.fillOval( mSquareXStart + Math.round(pos.x * mSquareSize) - diameter / 2,
                            mSquareYStart + Math.round(pos.y * mSquareSize) - diameter / 2,
                            diameter,
                            diameter );

                diameter += 1;
            }
            g.setColor(Color.black);
        }

        g.drawRect( mSquareXStart,
                    mSquareYStart,
                    mSquareSize,
                    mSquareSize );
        
        g.fillOval( mSquareXStart + Math.round(mBallPositionX * mSquareSize) - mBallDiameter / 2,
                    mSquareYStart + Math.round(mBallPositionY * mSquareSize) - mBallDiameter / 2,
                    mBallDiameter,
                    mBallDiameter );

        if (mShowArrows) {        
            drawArrow(  g, 
                        mSquareXStart + Math.round(mBallPositionX * mSquareSize),
                        mSquareYStart + Math.round(mBallPositionY * mSquareSize),
                        mSquareXStart + Math.round((mBallPositionX + mBallVelocityX) * mSquareSize),
                        mSquareYStart + Math.round((mBallPositionY + mBallVelocityY) * mSquareSize),
                        Color.blue,
                        2);
        
            drawArrow(  g, 
                        mSquareXStart + Math.round(mBallPositionX * mSquareSize),
                        mSquareYStart + Math.round(mBallPositionY * mSquareSize),
                        mSquareXStart + Math.round((mBallPositionX + mBallAccelerationX) * mSquareSize),
                        mSquareYStart + Math.round((mBallPositionY + mBallAccelerationY) * mSquareSize),
                        Color.red,
                        2);
        }
    }

    public void initUI() {
        setLayout(null);

        mLabelX = new Label("x:");
        mLabelX.setBounds(60, 220, 25, 25);
        mLabelX.setBackground(Color.lightGray);
        mLabelX.setFont(new Font("TimesRoman", Font.BOLD, 18));
        add(mLabelX);

        mTextX = new TextField(Float.toString(mBallPositionX));
        mTextX.setBounds(90, 220, 80, 25);
        mTextX.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mTextX);

        mTextX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    float newPosX = Float.parseFloat(mTextX.getText());
                    float ballR = (float)(mBallDiameter / (float)mSquareSize) / 2.0f;
                    if (newPosX > ballR && newPosX < 1.0f - ballR) {
                        mBallPositionX = Float.parseFloat(mTextX.getText());
                    }
                    else {
                        mTextX.setText(Float.toString(mBallPositionX));
                    }
                }
                catch(NumberFormatException e) {
                    mTextX.setText(Float.toString(mBallPositionX));
                }
            }
        });


        mLabelY = new Label("y:");
        mLabelY.setBounds(60, 250, 25, 25);
        mLabelY.setBackground(Color.lightGray);
        mLabelY.setFont(new Font("TimesRoman", Font.BOLD, 18));
        add(mLabelY);

        mTextY = new TextField(Float.toString(mBallPositionY));
        mTextY.setBounds(90, 250, 80, 25);
        mTextY.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mTextY);
        
        mTextY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    float newPosY = Float.parseFloat(mTextY.getText());
                    float ballR = (float)(mBallDiameter / (float)mSquareSize) / 2.0f;
                    if (newPosY > ballR && newPosY < 1.0f - ballR) {
                        mBallPositionY = Float.parseFloat(mTextY.getText());
                    }
                    else {
                        mTextY.setText(Float.toString(mBallPositionY));
                    }
                }
                catch(NumberFormatException e) {
                    mTextY.setText(Float.toString(mBallPositionY));
                }
            }
        });


        mLabelVX = new Label("vx:");
        mLabelVX.setBounds(200, 220, 35, 25);
        mLabelVX.setBackground(Color.lightGray);
        mLabelVX.setFont(new Font("TimesRoman", Font.BOLD, 18));
        add(mLabelVX);
        
        mTextVX = new TextField(Float.toString(mBallVelocityX));
        mTextVX.setBounds(240, 220, 80, 25);
        mTextVX.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mTextVX);

        mTextVX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    mBallVelocityX = Float.parseFloat(mTextVX.getText());
                }
                catch(NumberFormatException e) {
                    mTextVX.setText(Float.toString(mBallVelocityX));
                }
            }
        });


        mLabelVY = new Label("vy:");
        mLabelVY.setBounds(200, 250, 35, 25);
        mLabelVY.setBackground(Color.lightGray);
        mLabelVY.setFont(new Font("TimesRoman", Font.BOLD, 18));
        add(mLabelVY);
        
        mTextVY = new TextField(Float.toString(mBallVelocityY));
        mTextVY.setBounds(240, 250, 80, 25);
        mTextVY.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mTextVY);

        mTextVY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    mBallVelocityY = Float.parseFloat(mTextVY.getText());
                }
                catch(NumberFormatException e) {
                    mTextVY.setText(Float.toString(mBallVelocityY));
                }
            }
        });


        mLabelAlpha = new Label("a:");
        mLabelAlpha.setBounds(60, 290, 25, 25);
        mLabelAlpha.setBackground(Color.lightGray);
        mLabelAlpha.setFont(new Font("TimesRoman", Font.BOLD, 18));
        add(mLabelAlpha);

        mTextAlpha = new TextField(Float.toString(mAlpha));
        mTextAlpha.setBounds(90, 290, 80, 25);
        mTextAlpha.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mTextAlpha);
        
        mTextAlpha.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    mAlpha = Float.parseFloat(mTextAlpha.getText());
                    updateAcceleration();
                }
                catch(NumberFormatException e) {
                    mTextAlpha.setText(Float.toString(mAlpha));
                }
            }
        });


        mCheckboxShowArrows = new Checkbox(" Show velocity and acceleration", mShowArrows);
        mCheckboxShowArrows.setBounds(60, 340, 260, 25);
        mCheckboxShowArrows.setBackground(Color.lightGray);
        mCheckboxShowArrows.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mCheckboxShowArrows);

        mCheckboxShowArrows.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                mShowArrows = mCheckboxShowArrows.getState();
            }
        });


        mCheckboxTimeStopped = new Checkbox(" Stop time for simulation", mTimeStopped);
        mCheckboxTimeStopped.setBounds(60, 360, 260, 25);
        mCheckboxTimeStopped.setBackground(Color.lightGray);
        mCheckboxTimeStopped.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mCheckboxTimeStopped);

        mCheckboxTimeStopped.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                mTimeStopped = mCheckboxTimeStopped.getState();
            }
        });


        mCheckboxShowTail = new Checkbox(" Show tail", mShowTail);
        mCheckboxShowTail.setBounds(60, 380, 260, 25);
        mCheckboxShowTail.setBackground(Color.lightGray);
        mCheckboxShowTail.setFont(new Font("TimesRoman", Font.BOLD, 16));
        add(mCheckboxShowTail);

        mCheckboxShowTail.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                mShowTail = mCheckboxShowTail.getState();
                if (!mShowTail) {
                    mTail.clear();
                }
            }
        });


        mSimulationSpeedSlider = new Scrollbar(Scrollbar.HORIZONTAL, mSimulationSpeed, 1, 1, mMaxSimulationSpeed);
        mSimulationSpeedSlider.setBlockIncrement(1);
        mSimulationSpeedSlider.setUnitIncrement(1);
        mSimulationSpeedSlider.setBounds(60, 490, mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 60, 25);
        mSimulationSpeedSlider.setBackground(new Color(32, 16, 96));
		add(mSimulationSpeedSlider);

		mSimulationSpeedSlider.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent event) {
                mSimulationSpeed = mSimulationSpeedSlider.getValue();
            }
        });
    }

    public void drawUI(Graphics g) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, mWindowWidth - mSquareSize - 2 * mSquareMargin, mWindowHeight);

        g.setColor(Color.gray);
        g.fillRoundRect(0, 0, mWindowWidth - mSquareSize - 2 * mSquareMargin, mWindowHeight, 50, 50);

        /* Draw simulation name */
        {
            g.setColor(Color.lightGray);
            g.fillRoundRect(40, 50, mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 40, 35, 15, 15);

            g.setColor(Color.darkGray);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 24));

            FontMetrics fm = g.getFontMetrics();
            int x = 40 + (mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 40 - fm.stringWidth(mSimulationName)) / 2;
            int y = 50 + (35 - fm.getHeight()) + fm.getAscent();
            g.drawString(mSimulationName, x, y);
        }

        /* Draw simulation parameters header */
        {
            g.setColor(Color.lightGray);
            g.fillRoundRect(40, 150, mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 40, 270, 15, 15);

            String header = "Simulation parameters";
            
            g.setColor(Color.darkGray);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 20));

            FontMetrics fm = g.getFontMetrics();
            int x = 40 + (mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 40 - fm.stringWidth(header)) / 2;
            int y = 150 + (35 - fm.getHeight()) + fm.getAscent();
            g.drawString(header, x, y);
        }

        /* Draw simulation speed slider */
        {
            g.setColor(Color.lightGray);
            g.fillRoundRect(40, 440, mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 40, 90, 15, 15);

            String header = "Simulation speed";
            
            g.setColor(Color.darkGray);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 20));

            FontMetrics fm = g.getFontMetrics();
            int x = 40 + (mWindowWidth - mSquareSize - 2 * mSquareMargin - 2 * 40 - fm.stringWidth(header)) / 2;
            int y = 440 + (35 - fm.getHeight()) + fm.getAscent();
            g.drawString(header, x, y);
        }

        /* Draw simulation parameters values */
        if (!mTimeStopped)
        {
            mTextX.setText(Float.toString(mBallPositionX));
            mTextY.setText(Float.toString(mBallPositionY));
            mTextVX.setText(Float.toString(mBallVelocityX));
            mTextVY.setText(Float.toString(mBallVelocityY));
        }
    }

    /*
     * Override callbacks for mouse
     */
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {
        /* Return focus to the main window as soon as user clicked on it */
        requestFocusInWindow();
        if (e.getButton() == MouseEvent.BUTTON1) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            if (mouseX > mSquareXStart + mBallDiameter / 2 && mouseX < mWindowWidth - 1 - mSquareMargin - mBallDiameter / 2 &&
                mouseY > mSquareYStart + mBallDiameter / 2 && mouseY < mWindowHeight - 1 - mSquareMargin - mBallDiameter / 2) {

                mBallPositionX = (float)(mouseX - mSquareXStart) / (float)mSquareSize;
                mBallPositionY = (float)(mouseY - mSquareYStart) / (float)mSquareSize;
                mTextX.setText(Float.toString(mBallPositionX));
                mTextY.setText(Float.toString(mBallPositionY));

                updateAcceleration();
            }
        }
    }

    /*
     * Override callbacks for keyboard
     */
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            mShowArrows = !mShowArrows;
            mCheckboxShowArrows.setState(mShowArrows);
        }
        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            mTimeStopped = !mTimeStopped;
            mCheckboxTimeStopped.setState(mTimeStopped);
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (mSimulationSpeed > 1) {
                mSimulationSpeed--;
                mSimulationSpeedSlider.setValue(mSimulationSpeed);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (mSimulationSpeed < mMaxSimulationSpeed) {
                mSimulationSpeed++;
                mSimulationSpeedSlider.setValue(mSimulationSpeed);
            }
        }
    }





    /*
     *  Main function in case applet invoked by java directly
     */
    public static void main(String[] args) {
        Frame frame = new Frame("Ball Simulation");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                frame.dispose();
            }
        });

        BallSimulation app = new BallSimulation();
        app.init();
        frame.add(app);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
