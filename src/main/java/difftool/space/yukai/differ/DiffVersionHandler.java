package difftool.space.yukai.differ;

import java.io.File;
import java.io.IOException;
import java.util.List;

import difftool.space.yukai.utils.FileUtils;

/**
 * 同一份文件的不同版本句柄
 * 
 * @author kyu
 * @date 2017年4月10日
 */
public class DiffVersionHandler {
	private String version1Path;
	private String version2Path;
	private List<String> version1Lines;
	private List<String> version2Lines;
	/* 版本之间是否有差异 */
	private boolean isDiff;

	public enum DiffVersion {
		VERSION1, VERSION2
	}

	public List<String> getVersionLines(DiffVersion version) throws IOException {
		switch (version) {
		case VERSION1:
			if (version1Lines == null) {
				version1Lines = FileUtils.fileToLines(new File(version1Path));
			}
			return version1Lines;
		case VERSION2:
			if (version2Lines == null) {
				version2Lines = FileUtils.fileToLines(new File(version2Path));
			}
			return version2Lines;
		default:
			//never do this
			return null;
		}
	}

	public boolean isDiff() {
		return isDiff;
	}

	public void diff(boolean diff) {
		isDiff = diff;
	}

	public DiffVersionHandler(String version1, String version2) {
		this.version1Path = version1;
		this.version2Path = version2;
	}

	public String getVersion1Path() {
		return version1Path;
	}

	public String getVersion2Path() {
		return version2Path;
	}

}
