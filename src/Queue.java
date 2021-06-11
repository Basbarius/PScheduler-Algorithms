public class Queue {
    private PCB end;
    private PCB start;
    private int length;

    public Queue(){
        length = 0;
    }

    public boolean isEmpty(){
        return end == null;
    }

    public void enqueue(PCB node){
        length++;
        if(end == null){
            start = node;
            end = node;
            return;
        }
        end.programCounter = node;
        end = node;
    }

    public PCB dequeue(){
        if(isEmpty()) return null;
        length--;
        PCB node = start;
        if(end == start){
            end = null;
            start = null;
            return node;
        }
        start = node.programCounter;
        return node;
    }

    public PCB dequeueShortest(){
        if(isEmpty()) return null;
        length--;
        PCB node = start;
        if(end == start){
            end = null;
            start = null;
            return node;
        }
        int shortestValue = 10000;
        while(node != null){
            if(node.timeLeft < shortestValue) shortestValue = node.timeLeft;
            node = node.programCounter;
        }
        node = start;
        PCB last = null;
        while(node.timeLeft != shortestValue){
            last = node;
            node = node.programCounter;
        }
        if(node == start){
            start = node.programCounter;
            return node;
        }
        else if(node == end){
            end = last;
            return node;
        }
        else{
            last.programCounter = node.programCounter;
            return node;
        }
    }

    public PCB dequeuePriority(){
        if(isEmpty()) return null;
        length--;
        PCB node = start;
        if(end == start){
            end = null;
            start = null;
            return node;
        }
        int maxPriority = 100000;
        while(node != null){
            if(node.priority < maxPriority) maxPriority = node.priority;
            node = node.programCounter;
        }
        node = start;
        PCB last = null;
        while(node.priority != maxPriority){
            last = node;
            node = node.programCounter;
        }
        if(node == start){
            start = node.programCounter;
            return node;
        }
        else if(node == end){
            end = last;
            return node;
        }
        else{
            last.programCounter = node.programCounter;
            return node;
        }

    }

    public String[][] toTableFormat(){
        String[][] contents = new String[length][4];
        PCB index = start;
        for(int i=0;i<length;i++){
            contents[i] = index.toStringArrayWithoutPriority();
            index = index.programCounter;
        }
        return contents;
    }

    public void printQueue(){
        PCB index = start;
        while(index != null){
            System.out.println(index.pID);
            index = index.programCounter;
        }
    }
}
