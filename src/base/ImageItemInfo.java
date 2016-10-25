/*
 The reason i put the lines and strings together is to 
 record every drawing item (a line or string) by its original order.
 I could have done this by putting line and string classes into an object list
 but doing this results in boxing and unboxing.

 Through out the project, this will be used instead of a bunch of parameters.
 Too much parameters lead to confusion and it is easy to make a order mistake.
 I myself always put font size and font style in the wrong order.
 */
package base;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

public class ImageItemInfo implements Serializable, Comparable {

    //For transferring
    //System.Util.UUID

    public String id;
    /**
     * starts from zero, add one each time.
     */
    public int order;
    //Author's ip address.
    public String authorIP;
    public long createTime;

    //For both
    public Color color;
    public Point fromPoint;
    public ImageItemType type;

    //For string
    public String text;
    public Point point;
    public String fontname;
    public int fontStyle;
    public int fontsize;
    //For line
    public Point toPoint;
    //For drawing, invalid items are those without previous ones.
    //For example, item 5 is invalid when these is no item 4.
    public boolean isValid;
    public byte[] imageBytes;

    public enum ImageItemType {

        LINE,
        TEXT,
        IMAGE
    }

    public boolean getIsLocalItem() {
        return this.authorIP.equalsIgnoreCase(Config.getLocalIp());
    }

    @Override
    public int compareTo(Object t) {
        ImageItemInfo other = (ImageItemInfo) t;
        if (this.createTime > other.createTime) {
            return 1;
        } else if (this.createTime < other.createTime) {
            return -1;
        }
        return 0;
    }
}
