public class PCB {
    int pID;
    int timeLeft;
    int originalTime;
    int priority;
    int waitingTime;
    int responseTime;
    boolean hasBeenAttended;
    String currentState;
    PCB programCounter;

    public PCB(int pID, int timeLeft, int priority) {
        this.pID = pID;
        this.timeLeft = timeLeft;
        originalTime = timeLeft;
        this.priority = priority;
        this.currentState = "Ready";
        this.programCounter = null;
        waitingTime = 0;
        responseTime = 0;
        hasBeenAttended = false;
    }

    public String[] toStringArrayWithoutPriority(){
        String[]  array = new String[3];
        array[0] = Integer.toString(pID);
        array[1] = Integer.toString(timeLeft);
        array[2] = currentState;
        return array;
    }

    public String[] toStringArrayWithPriority(){
        String[]  array = new String[4];
        array[0] = Integer.toString(pID);
        array[1] = Integer.toString(timeLeft);
        array[2] = Integer.toString(priority);
        array[3] = currentState;
        return array;
    }

    public int getPID() {
        return pID;
    }

    public int getPriority(){
        return priority;
    }

    public String getCurrentState(){
        return currentState;
    }
}
