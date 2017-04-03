package pm;

import java.awt.Component;
import java.awt.Container;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import calsim.app.AppUtils;
import calsim.app.Project;
import gov.ca.water.calgui.results.DisplayFrame;
import gov.ca.water.calgui.results.Report.PathnameMap;
import vista.db.dss.DSSUtil;
import vista.report.TSMath;
import vista.set.Constants;
import vista.set.DataReference;
import vista.set.DataSetElement;
import vista.set.ElementFilter;
import vista.set.ElementFilterIterator;
import vista.set.Group;
import vista.set.MultiIterator;
import vista.set.Pathname;
import vista.set.RegularTimeSeries;
import vista.set.Stats;
import vista.set.TimeSeries;
import vista.time.SubTimeFormat;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeWindow;

public class ResultUtils implements ChangeListener {
	private static final Logger LOG = Logger.getLogger(ResultUtils.class.getName());
	private String lookups[][];
	private static ResultUtils resultUtils;
	private SwingEngine swingEngine;
	private Project project;
	private String table5[][]; // Holds DSS Schematic link values
	private StringBuffer messages = new StringBuffer();

	public void clearMessages() {
		messages.setLength(0);
	}

	public void addMessage(String msg) {
		messages.append(msg).append("\n");
	}

	public String getMessages() {
		return messages.toString();
	}

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static ResultUtils getXMLParsingSvcImplInstance(SwingEngine swingEngine) {
		if (resultUtils == null) {
			resultUtils = new ResultUtils(swingEngine);
		}
		return resultUtils;
	}

	private ResultUtils(SwingEngine swingEngine) {
		this.swingEngine = swingEngine;
		readInLookups();

		// Create a WRIMS GUI project for WRIMS GUI to work off of

		project = new Project();
		AppUtils.setCurrentProject(project);
		AppUtils.baseOn = false;

		// Read Schematic_DSS_link4.table and place in Table5
		ArrayList<String> guiLinks5 = new ArrayList<String>();

		try {
			guiLinks5 = getGUILinks("Config/Schematic_DSS_link4.table");
			table5 = new String[guiLinks5.size()][6];

			for (int i = 0; i < guiLinks5.size(); i++) {
				String tokens[] = guiLinks5.get(i).split("\t");
				table5[i][0] = tokens[0];
				table5[i][1] = tokens[1];
				table5[i][2] = tokens[2];
				table5[i][3] = tokens[3];
				table5[i][4] = tokens[4];
				table5[i][5] = tokens[5];
			}
		} catch (Exception ex) {

		}
	}

	public void quickDisplay(String cbText, String cbName) {
		// ----- Quick Results: HANDLE DISPLAY OF SINGLE VARIABLE -----
		// menu.setCursor(hourglassCursor);
		JList lstScenarios = (JList) swingEngine.find("SelectedList");
		if (lstScenarios.getModel().getSize() == 0) {
			JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			DisplayFrame.showDisplayFrames(DisplayFrame.quickState() + ";Locs-" + cbText + ";Index-" + cbName,
					lstScenarios);
		}

	}

	public void toggleEnComponentAndChildren(Component component, Boolean b) {
		component.setEnabled(b);
		for (Component child : ((Container) component).getComponents()) {
			toggleEnComponentAndChildren(child, b);
		}
	}

	public ArrayList<String> getGUILinks(String filename) {
		ArrayList<String> GUILinks = new ArrayList<String>();
		Scanner input;
		try {
			input = new Scanner(new FileReader(filename));
		} catch (FileNotFoundException e) {
			LOG.debug(e.getMessage());
			return null;
		}
		int lineCount = 0;
		// int rowid = 0;
		// int colid = 0;
		while (input.hasNextLine()) {
			String line = input.nextLine();
			lineCount++;
			if (lineCount > 1) {
				StringTokenizer st1 = new StringTokenizer(line, "\t| ");
				if (st1.countTokens() > 0) {
					GUILinks.add(line);
				}
			}
		}
		input.close();
		return GUILinks;
	}

	public ArrayList<double[]> buildDataArray(DataReference ref1, DataReference ref2, TimeWindow tw) {
		ArrayList<double[]> dlist = new ArrayList<double[]>();
		if ((ref1 == null) && (ref2 == null)) {
			return dlist;
		}
		TimeSeries data1 = (TimeSeries) ref1.getData();
		TimeSeries data2 = (TimeSeries) ref2.getData();
		if (tw != null) {
			data1 = data1.createSlice(tw);
			data2 = data2.createSlice(tw);
		}
		MultiIterator iterator = buildMultiIterator(new TimeSeries[] { data1, data2 }, Constants.DEFAULT_FLAG_FILTER);
		while (!iterator.atEnd()) {
			DataSetElement e = iterator.getElement();
			Date date = convertToDate(TimeFactory.getInstance().createTime(e.getXString()));
			dlist.add(new double[] { date.getTime(), e.getX(1), e.getX(2) });
			iterator.advance();
		}
		return dlist;
	}

	public Date convertToDate(Time time_val) {
		return new Date(time_val.getDate().getTime() - TimeZone.getDefault().getRawOffset());
	}

	public MultiIterator buildMultiIterator(TimeSeries[] dsarray, ElementFilter filter) {
		if (filter == null) {
			return new MultiIterator(dsarray);
		} else {
			return new MultiIterator(dsarray, filter);
		}
	}

	public ArrayList<double[]> buildExceedanceArray(DataReference ref1, DataReference ref2, boolean end_of_sept,
			TimeWindow tw) {
		ArrayList<Double> x1 = sort(ref1, end_of_sept, tw);
		ArrayList<Double> x2 = sort(ref2, end_of_sept, tw);
		ArrayList<double[]> darray = new ArrayList<double[]>();
		int i = 0;
		int n = Math.round(Math.min(x1.size(), x2.size()));
		while (i < n) {
			darray.add(new double[] { 100.0 - 100.0 * i / (n + 1), x1.get(i), x2.get(i) });
			i = i + 1;
		}
		return darray;
	}

	public ArrayList<Double> sort(DataReference ref, boolean end_of_sept, TimeWindow tw) {
		TimeSeries data = (TimeSeries) ref.getData();
		if (tw != null) {
			data = data.createSlice(tw);
		}
		ArrayList<Double> dx = new ArrayList<Double>();
		ElementFilterIterator iter = new ElementFilterIterator(data.getIterator(), Constants.DEFAULT_FLAG_FILTER);
		while (!iter.atEnd()) {
			if (end_of_sept) {
				if (iter.getElement().getXString().indexOf("30SEP") >= 0) {
					dx.add(iter.getElement().getY());
				}
			} else {
				dx.add(iter.getElement().getY());
			}
			iter.advance();
		}
		Collections.sort(dx);
		return dx;
	}

	/**
	 * Retrieves the contents list for a dss file
	 *
	 * @param filename
	 * @return a handle to the content listing for a dss file
	 */
	public Group opendss(String filename) {
		return DSSUtil.createGroup("local", filename);
	}

	public RegularTimeSeries cfs2taf(RegularTimeSeries data) {
		RegularTimeSeries data_taf = (RegularTimeSeries) TSMath.createCopy(data);
		TSMath.cfs2taf(data_taf);
		return data_taf;
	}

	public double avg(RegularTimeSeries data, TimeWindow tw) {
		try {
			return Stats.avg(data.createSlice(tw)) * 12;
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			return Double.NaN;
		}
	}

	public DataReference getReference(Group group, String path, boolean calculate_dts,
			ArrayList<PathnameMap> pathname_maps, int group_no) {
		if (calculate_dts) {
			try {
				// FIXME: add expression parser to enable any expression
				String bpart = path.split("/")[2];
				String[] vars = bpart.split("\\+");
				DataReference ref = null;
				for (String varname : vars) {
					DataReference xref = null;
					String varPath = createPathFromVarname(path, varname);
					xref = getReference(group, varPath, false, pathname_maps, group_no);
					if (xref == null) {
						throw new RuntimeException("Aborting calculation of " + path + " due to previous path missing");
					}
					if (ref == null) {
						ref = xref;
					} else {
						ref = ref.__add__(xref);
					}
				}
				return ref;
			} catch (Exception ex) {
				addMessage(ex.getMessage());
				LOG.debug(ex.getMessage());
				return null;
			}
		} else {
			try {
				DataReference[] refs = findpath(group, path, true);
				if (refs == null) {
					String msg = "No data found for " + group + " and " + path;
					addMessage(msg);
					System.err.println(msg);
					return null;
				} else {
					return refs[0];
				}
			} catch (Exception ex) {
				String msg = "Exception while trying to retrieve " + path + " from " + group;
				System.err.println(msg);
				addMessage(msg);
				LOG.debug(msg);
				return null;
			}
		}
	}

	/**
	 * findpath(g,path,exact=1): this returns an array of matching data
	 * references g is the group returned from opendss function path is the
	 * dsspathname e.g. '//C6/FLOW-CHANNEL////' exact means that the exact
	 * string is matched as opposed to the reg. exp.
	 *
	 * @param g
	 * @param path
	 * @param exact
	 * @return
	 */
	public DataReference[] findpath(Group g, String path, boolean exact) {
		String[] pa = new String[6];
		for (int i = 0; i < 6; i++) {
			pa[i] = "";
		}
		int i = 0;
		for (String p : path.trim().split("/")) {
			if (i == 0) {
				i++;
				continue;
			}
			if (i >= pa.length) {
				break;
			}
			pa[i - 1] = p;
			if (exact) {
				if (p.length() > 0) {
					pa[i - 1] = "^" + pa[i - 1] + "$";
				}
			}
			i++;
		}
		return g.find(pa);
	}

	private String createPathFromVarname(String path, String varname) {
		String[] parts = path.split("/");
		if (parts.length > 2) {
			parts[2] = varname;
		}
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.length() > 0) {
				part = "^" + part + "$";
			}
			builder.append(part).append("/");
		}
		return builder.toString();
	}

	public String formatTimeWindowAsWaterYear(TimeWindow tw) {
		SubTimeFormat year_format = new SubTimeFormat("yyyy");
		return tw.getStartTime().__add__("3MON").format(year_format) + "-"
				+ tw.getEndTime().__add__("3MON").format(year_format);
	}

	public String getExceedancePlotTitle(PathnameMap path_map) {
		String title = "Exceedance " + path_map.var_name.replace("\"", "");
		if (path_map.var_category.equals("S_SEPT")) {
			title = title + " (Sept)";
		}
		return title;
	}

	public String getUnitsForReference(DataReference ref) {
		if (ref != null) {
			return ref.getData().getAttributes().getYUnits();
		}
		return "";
	}

	public String getUnits(DataReference ref1, DataReference ref2) {
		if (ref1 == null) {
			if (ref2 == null) {
				return "";
			} else {
				return getUnitsForReference(ref2);
			}
		} else {
			return getUnitsForReference(ref1);
		}
	}

	public String getTypeOfReference(DataReference ref) {
		if (ref != null) {
			Pathname p = ref.getPathname();
			return p.getPart(Pathname.C_PART);
		}
		return "";
	}

	public String getType(DataReference ref1, DataReference ref2) {
		if (ref1 == null) {
			if (ref2 == null) {
				return "";
			} else {
				return getTypeOfReference(ref2);
			}
		} else {
			return getTypeOfReference(ref1);
		}
	}

	/**
	 * Getter for access to application-wide SwiXml engine
	 *
	 * @return swix
	 */
	public SwingEngine getSwix() {
		return swingEngine;
	}

	/**
	 * Reads GUI_Links3.table into the String array lookups[][] (controls Quick
	 * Results display)
	 *
	 * @return
	 */
	private int readInLookups() {
		Scanner input;
		try {
			input = new Scanner(new FileReader("Config/GUI_Links3.table"));
		} catch (FileNotFoundException e) {
			LOG.debug("Cannot open input file Config/GUI_Links3.table: " + e.getMessage());
			return -1;
		}
		Vector<String> allLookups = new Vector<String>();
		int lineCount = 0;
		input.nextLine(); // Skip header line
		while (input.hasNextLine()) {
			String line = input.nextLine();
			allLookups.add(line);
			lineCount++;
		}
		input.close();
		lookups = new String[lineCount][6];
		for (int i = 0; i < lineCount; i++) {
			String[] parts = allLookups.get(i).split("[\t]+");
			for (int j = 0; j < 6; j++) {
				if (parts[j].equals("null"))
					parts[j] = "";
				lookups[i][j] = parts[j];
			}
			if (lookups[i][1].equals("") && !lookups[i][0].startsWith("0")) { // additional
				JCheckBox cb = (JCheckBox) swingEngine.find("ckbp" + lookups[i][0]);
				cb.setEnabled(false);
			}
		}
		return 0;
	}

	public String getLookups5(int i, int j) {
		return table5[i][j];
	}

	public int getLookups5Length() {
		return table5.length;
	}

	public String getLookups(int i, int j) {
		return lookups[i][j];
	}

	public int getLookupsLength() {
		return lookups.length;
	}

	public Project getProject() {
		return project;
	}

	public int monthToInt(String month) {
		HashMap<String, Integer> monthMap = new HashMap<String, Integer>();
		monthMap.put("jan", 1);
		monthMap.put("feb", 2);
		monthMap.put("mar", 3);
		monthMap.put("apr", 4);
		monthMap.put("may", 5);
		monthMap.put("jun", 6);
		monthMap.put("jul", 7);
		monthMap.put("aug", 8);
		monthMap.put("sep", 9);
		monthMap.put("oct", 10);
		monthMap.put("nov", 11);
		monthMap.put("dec", 12);
		month = month.toLowerCase();
		Integer monthCode = null;
		try {
			monthCode = monthMap.get(month);
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
		if (monthCode == null) {
			LOG.debug("Invalid Key at UnitsUtils.monthToInt");
			return -1;
		}
		return monthCode.intValue();
	}

	/**
	 * Sets up a spinner for a numeric range
	 *
	 * @param jspn
	 *            - Swing spinner component
	 * @param val
	 *            - Initial value
	 * @param min
	 *            - Minimum value
	 * @param max
	 *            - Maximum value
	 * @param step
	 *            - Increment between values
	 * @param format
	 *            - Format for display
	 * @param obj
	 *            - ChangeListener
	 * @param changelistener
	 *            - True is a ChangeListener is to be assigned
	 */
	public static void SetNumberModelAndIndex(JSpinner jspn, int val, int min, int max, int step, String format,
			Object obj, boolean changelistener) {

		SpinnerModel spnmod = new SpinnerNumberModel(val, min, max, step);
		jspn.setModel(spnmod);
		jspn.setEditor(new JSpinner.NumberEditor(jspn, format));
		if (changelistener == true) {
			jspn.addChangeListener((ChangeListener) obj);
		}
	}

	/**
	 *
	 * @param jspn
	 *            - Swing spinner component
	 * @param idx
	 * @param obj
	 *            - ChangeListener
	 * @param changelistener
	 *            - True is a ChangeListener is to be assigned
	 *
	 */
	public static void SetMonthModelAndIndex(JSpinner jspn, int idx, Object obj, boolean changelistener) {
		String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

		try {
			SpinnerListModel monthModel = new SpinnerListModel(monthNames);
			jspn.setModel(monthModel);
			jspn.setValue(monthNames[idx]);
			if (changelistener == true) {
				jspn.addChangeListener((ChangeListener) obj);
			}
		}

		catch (Exception e) {
			LOG.debug("Problem reading table files. " + e);
		}
	}

	@Override
	public void stateChanged(ChangeEvent changeEvent) {
		Component c = (Component) changeEvent.getSource();
		String lcName = c.getName().toLowerCase();
		if (lcName.substring(0, 3).equals("spn")) {
			// Constrain run times to [10/1921,9/2003]
			int syr = (Integer) ((JSpinner) swingEngine.find("spnRunStartYear")).getValue();
			int eyr = (Integer) ((JSpinner) swingEngine.find("spnRunEndYear")).getValue();
			int smo = monthToInt(((String) ((JSpinner) swingEngine.find("spnRunStartMonth")).getValue()).trim());
			int emo = monthToInt(((String) ((JSpinner) swingEngine.find("spnRunEndMonth")).getValue()).trim());
			if ((syr == 1921) && (smo < 10))
				((JSpinner) swingEngine.find("spnRunStartMonth")).setValue("Oct");
			if ((eyr == 2003) && (emo > 9))
				((JSpinner) swingEngine.find("spnRunEndMonth")).setValue("Sep");
			// Constrain display times the same way [inefficient?]
			syr = (Integer) ((JSpinner) swingEngine.find("spnStartYear")).getValue();
			eyr = (Integer) ((JSpinner) swingEngine.find("spnEndYear")).getValue();
			smo = monthToInt(((String) ((JSpinner) swingEngine.find("spnStartMonth")).getValue()).trim());
			emo = monthToInt(((String) ((JSpinner) swingEngine.find("spnEndMonth")).getValue()).trim());
			if ((syr == 1921) && (smo < 10))
				((JSpinner) swingEngine.find("spnStartMonth")).setValue("Oct");
			if ((eyr == 2003) && (emo > 9))
				((JSpinner) swingEngine.find("spnEndMonth")).setValue("Sep");
		} else if (lcName.equals("tabbedpane1")) {
			JMenuBar menuBar = (JMenuBar) this.swingEngine.find("menu");
			menuBar.setSize(150, 20);
			if (((JTabbedPane) c).getSelectedIndex() == 6) { // Quick Results

			}
		}
	}
}
