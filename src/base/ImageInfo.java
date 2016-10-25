/*
 ImageInfo and ImageItemInfo are used
 to record drawing actions.
 These are crucial if we want to redraw the current whiteboard content.

 Author 100136054
 */
package base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import thirdPart.ClockSync;
import java.util.UUID;
import log.LogObservable;
import log.LogTarget;

public class ImageInfo implements Serializable {

    private ArrayList<ImageItemInfo> items = new ArrayList<ImageItemInfo>();
    public String id;
    private long createTime = 0;
    //public long lastUpdateTime = 0;
    private ArrayList<String> retryIps = new ArrayList<String>();

    public ImageInfo(long time) {
        id = UUID.randomUUID().toString();
        createTime = time;
        //lastUpdateTime = createTime;
        LogObservable.setMessage("Image created." + id + "," + createTime, LogTarget.CONSOLE);
    }

    /**
     * the image dispatcher points at this class. Assigning a value to an
     * ImageInfo variable breaks the linkage between the dispatcher and the
     * imageInfo instance.
     *
     * @param newImageInfo
     */
    public void update(ImageInfo newImageInfo) {
        retryIps.clear();
        if (this.createTime == 0) {
            this.createTime = newImageInfo.createTime;
        }
        //do not accept old images.
        if (this.createTime > newImageInfo.createTime) {
            return;
        }

        if (this.id.equalsIgnoreCase(newImageInfo.id)) {
            //the same image
            for (ImageItemInfo item : newImageInfo.items) {
                addItem(item);
            }
            //lastUpdateTime = ClockSync.getAdjustedTime();
        } else {
            //different image
            this.items.clear();
            this.clone(newImageInfo);
        }
    }

    public ImageItemInfo getItem(int index) {
        return items.get(index);
    }

    public void addItem(ImageItemInfo item) {
        if (this.createTime == 0) {
            this.createTime = ClockSync.getAdjustedTime();
        }

        if (this.itemExists(item.id)) {
            return;
        }

        if (item.id == null) {
            item.id = UUID.randomUUID().toString();
        }
        if (item.authorIP == null) {
            item.authorIP = Config.getLocalIp();
        }
        if (item.getIsLocalItem() && item.order == 0) {
            item.order = getLastItemOrder()+1;
        }

        if (item.order == 0) {
            item.isValid = true;
        } else {
            //the validity is determined by the existance of the previous item.
            ImageItemInfo previousItem = getItem(item.authorIP, item.order-1);
            if(previousItem!=null){
                item.isValid = previousItem.isValid;
            }
            else{
                item.isValid=false;
            }
        }
        
        //set the retry IPs
        if(item.isValid ){
            retryIps.remove(item.authorIP);
        }else{
            retryIps.remove(item.authorIP);
            retryIps.add(item.authorIP);
        }
        
        items.add(item);
        Collections.sort(items);
        //Following items by the same author have the same validity.
        ImageItemInfo nextItem = getItem(item.authorIP, item.order+1);
        while(nextItem!=null){
            nextItem.isValid=item.isValid;
            nextItem = getItem(nextItem.authorIP, nextItem.order+1);
        }
    }

    public boolean itemExists(String id) {
        ImageItemInfo item = getItem(id);
        return item != null;
    }

    public boolean itemExists(String authorIP, int order) {
        ImageItemInfo item = getItem(authorIP, order);
        return item != null;
    }

    public ImageItemInfo getItem(String id) {
        for (ImageItemInfo item : items) {
            if (item.id.equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }

    public ImageItemInfo getItem(String authorIP, int order) {
        for (ImageItemInfo item : items) {
            if (item.authorIP.equalsIgnoreCase(authorIP) && item.order == order) {
                return item;
            }
        }
        return null;
    }

    public int getLastItemOrder() {
        int lastOrder = -1;
        for (ImageItemInfo item : items) {
            if (!item.getIsLocalItem()) {
                continue;
            }
            lastOrder = IntUtil.max(lastOrder, item.order);
        }
        return lastOrder;
    }

    public int countItems() {
        return items.size();
    }

    public ArrayList<ImageItemInfo> getItems() {
        return items;
    }

    public void clearItems() {
        items.clear();
    }

    public void clone(ImageInfo newImageInfo) {
        this.id = newImageInfo.id;
        this.createTime = newImageInfo.createTime;
        //this.lastUpdateTime = newImageInfo.lastUpdateTime;
        this.items = newImageInfo.items;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    Iterable<String> getRetryIPs() {
        return this.retryIps;
    }
}
