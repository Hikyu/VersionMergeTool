package difftool.space.yukai;

public class App {
	public static void main(String[] args) {
		String mergeTo ="Y:\\code\\dbstudio\\features\\";
		String newVer = "Y:\\dbaeaver\\dbeaver-3.7.8\\features\\";
		String oldVer = "Y:\\dbaeaver\\dbeaver-3.7.5\\features\\";
		VersionMergeTool tool = new VersionMergeTool();
		tool.addToMergeVersion(oldVer, newVer, mergeTo);
		tool.doMerge();
	}
}
