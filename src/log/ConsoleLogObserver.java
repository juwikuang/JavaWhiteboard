/**
 * This is the console message observer.
 * Observer design pattern is used here.
 */

package log;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class ConsoleLogObserver implements Observer  {
    
    private String message = null;

    @Override
    public void update(Observable o, Object o1) {
        //Why java does not have a generic observable class?!
        //Update it when java has a generic observable class.
        LogObservable observable = (LogObservable) o;
        LogTarget[] targets = observable.getTargets();
        for (LogTarget t : targets) {
            if (t == LogTarget.CONSOLE) {
                this.message = observable.getMessage();
                System.out.println(this.message +" "+ (new Date()).toString());
            }
        }
    }
}