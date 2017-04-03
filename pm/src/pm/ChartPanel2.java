package pm;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;

public class ChartPanel2 extends JPanel {

	private XYPlot subplot1;

	public ChartPanel2() {

		super();
		JFreeChart chart;

		DSSGrabber1 dg = DG_Handle.getInstance().getDG();
		dg.setBase(DG_Handle.getInstance().getBaseName());
		dg.setLocation("*/HYDROPOWER/CARRPP/ENERGY/01JAN1930/1MON/POWERPLANT-GENERATION/");
		dg.setDateRange("FEB1924-feb2003");
		dg.checkReadiness();

		TimeSeriesContainer[] tscs = dg
				.getPrimarySeries("/HYDROPOWER/CARRPP/ENERGY/01JAN1930/1MON/POWERPLANT-GENERATION/");

		TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		TimeSeries[] series = new TimeSeries[1];
		HecTime ht = new HecTime();

		series[0] = new TimeSeries("");
		int i = 0;
		for (int j = 0; j < tscs[i].numberValues; j++) {
			ht.set(tscs[0].times[j]);
			series[i].addOrUpdate(new Month(ht.month(), ht.year()), tscs[i].values[j]);
		}
		dataset1.addSeries(series[0]);
		XYItemRenderer renderer1 = new StandardXYItemRenderer();
		NumberAxis rangeAxis1 = new NumberAxis("Plot 1");
		subplot1 = new XYPlot(dataset1, null, rangeAxis1, renderer1);

		NumberAxis rangeAxis2 = new NumberAxis("Plot 2");
		XYPlot subplot2 = new XYPlot(dataset1, null, rangeAxis2, renderer1);

		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis(""));
		plot.add(subplot1, 1);
		plot.add(subplot2, 1);
		plot.setGap(15.0);
		plot.setOrientation(PlotOrientation.VERTICAL);
		chart = new JFreeChart("Test", JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		final ChartPanel p1 = new ChartPanel(chart);
		p1.setMaximumDrawHeight(1200);
		p1.setMaximumDrawWidth(1920);
		p1.setMinimumDrawHeight(480);
		p1.setMinimumDrawWidth(640);
		p1.setPreferredSize(new Dimension(800, 600));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(p1);

	}

	public void setTitle(String s) {
		subplot1.getRangeAxis().setLabel(s);
	}
}