package difftool.space.yukai;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import difftool.space.yukai.utils.FileUtils;

public class FileUtilTest {
	@Test
	@Ignore
	public void testGetAllFilesPath() {
		String rootPath = "Y:\\dbaeaver\\dbeaver-3.6.5\\plugins\\";
		File file = new File(rootPath);
		Set<String> allFilesPath;
		try {
			allFilesPath = FileUtils.getAllFilesPath(file);
			for (String fileName : allFilesPath) {
//				System.out.println(fileName);
				System.out.println(fileName.substring(rootPath.length()));
			}
			System.out.println(allFilesPath.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFileCopy() {
		String src = "Y:\\dbaeaver\\dbeaver-3.7.5\\plugins\\org.jkiss.dbeaver.ext.generic\\plugin~.xml";
		String dest = "Y:\\code\\dbstudio\\plugins\\org.jkiss.dbeaver.ext.generic\\plugin~.xml";
		try {
			FileUtils.copyFile(src, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
