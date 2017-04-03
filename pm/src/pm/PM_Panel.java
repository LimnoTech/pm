package pm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class PM_Panel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JRadioButton rb1 = new JRadioButton("By Year");
	private JSpinner sp;
	private JRadioButton rb2 = new JRadioButton("By Station");
	private JComboBox<String> sList;
	private ChartPanel2 cp;

	public PM_Panel() {

		super();

		this.setName("CVP");
		this.setLayout(new BorderLayout());

		Box h = Box.createHorizontalBox();
		ButtonGroup bg = new ButtonGroup();

		// By year radiobutton and spinner

		rb1.setSelected(true);
		bg.add(rb1);
		rb1.addActionListener(this);
		h.add(rb1);

		SpinnerModel model = new SpinnerNumberModel(1923, 1923, 2002, 1);
		sp = new JSpinner(model);
		h.add(sp);

		// By station radiobutton and combo box

		bg.add(rb2);
		rb2.addActionListener(this);
		h.add(rb2);

		String[] stationNames = { "ST 1", "ST 2", "ST 3" };
		sList = new JComboBox<String>(stationNames);
		sList.setEnabled(false);
		sList.addActionListener(this);
		h.add(sList);

		// Build panel

		this.add(h, BorderLayout.NORTH);
		cp = new ChartPanel2();
		this.add(cp, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) e.getSource();

			if (rb.isSelected()) {
				sp.setEnabled(rb.getText().equals("By Year"));
				sList.setEnabled(!sp.isEnabled());
			}
		} else if (e.getSource() instanceof JComboBox) {
			cp.setTitle((String) sList.getSelectedItem());
			cp.invalidate();
		}

	}
}
