/**
 * This is the GUI log printer.
 * Observer design pattern is used here.
 */
package log;

import base.SimpleWhiteboard;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class GuiLogObserver implements Observer {

    private String message = null;

    @Override
    public void update(Observable o, Object o1) {
        //Why java does not have a generic observable class?!
        //Update it when java has a generic observable class.
        LogObservable observable = (LogObservable) o;
        LogTarget[] targets = observable.getTargets();
        for (LogTarget t : targets) {
            if (t == LogTarget.GUI) {
                this.message = observable.getMessage();
                SimpleWhiteboard.getInstance().UpdateMessage(this.message +" "+ (new Date()).toString());
            }
        }
    }
}
