package pm;

import javax.swing.JList;

public class DG_Handle {

	private static DG_Handle instance;
	private static DSSGrabber1 dg;
	private static String[] scens = { "CL_DCR2015_2020D09E_Corrob_DV_20150901_LtGen.dss" };

	private DG_Handle() {
	};

	public static DG_Handle getInstance() {
		if (instance == null) {
			instance = new DG_Handle();

			JList<String> list = new JList<String>(scens);
			dg = new DSSGrabber1(list);
		}
		return instance;
	}

	public DSSGrabber1 getDG() {
		return dg;
	}

	public String getBaseName() {
		return scens[0];
	}
}
