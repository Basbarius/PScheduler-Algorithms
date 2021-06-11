import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class AlgoVisualizer extends JFrame{
    DefaultTableModel historyTableModel = new DefaultTableModel();
    JTable historyTable = new JTable(historyTableModel);

    TableColumnModel columnModel = historyTable.getColumnModel();

    private int row = 0;
    private int n = 1;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel bodyPanel;
    private JTable processTable;
    private JComboBox algoSelector;
    private JTextField quantaSelector;
    private JPanel selectorPane;
    private JTextField quantumValueText;
    private JPanel buttonPanel;
    private JButton startButton;
    private JButton closeButton;
    private JProgressBar progressBar1;
    private JLabel waitingTimeLabel;
    private JLabel responseTimeLabel;
    private JLabel progressLabel;
    private JLabel percentageLabel;
    private JButton pauseButton;
    private JButton fillTableButton;
    private JScrollPane tableScrollPane;
    private JLabel timeValueLabel;
    private JTextField nProcessesValueText;
    private JLabel waitingValueLabel;
    private JLabel responseValueLabel;
    private JSlider timerSlider;
    private JScrollPane endTableScrollPane;
    private JTable rProcessTable;
    private JLabel nProcessesLabel;
    private JLabel quantumLabel;
    private JLabel headerLabel;
    private JLabel timerSpeedLabel;
    private JButton randomizeTableButton;
    private JButton helpButton;
    private JScrollPane tablePane;
    private JOptionPane end;

    private boolean isPlaying;
    private int timeCounter = 0;
    private int timeToSubtract = 0;
    private int timerValue = 1000;


    private int amountOfProcesses = 5;
    private final String[] tableColumnNamesWithoutPriority = {"PID", "Time left", "Current State"};
    private final String[] tableColumnNamesWithPriority = {"PID", "Time left","Priority", "Current State"};
    private Queue queue;
    private ArrayList<PCB> processes;
    private PCB currentProcess;
    private String currentAlgo = "Shortest-Job-First";
    private boolean skipProcess = false;
    DecimalFormat format = new DecimalFormat("####.##");

    public AlgoVisualizer(){
        //set the number of Columns of the table
        historyTableModel.setColumnCount(0);

        //display main window
        add(mainPanel);
        setTitle("Algorithm Visualizer");
        setSize(940, 400);
        //pack();
        setLocationRelativeTo(null);

        //hide quantum by default
        quantumLabel.setVisible(false);
        quantumValueText.setVisible(false);

        String texto = "Help";
        helpButton.setToolTipText(texto);

        //setup action listeners to components
        startButton.addActionListener(startTimer);
        fillTableButton.addActionListener(fillTable);
        randomizeTableButton.addActionListener(fillTable);
        algoSelector.addActionListener(algoSelectorListener);
        closeButton.addActionListener(closeButtonListener);
        pauseButton.addActionListener(pauseButtonListener);
        helpButton.addActionListener(helpButtonListener);

    }

    //motor of the whole process, only executes when timer hits 0
    //based on algorithm, handles process time, state and the current process
    ActionListener timeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //sets up timer in window
            //timeValueLabel.setText(Integer.toString(timeCounter));
            timeCounter++;

            //handles process based on scheduling algorithm
            handleProcess();
            if(currentProcess == null) return;

            //update bar according to process completion
            updateBar(currentProcess);

            //redraw the table based on changes from arraylist
            drawTable();

            //handle waiting and response times
            handleWaitingAndResponse();

            //terminate process
            if(currentProcess.timeLeft <= 0){
                currentProcess.currentState = "Terminated";
                handleHistoryTable();
                if(allProcessesTerminated()){
                    timer.stop();
                    drawTable();
                    enableEditing();
                    JOptionPane.showMessageDialog(null, "All processes ended\n" +
                            "Elapsed time: " + timeCounter + " seconds");
                }
            }
        }
    };

    //handles different algorithm processes
    private void handleProcess(){
        //if a process must be dispatched due to multiple reasons, evaluation is true
        boolean dispatchNeeded = currentProcess == null || currentProcess.currentState.equals("Terminated") || skipProcess;
        if(dispatchNeeded){
            if(currentAlgo.equals("Shortest-Job-First"))
                currentProcess = queue.dequeueShortest();
            else if(currentAlgo.equals("Priority"))
                currentProcess = queue.dequeuePriority();
            else {
                skipProcess = false;
                //change state from waiting to ready
                if(currentProcess != null && currentProcess.currentState.equals("Waiting"))
                    currentProcess.currentState = "Ready";
                currentProcess = queue.dequeue();
                timeToSubtract = Integer.parseInt(quantumValueText.getText());
                if(timeToSubtract > currentProcess.timeLeft) timeToSubtract = currentProcess.timeLeft;
            }
            if(currentProcess == null){
                timer.stop();
                return;
            }
            currentProcess.currentState = "Running";
            currentProcess.hasBeenAttended = true;
        }
        if(currentProcess.timeLeft > 0)
            currentProcess.timeLeft--;
        if(currentAlgo.equals("Round-Robin")){
            timeToSubtract--;
            //when quantum is reached and there is still time left
            if(timeToSubtract == 0 && currentProcess.timeLeft != 0){
                currentProcess.currentState = "Waiting";
                queue.enqueue(currentProcess);
                skipProcess = true;
            }
        }
    }

    private void handleWaitingAndResponse(){
        int sumWaiting = 0, sumResponse = 0;
        for (PCB process : processes) {
            if (process.currentState.equals("Ready")) {
                process.waitingTime += 1;
                if (!process.hasBeenAttended) {
                    process.responseTime++;
                }
            }
            sumWaiting += process.waitingTime;
            sumResponse += process.responseTime;
        }

        double averageWaitingTime = (double)sumWaiting / processes.size();
        double averageResponseTime = (double)sumResponse / processes.size();
        waitingValueLabel.setText(format.format(averageWaitingTime));
        responseValueLabel.setText(format.format(averageResponseTime));
    }

    private void updateBar(PCB process){
        //calculate percentage complete based on original time and current time left
        double value = (1 - (double)process.timeLeft/process.originalTime) * 100;
        percentageLabel.setText((int) value + "%");
        progressBar1.setValue((int)value);
    }

    private void handleHistoryTable(){
        int previousPID = 0;
        if (row != 0) {
            previousPID = (int) historyTableModel.getValueAt(row - 1, 1);
            //System.out.println("PID: " + previousPID + ", row: " + row);
        }
        if(row == 0 || currentProcess.getPID() != previousPID){
            if (currentAlgo.equals("Shortest-Job-First") | currentAlgo.equals("Round-Robin")) {
                historyTableModel.insertRow(row, new Object[]{n, currentProcess.getPID(), currentProcess.getCurrentState()});
            } else {
                historyTableModel.insertRow(row, new Object[]{n, currentProcess.getPID(), currentProcess.getPriority(), currentProcess.getCurrentState()});
            }
            row++;
            n++;
        }
    }

    Timer timer = new Timer(timerValue, timeListener);

    ActionListener startTimer = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(queue == null) return;
            if(allProcessesTerminated()){
                JOptionPane.showMessageDialog(null, "Fill the table with new processes to be able to start");
            }
            timerValue = timerSlider.getValue();
            timer.setDelay(timerValue);
            //timer = new Timer(timerValue, timeListener);
            isPlaying = true;
            timer.start();
            disableEditing();
        }
    };


    ActionListener closeButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            mainPanel.setVisible(false);
            dispose();
        }
    };

    ActionListener pauseButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            isPlaying = false;
            timer.stop();
            enableEditing();
        }
    };

    //fill both the PCB table and the process queue, also defines runnableProcesses
    ActionListener fillTable = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            if (currentAlgo.equals("Shortest-Job-First") | currentAlgo.equals("Round-Robin")){
                setHistoryTableModelNormal();
            } else {
                setHistoryTableModelPriority();
            }
            //reset timer and processes
            row = 0;
            n = 1;
            currentProcess = null;
            isPlaying = false;
            timer.stop();
            timeCounter = 0;
            progressBar1.setValue(0);
            //create new processes
            Random random = new Random();
            processes = new ArrayList<>();
            queue = new Queue();
            enableEditing();

            JButton state = (JButton) e.getSource();
            amountOfProcesses = Integer.parseInt(nProcessesValueText.getText());
            int nToUse = amountOfProcesses;
            if(state ==  randomizeTableButton) nToUse = random.nextInt(7) + 3;
            for(int i=0; i<nToUse; i++){
                PCB process = new PCB(random.nextInt(10000), random.nextInt(30) + 1, random.nextInt(4) + 1);
                processes.add(process);
                queue.enqueue(process);
            }
            drawTable();

            int rowCount = historyTableModel.getRowCount();
            //Remove rows one by one from the end of the table
            for (int i = rowCount-1; i >= 0; i--) {
                historyTableModel.removeRow(i);
            }
        }
    };

    //updates the table based on the values of the PCB arraylist
    private void drawTable(){
        //contents = queue.toTableFormat();
        boolean algoIsPriorityQueue = currentAlgo.equals("Priority");
        String[] columnsToUse = tableColumnNamesWithoutPriority;
        if(algoIsPriorityQueue) columnsToUse = tableColumnNamesWithPriority;

        String[][] contents = new String[processes.size()][columnsToUse.length];
        for(int i=0;i<processes.size();i++){
            if(algoIsPriorityQueue)
                contents[i] = processes.get(i).toStringArrayWithPriority();
            else
                contents[i] = processes.get(i).toStringArrayWithoutPriority();
        }
        processTable = new JTable(contents, columnsToUse);
        processTable.setFillsViewportHeight(true);
        tableScrollPane.setViewportView(processTable);
    }

    //check if all processes have a terminated state
    private boolean allProcessesTerminated(){
        for (PCB process : processes) {
            if (!process.currentState.equals("Terminated")) {
                return false;
            }
        }
        return true;
    }

    //in charge of changing algorithm when combobox selected
    ActionListener algoSelectorListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox state = (JComboBox) e.getSource();
            currentAlgo = state.getSelectedItem().toString();

            //hide quantum
            if(currentAlgo.equals("Round-Robin")){
                quantumLabel.setVisible(true);
                quantumValueText.setVisible(true);
            }
            else{
                quantumLabel.setVisible(false);
                quantumValueText.setVisible(false);
            }

            if (currentAlgo.equals("Shortest-Job-First") | currentAlgo.equals("Round-Robin")){
                setHistoryTableModelNormal();
            } else {
                setHistoryTableModelPriority();
            }
        }
    };

    ActionListener helpButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            MenuVis menu = new MenuVis();
            menu.setVisible(true);
        }
    };

    private void enableEditing(){
        quantumValueText.setEditable(true);
        nProcessesValueText.setEditable(true);
        timerSlider.setEnabled(true);
        algoSelector.setEnabled(true);
    }

    private void disableEditing(){
        quantumValueText.setEditable(false);
        nProcessesValueText.setEditable(false);
        timerSlider.setEnabled(false);
        algoSelector.setEnabled(false);
    }

    private void setHistoryTableModelNormal(){
        historyTableModel.setColumnCount(0);
        historyTableModel.addColumn("#");
        historyTableModel.addColumn("PID");
        historyTableModel.addColumn("Current State");
        rProcessTable.setFillsViewportHeight(true);
        endTableScrollPane.setViewportView(historyTable);

        //Size of the column
        columnModel.getColumn(0).setPreferredWidth(30);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(150);

        int rowCount = historyTableModel.getRowCount();
        //Remove rows one by one from the end of the table
        for (int i = rowCount-1; i >= 0; i--) {
            historyTableModel.removeRow(i);
        }
    }

    private void setHistoryTableModelPriority(){
        historyTableModel.setColumnCount(0);
        historyTableModel.addColumn("#");
        historyTableModel.addColumn("PID");
        historyTableModel.addColumn("Priority");
        historyTableModel.addColumn("Current State");
        rProcessTable.setFillsViewportHeight(true);
        endTableScrollPane.setViewportView(historyTable);

        //Size of the column
        columnModel.getColumn(0).setPreferredWidth(30);
        columnModel.getColumn(1).setPreferredWidth(150);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(150);

        int rowCount = historyTableModel.getRowCount();
        //Remove rows one by one from the end of the table
        for (int i = rowCount-1; i >= 0; i--) {
            historyTableModel.removeRow(i);
        }
    }
}
