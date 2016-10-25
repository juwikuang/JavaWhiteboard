package base;

import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SimpleWhiteboardPanel extends JPanel {

    private BufferedImage image;

    public SimpleWhiteboardPanel(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = this.image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        this.setFocusable(true);
    }

    public Dimension getPreferredSize() {
        return (new Dimension(this.image.getWidth(), this.image.getHeight()));
    }

    public Point drawLine(ImageItemInfo itemInfo) {
        Graphics g = this.image.getGraphics();
        if (itemInfo.color != null) {
            g.setColor(itemInfo.color);
        }
        g.drawLine(itemInfo.fromPoint.x, itemInfo.fromPoint.y, itemInfo.toPoint.x, itemInfo.toPoint.y);
        this.repaint();
        return (new Point(itemInfo.toPoint));
    }

    private Point drawString(ImageItemInfo itemInfo) {
        Graphics g = this.image.getGraphics();
        g.setColor(itemInfo.color);
        g.setFont(new Font(itemInfo.fontname, itemInfo.fontStyle, itemInfo.fontsize));
        g.drawString(itemInfo.text, itemInfo.point.x, itemInfo.point.y);
        FontMetrics f = g.getFontMetrics();
        Point newPoint = new Point(itemInfo.point);
        newPoint.x += f.stringWidth(itemInfo.text);
        this.repaint();
        return (newPoint);
    }

    private Point drawImage(ImageItemInfo itemInfo) {
        InputStream in = new ByteArrayInputStream(itemInfo.imageBytes);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException ex) {
            Logger.getLogger(SimpleWhiteboardPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        Graphics g = this.image.getGraphics();
        g.drawImage(img, itemInfo.point.x, itemInfo.point.y, this);
        this.repaint();
        return (itemInfo.point);
    }

    public void paintComponent(Graphics graphics) {
        Graphics g = graphics.create();
        g.drawImage(this.image, 0, 0, null);
    }

    public void clear() {
        Graphics g = this.image.getGraphics();
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        this.repaint();
    }

    public Point drawItem(ImageItemInfo itemInfo) {
        switch (itemInfo.type) {
            case LINE:
                return drawLine(itemInfo);
            case TEXT:
                return drawString(itemInfo);
            case IMAGE:
                return drawImage(itemInfo);
            default:
                return itemInfo.point;
        }
    }
}
