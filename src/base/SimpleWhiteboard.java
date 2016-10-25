package base;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import log.LogObservable;
import log.LogTarget;
import thirdPart.ClockSync;
import thirdPart.JFontChooser;

enum DrawMode {

    LINE, TEXT, IMAGE;
}

class SimpleWhiteboardControls extends JPanel implements ActionListener,
        MouseListener, KeyListener {

    private final SimpleWhiteboardPanel panel;
    private final JComboBox drawModeComboBox;
    //Buttons
    private final JButton colorButton;
    private final JButton fontButton;
    private final JButton newButton;
    private final JButton redrawButton;
    private final JLabel statusBar;
    private static final String[] drawModeName = {"line", "text", "image"};
    private DrawMode drawMode;
    private Color color;
    private Point point;
    //Font paramters
    private String fontname;
    private int fontStyle;
    private int fontSize;

    public SimpleWhiteboardControls(SimpleWhiteboardPanel simpleWhiteboardPanel) {
        super(new GridLayout(2, 2));

        this.panel = simpleWhiteboardPanel;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel pane1 = new JPanel();
        this.add(pane1);
        JPanel pane2 = new JPanel();
        this.add(pane2);

        //Status bar
        statusBar = new JLabel("Welcome");
        pane2.add(statusBar, BorderLayout.SOUTH);
        //drawMode
        this.drawMode = DrawMode.LINE;
        this.drawModeComboBox = new JComboBox(drawModeName);
        this.drawModeComboBox.addActionListener(this);
        pane1.add(this.drawModeComboBox);
        //buttons
        //color button
        this.colorButton = new JButton("Set Color");
        this.colorButton.addActionListener(this);
        pane1.add(this.colorButton);
        //create the font button
        this.fontButton = new JButton("Set Font");
        this.fontButton.addActionListener(this);
        pane1.add(this.fontButton);
        //create the clear button
        this.newButton = new JButton("New");
        this.newButton.addActionListener(this);
        pane1.add(this.newButton);
        //create the redraw button
        this.redrawButton = new JButton("Redraw");
        this.redrawButton.addActionListener(this);
        pane1.add(this.redrawButton);

        this.syncState();
        this.panel.addMouseListener(this);
        this.panel.addKeyListener(this);
        this.color = Color.BLACK;
        this.fontname = "Monospaced";
        this.fontStyle = Font.PLAIN;
        this.fontSize = 20;
    }

    public void drawLine(Point newPoint) {
        if (this.point == null) {
            this.point = newPoint;
        } else {
            ImageItemInfo lineInfo = new ImageItemInfo();
            lineInfo.type = ImageItemInfo.ImageItemType.LINE;
            lineInfo.createTime = ClockSync.getAdjustedTime();
            lineInfo.fromPoint = this.point;

            lineInfo.toPoint = newPoint;
            lineInfo.color = this.color;
            this.point = this.panel.drawItem(lineInfo);
            Config.imageInfo.addItem(lineInfo);

            imageUpdated();
        }
    }

    public void drawString(final String s) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    drawString(s);
                }
            });
        }

        if (this.point != null) {
            ImageItemInfo textInfo = new ImageItemInfo();
            textInfo.type = ImageItemInfo.ImageItemType.TEXT;
            textInfo.createTime = ClockSync.getAdjustedTime();
            textInfo.text = s;
            textInfo.point = this.point;
            textInfo.fontname = this.fontname;
            textInfo.fontsize = this.fontSize;
            textInfo.fontStyle = this.fontStyle;
            textInfo.color = this.color;
            this.point = this.panel.drawItem(textInfo);
            Config.imageInfo.addItem(textInfo);
            imageUpdated();
        }
    }

    private void drawImage(final Point newPoint, final byte[] imgBytes) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    drawImage(newPoint, imgBytes);
                }
            });
        }

        this.point = newPoint;
        ImageItemInfo picInfo = new ImageItemInfo();
        picInfo.type = ImageItemInfo.ImageItemType.IMAGE;
        picInfo.createTime = ClockSync.getAdjustedTime();
        picInfo.point = this.point;
        picInfo.imageBytes = imgBytes;
        this.point = this.panel.drawItem(picInfo);
        Config.imageInfo.addItem(picInfo);
        imageUpdated();
    }

    private void imageUpdated() {
        LogObservable.setMessage("Image Updated", LogTarget.CONSOLE, LogTarget.FILE);
        if (!Config.getIsConnected()) {
            return;
        }
        try {
            MulticastingUtil.send("ImageUpdated:" + Config.getLocalIp() + ":" + Long.toString(Config.imageInfo.getCreateTime()));
        } catch (Exception ex) {
            Logger.getLogger(SimpleWhiteboardControls.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    public void syncState() {
        switch (this.drawMode) {
            case LINE:
                this.drawModeComboBox.setSelectedIndex(0);
                break;
            case TEXT:
                this.drawModeComboBox.setSelectedIndex(1);
                break;
            case IMAGE:
                this.drawModeComboBox.setSelectedIndex(2);
                break;
            default:
                throw new RuntimeException("unknown draw mode");
        }
    }

    private void drawModeActionPerformed(ActionEvent actionEvent) {
        String cmd = (String) this.drawModeComboBox.getSelectedItem();
        if (cmd.equals("line")) {
            this.drawMode = DrawMode.LINE;
        } else if (cmd.equals("text")) {
            this.drawMode = DrawMode.TEXT;
        } else if (cmd.equals("image")) {
            this.drawMode = DrawMode.IMAGE;
        } else {
            throw new RuntimeException(String.format("unknown command: %s", cmd));
        }
    }

    private void colorActionPerformed(ActionEvent actionEvent) {
        color = JColorChooser.showDialog(this.panel,
                "choose colour", this.color);
        if (color != null) {
            this.color = color;
        }
    }

    private void fontActionPerformed(ActionEvent actionEvent) {
        JFontChooser chooser = new JFontChooser();
        chooser.showDialog(this);
        Font textFont = chooser.getSelectedFont();
        this.fontname = textFont.getFontName();
        this.fontStyle = textFont.getStyle();
        this.fontSize = textFont.getSize();
    }

    private void newActionPerformed(ActionEvent actionEvent) {
        newImage();
    }

    private void redrawActionPerformed(ActionEvent actionEvent) {
        RedrawThread at = new RedrawThread(Config.imageInfo, 500);
        at.start();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.drawModeComboBox) {
            this.drawModeActionPerformed(actionEvent);
        } else if (actionEvent.getSource() == this.colorButton) {
            this.colorActionPerformed(actionEvent);
        } else if (actionEvent.getSource() == this.fontButton) {
            this.fontActionPerformed(actionEvent);
        } else if (actionEvent.getSource() == this.newButton) {
            this.newActionPerformed(actionEvent);
        } else if (actionEvent.getSource() == this.redrawButton) {
            this.redrawActionPerformed(actionEvent);
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        switch (this.drawMode) {
            case TEXT:
                String s = Character.toString(keyEvent.getKeyChar());
                this.drawString(s);
                break;
            default:
                // ignore event if not in text mode
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        // make sure panel gets focus when clicked
        this.panel.requestFocusInWindow();
        Point newPoint = mouseEvent.getPoint();
        switch (this.drawMode) {
            case TEXT:
                this.point = newPoint;
                break;
            case LINE:
                switch (mouseEvent.getButton()) {
                    case MouseEvent.BUTTON1:
                        this.drawLine(newPoint);
                        break;
                    case MouseEvent.BUTTON3:
                        this.point = null;
                        break;
                    default:
                        System.err.println(String.format("got mouse button %d",
                                mouseEvent.getButton()));
                        break;
                }
                break;
            case IMAGE:
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
                
                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage originalImage = ImageIO.read(fc.getSelectedFile());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(originalImage, "jpg", baos);
                        baos.flush();
                        byte[] imageInByte = baos.toByteArray();
                        baos.close();
                        this.drawImage(newPoint, imageInByte);
                    } catch (IOException ex) {
                        Logger.getLogger(SimpleWhiteboardControls.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                break;
            default:
                throw new RuntimeException("unknown drawing mode");
        }
    }

    /**
     * It is not needed to distinguish draw string and draw line until the very
     * bottom level has been reached.
     * @param stringInfo
     */
    public void drawItem(final ImageItemInfo stringInfo) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    drawItem(stringInfo);
                }
            });
        }

        try {
            this.panel.drawItem(stringInfo);
        } catch (Exception ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
            throw ex;
        } finally {

        }
    }

    public void clear() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.clear();
                    Config.imageInfo.clearItems();
                }
            });
        }
        panel.clear();
        Config.imageInfo.clearItems();
    }

    public void update(final ImageInfo info) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update(info);
                }
            });
        }

        panel.clear();

        try {
            Config.imageInfo.update(info);
            for (ImageItemInfo itemInfo : Config.imageInfo.getItems()) {
                if (itemInfo.isValid) {
                    Config.panel.drawItem(itemInfo);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
            throw ex;
        } finally {

        }
    }

    void updateMessage(final String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateMessage(message);
                }
            });
        }

        String[] messageLines = message.split("\r\n");
        String printedMessage = "<html>";
        for (String line : messageLines) {
            printedMessage += line + "<br>";
        }
        printedMessage = printedMessage.substring(0, printedMessage.length() - 4);
        printedMessage += "</html>";
        this.statusBar.setText(printedMessage);
    }

    private void newImage() {
        clear();
        Config.imageInfo.update(new ImageInfo(ClockSync.getAdjustedTime()));
    }

}

class SimpleWhiteboardMenuActionListener implements ActionListener {

    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();
        System.err.println(String.format("menu action: %s", actionCommand));
        switch (actionCommand) {
            case "Connect":
                connect();
                break;
            case "Disconnect":
                LogObservable.setMessage("Disconnected", LogTarget.CONSOLE, LogTarget.FILE);
                try {
                    MulticastingUtil.send("Leave:" + Config.getLocalIp());
                    Config.setIsConnected(false);
                    Config.setIpList(new ArrayList<String>() {
                    });
                    Config.addIp(Config.getLocalIp());
                } catch (Exception ex) {
                    Logger.getLogger(SimpleWhiteboardMenuActionListener.class.
                            getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "IP Table":
                String ipString = StringUtil.join("\r\n", Config.getIpList());
                JOptionPane.showMessageDialog(null, ipString, "Connected Users",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Open":
                open();
                break;
            case "Save":
                save();
                break;
        }
    }

    private void save() {
        ObjectOutputStream osw = null;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Whiteboard files",
                    "wb"));
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                osw = new ObjectOutputStream(new FileOutputStream(fc.
                        getSelectedFile().getAbsolutePath()));
                osw.writeObject(Config.imageInfo);
                osw.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            try {
                osw.close();
            } catch (IOException ex) {
                //cannot check if osw is closed or not.
            }
        }
    }

    private void open() {
        ObjectInputStream ois = null;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Whiteboard files",
                    "wb"));
            fc.showOpenDialog(null);
            ois = new ObjectInputStream(new FileInputStream(
                    fc.getSelectedFile().getAbsolutePath()));
            Config.imageInfo.update((ImageInfo) ois.readObject());
            SimpleWhiteboard.getInstance().Update(Config.imageInfo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
        } finally {
            try {
                ois.close();
            } catch (IOException ex) {
                Logger.getLogger(SimpleWhiteboardMenuActionListener.class.
                        getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void connect() {

        try {

            LogObservable.setMessage("Connnecting", LogTarget.CONSOLE, LogTarget.FILE);
            MulticastingUtil.send("Join:" + Config.getLocalIp());
            Config.setIsConnected(true);
        } catch (Exception ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).
                    log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, this, "Failed to connect", JOptionPane.OK_OPTION);
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        String serverIp = null;
        for (String ip : Config.getIpList()) {
            if (ip.equalsIgnoreCase(Config.getLocalIp())) {
                continue;
            }
            serverIp = ip;
            break;
        }

        if (serverIp != null) {
            ImageRequester.Request(serverIp, false);
            SimpleWhiteboard.getInstance().Update(Config.imageInfo);
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            MulticastingUtil.send("ImageUpdated:" + Config.getLocalIp() + ":" + Long.toString(Config.imageInfo.getCreateTime()));
        } catch (Exception ex) {
            Logger.getLogger(SimpleWhiteboardMenuActionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/**
 * Singleton is used here. SimpleWhiteboard class is also the interface for
 * SimpleWhiteboardPanel and SimpleWhiteboardControls.
 *
 */
public class SimpleWhiteboard extends JFrame {

    private static SimpleWhiteboard instance = null;

    protected SimpleWhiteboard() {
        // Exists only to defeat instantiation.
    }

    public static SimpleWhiteboard getInstance() {
        if (instance == null) {
            instance = new SimpleWhiteboard(1000, 600);
        }
        return instance;
    }

    private JScrollPane scrollPane;
    private SimpleWhiteboardPanel simpleWhiteboardPanel;
    private SimpleWhiteboardControls controls;
    private JMenuBar menuBar;
    private SimpleWhiteboardMenuActionListener menuActionListener;

    protected SimpleWhiteboard(int width, int height) {
        super(String.format("<100136054> %s %s", Config.getAppName(), Config.getLocalIp()));

        this.simpleWhiteboardPanel = new SimpleWhiteboardPanel(width, height);
        this.scrollPane = new JScrollPane(this.simpleWhiteboardPanel);
        Config.panel = this.simpleWhiteboardPanel;
        this.getContentPane().add(this.scrollPane);
        this.controls = new SimpleWhiteboardControls(
                this.simpleWhiteboardPanel
        );
        if (!Config.isMonitor) {
            this.getContentPane().add(this.controls,
                    BorderLayout.SOUTH);
        }

        this.menuBar = new JMenuBar();
        this.menuActionListener = new SimpleWhiteboardMenuActionListener();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(this.menuActionListener);
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this.menuActionListener);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        this.menuBar.add(fileMenu);

        JMenu networkMenu = new JMenu("Network");
        JMenuItem connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(this.menuActionListener);
        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(this.menuActionListener);
        JMenuItem ipTableItem = new JMenuItem("IP Table");
        ipTableItem.addActionListener(menuActionListener);
        networkMenu.add(connectItem);
        networkMenu.add(disconnectItem);
        networkMenu.add(ipTableItem);
        this.menuBar.add(networkMenu);
        this.setJMenuBar(this.menuBar);
    }

    public void clear() {
        this.controls.clear();
    }

    public void Update(ImageInfo info) {
        this.controls.update(info);
    }

    public void drawItem(ImageItemInfo info) {
        this.controls.drawItem(info);
    }

    public SimpleWhiteboardPanel getWhiteboardPanel() {
        return (this.simpleWhiteboardPanel);
    }

    public void UpdateMessage(String message) {
        this.controls.updateMessage(message);
    }
}
