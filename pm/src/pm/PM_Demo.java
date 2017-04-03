package pm;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class PM_Demo {

	public static void main(String[] args) {

		JFrame f = new JFrame("PM_Demo");
		f.setLayout(new BorderLayout());

		JTabbedPane tp = new JTabbedPane();

		// First panel

		PM_Panel p1 = new PM_Panel();

		// Second panel

		JPanel p2 = new JPanel();
		p2.setName("SWP");
		p2.setLayout(new BorderLayout());

		tp.add("CVP", p1);
		tp.add("SWP", p2);

		f.add(tp);
		f.pack();
		f.setVisible(true);

	}

}
