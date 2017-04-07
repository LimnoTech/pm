package pm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PM_Panel extends JPanel implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JRadioButton rb1 = new JRadioButton("By Month");
	private JSpinner sp;
	private JRadioButton rb2 = new JRadioButton("By Station");
	private JComboBox<String> sList;
	private JComboBox<String> cList;
	private ChartPanel2 cp;
	private JCheckBox cb1 = new JCheckBox("Exceedance");

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

		sp = new JSpinner();
		ResultUtils.SetMonthModelAndIndex(sp, 9, null, false);
		sp.addChangeListener(this);
		h.add(sp);
		h.add(Box.createRigidArea(new Dimension(20, 20)));

		// By station radiobutton and combo box

		bg.add(rb2);
		rb2.addActionListener(this);
		h.add(rb2);

		String[] bParts = { "CARRPP", "CVPSANLUISPP", "FOLSOMPP", "KESWICKPP", "NIMBUSPP", "ONEILPP", "SHASTAPP",
				"SPRINGCREEKPP", "TRINITYPP" };

		sList = new JComboBox<String>(bParts);
		sList.setSelectedIndex(0);
		sList.setEnabled(false);
		sList.addActionListener(this);
		h.add(sList);
		h.add(Box.createRigidArea(new Dimension(20, 20)));

		// C-PART selection

		h.add(new JLabel("  C-PART: "));

		String[] cParts = { "ENERGY", "FORGONE", "RELEASE", "SPILL" };

		cList = new JComboBox<String>(cParts);
		cList.setSelectedIndex(0);
		cList.setEnabled(true);
		cList.addActionListener(this);
		h.add(cList);
		h.add(Box.createRigidArea(new Dimension(20, 20)));

		h.add(cb1);

		// Build panel

		this.add(h, BorderLayout.NORTH);
		cp = new ChartPanel2((String) sList.getSelectedItem(), (String) cList.getSelectedItem(),
				(String) sp.getValue());
		this.add(cp, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) e.getSource();

			if (rb.isSelected()) {
				sp.setEnabled(rb.getText().equals("By Month"));
				sList.setEnabled(!sp.isEnabled());
			}
		} else if (e.getSource() instanceof JComboBox) {
			cp.setTitle((String) sList.getSelectedItem());
			cp.invalidate();
		}

	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub

	}
}
