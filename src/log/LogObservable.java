/**
 * This is the log centre.
 * Log or error Message (not UDP message or TPC message)
 * are centralised here
 * before distributed to different observers.
 * Observer design pattern is used here.
 */
package log;

import java.util.Observable;

public class LogObservable extends Observable {

    //Fields
    private String message;
    private LogTarget[] targets;

    //Constructor and singleton
    private LogObservable() {

    }

    private static LogObservable instance = null;

    public static LogObservable getInstance() {
        if (instance == null) {
            instance = new LogObservable();
        }
        return instance;
    }

    //Methods
    public static void setMessage(String message, LogTarget... targets) {
        LogObservable.getInstance().message = message;
        LogObservable.getInstance().targets = targets;
        LogObservable.getInstance().setChanged();
        LogObservable.getInstance().notifyObservers();
    }

    public static String getMessage() {
        return LogObservable.getInstance().message;
    }

    public static LogTarget[] getTargets() {
        return LogObservable.getInstance().targets;
    }

}
