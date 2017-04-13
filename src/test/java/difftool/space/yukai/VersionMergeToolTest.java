package difftool.space.yukai;

import org.junit.Test;

import difftool.space.yukai.merge.VersionMergeTool;

public class VersionMergeToolTest {
	private static final String mergeToRoot = "Y:\\code\\dbstudio\\";
	private static final String newVerRoot = "Y:\\dbaeaver\\dbeaver-3.8.1\\";
	private static final String oldVerRoot = "Y:\\dbaeaver\\dbeaver-3.7.8\\";

	private static final String features = "features\\";
	private static final String db2 = "plugins\\org.jkiss.dbeaver.ext.db2\\";
	private static final String derby = "plugins\\org.jkiss.dbeaver.ext.derby\\";
	private static final String erd = "plugins\\org.jkiss.dbeaver.ext.erd\\";
	private static final String firebird = "plugins\\org.jkiss.dbeaver.ext.firebird\\";
	private static final String generic = "plugins\\org.jkiss.dbeaver.ext.generic\\";
	private static final String import_config = "plugins\\org.jkiss.dbeaver.ext.import-config\\";
	private static final String informix = "plugins\\org.jkiss.dbeaver.ext.informix\\";
	private static final String mssql = "plugins\\org.jkiss.dbeaver.ext.mssql\\";
	private static final String mysql = "plugins\\org.jkiss.dbeaver.ext.mysql\\";
	private static final String netezza = "plugins\\org.jkiss.dbeaver.ext.netezza\\";
	private static final String oracle = "plugins\\org.jkiss.dbeaver.ext.oracle\\";
	private static final String phoenix = "plugins\\org.jkiss.dbeaver.ext.phoenix\\";
	private static final String postgresql = "plugins\\org.jkiss.dbeaver.ext.postgresql\\";
	private static final String teradata = "plugins\\org.jkiss.dbeaver.ext.teradata\\";
	private static final String vertica = "plugins\\org.jkiss.dbeaver.ext.vertica\\";
	private static final String wmi = "plugins\\org.jkiss.dbeaver.ext.wmi\\";
	private static final String intro = "plugins\\org.jkiss.dbeaver.intro\\";
	private static final String model = "plugins\\org.jkiss.dbeaver.model\\";
	private static final String test = "plugins\\org.jkiss.dbeaver.test\\";

	@Test
	public void testBackup() {
		String[] folders = new String[] { features, db2, derby, erd, firebird, generic, import_config, informix, mssql,
				mysql, netezza, oracle, phoenix, postgresql, teradata, vertica, wmi, intro, model, test };
		String mergeTo, newVer, oldVer;
		VersionMergeTool tool = new VersionMergeTool(8, "C:\\Users\\kyu\\Desktop\\tt\\");
		
		for (String folder : folders) {
			mergeTo = mergeToRoot + folder;
			newVer = newVerRoot + folder;
			oldVer = oldVerRoot + folder;
			tool.addToMergeVersion(oldVer, newVer, mergeTo);
		}
		tool.doBackup();
	}
	
}
