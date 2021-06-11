import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuVis extends JFrame {
    private JPanel panel1;
    private JLabel l1;
    private JLabel l2;
    private JLabel l3;
    private JLabel l4;
    private JLabel l5;
    private JLabel l6;
    private JLabel l7;
    private JLabel l8;
    private JButton backButton;

    public MenuVis() {
        add(panel1);
        setTitle("Information");
        setSize(640,480);
        setLocationRelativeTo(null);

        backButton.addActionListener(backButtonListener);
    }

    ActionListener backButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            panel1.setVisible(false);
            dispose();
        }
    };
}


