package log;

public enum LogTarget {

    FILE(1),
    CONSOLE(1<<1),
    GUI(1<<2),
    MESSAGE_BOX(1<<3);

    private final int value;

    LogTarget(LogTarget ... args) {
        int result = 0;
        for(LogTarget i : args){
            result = (result | i.getValue());
        }
        this.value = result;
    }

    LogTarget(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean contains(LogTarget target) {
        return (this.value & target.getValue()) == target.getValue();
    }
}
