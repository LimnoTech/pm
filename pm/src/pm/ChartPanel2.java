package pm;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;

public class ChartPanel2 extends JPanel {

	private JFreeChart[] charts = new JFreeChart[12];
	private String[] bParts = { "CARRPP", "CVPSANLUISPP", "FOLSOMPP", "KESWICKPP", "NIMBUSPP", "ONEILPP", "SHASTAPP",
			"SPRINGCREEKPP", "TRINITYPP" };
	private String[] cParts = { "ENERGY", "FORGONE", "RELEASE", "SPILL" };
	private TimeSeries[][] series;
	private TimeSeries[] dataSeries = new TimeSeries[12];
	ChartPanel[] panels = new ChartPanel[12];
	TimeSeriesCollection[] datasets = new TimeSeriesCollection[12];

	private void readManyTimeSeries() {
		DSSGrabber1 dg = DG_Handle.getInstance().getDG();
		series = new TimeSeries[bParts.length][cParts.length];
		HecTime ht = new HecTime();
		for (int i = 0; i < bParts.length; i++) {
			for (int j = 0; j < cParts.length; j++) {

				dg.setBase(DG_Handle.getInstance().getBaseName());
				String seriesName = "/HYDROPOWER/" + bParts[i] + "/" + cParts[j]
						+ "/01JAN1930/1MON/POWERPLANT-GENERATION/";
				dg.setLocation("*" + seriesName);
				dg.setDateRange("FEB1924-feb2003");
				dg.checkReadiness();

				TimeSeriesContainer[] tscs = dg.getPrimarySeries(seriesName);
				series[i][j] = new TimeSeries(bParts[i] + "/" + cParts[j]);

				for (int k = 0; k < tscs[0].numberValues; k++) {
					ht.set(tscs[0].times[k]);
					series[i][j].addOrUpdate(new Month(ht.month(), ht.year()), tscs[0].values[k]);
				}
			}
		}

	}

	/**
	 * 
	 * @param bPart
	 * @param cPart
	 * @param month
	 */
	public ChartPanel2(String bPart, String cPart, String month) {

		super();

		readManyTimeSeries();
		this.setLayout(new GridLayout(0, 2));

		for (int i = 0; i < 12; i++) {
			datasets[i] = new TimeSeriesCollection();
			charts[i] = ChartFactory.createXYLineChart("", "Time (1MON)", "Value", null, true);
			panels[i] = new ChartPanel(charts[i]);
			panels[i].setMaximumDrawHeight(1200);
			panels[i].setMaximumDrawWidth(1920);
			panels[i].setMinimumDrawHeight(480);
			panels[i].setMinimumDrawWidth(640);
			panels[i].setPreferredSize(new Dimension(800, 600));

		}

		resetCharts(bPart, cPart, month, false);

	}

	public void resetCharts(String bPart, String cPart, String month, boolean isExceedance) {

		for (Component c : this.getComponents())
			if (c instanceof ChartPanel)
				this.remove(c);

		if (bPart.equals(""))
			buildMonthCharts(bPart, isExceedance);
		else
			buildStationCharts(month, isExceedance);

	}

	private void buildStationCharts(String month, boolean isExceedance) {
		
		for (int i = 0; i < bParts.length; i++) {
			
			// Set series
			
			dataSeries[i].clear();
			dataSeries[i] = series[i][0];
			
			// Build chart
			
			if (charts[i] != null && !(charts[i].getXYPlot().getDomainAxis()  instanceof DateAxis))
				charts[i] = null;
			
			if (charts[i] == null) {
				charts[i] = ChartFactory.createXYLineChart("", "Time (1MON)", "Value", null, true);
			} else 
			{
				charts[i]panels[i].getChart().getXYPlot().getDataset();
				datasets[i].addSeries(series[i][0]);				
			}
			

			XYPlot plot = panels[i].getChart().getXYPlot();
			plot.setDataset((XYDataset) datasets[i]);
			DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
			dateAxis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
			panels[i].getChart().setTitle(series[i][0].getKey().toString());
			this.add(panels[i]);
		}

	}

	private void buildMonthCharts(String bPart, boolean isExceedance) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 12; i++) {

			this.add(panels[i]);
		}

	}

	public void setTitle(String s) {
		charts[0].getXYPlot().getRangeAxis().setLabel(s);
	}
}