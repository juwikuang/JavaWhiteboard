/*
 Slowly redraw an image.
 */
package base;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RedrawThread extends Thread {

    ImageInfo imageInfo;
    int interval;

    /**
     * 
     * @param image the image to be redraw
     * @param interval the speed
     */
    public RedrawThread(ImageInfo image, int interval) {
        this.imageInfo = image;
        this.interval = interval;
    }

    @Override
    public void run() {
        Config.panel.clear();
        for (int i = 0; i < imageInfo.countItems(); i++) {
            try {
                //ignore invalid items.
                if(!imageInfo.getItem(i).isValid)
                {
                    continue;
                }
                SimpleWhiteboard.getInstance().drawItem(imageInfo.getItem(i));
                Thread.sleep(interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(RedrawThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
