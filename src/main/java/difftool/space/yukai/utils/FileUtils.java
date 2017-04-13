package difftool.space.yukai.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件操作
 * 
 * @author kyu
 * @date 2017年4月10日
 */
public class FileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	/**
	 * 从给定的根目录检索，递归得到该目录下所有的文件 
	 * 
	 * 2017年4月10日
	 * @return
	 * @throws IOException
	 */
	public static Set<String> getAllFilesPath(File root) throws IOException {
		FilenameFilter filter = new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return true;
			}
		};
		return getAllFilesPath(root, filter);
	}
	
	public static Set<String> getAllFilesPath(File root, FilenameFilter filter) throws IOException {
		Set<String> files = new HashSet<String>();
		try {
			if (!root.exists()) {
				throw new IllegalArgumentException("路径无效： " + root.getCanonicalPath());
			}
			File[] childs = root.listFiles(filter);
			for (File file : childs) {
				//深度优先
				if (file.isDirectory()) {
					files.addAll(getAllFilesPath(file, filter));
				} else {
					files.add(file.getCanonicalPath());
				}
			}
		} catch (Exception e) {
			// 递归异常
			logger.error("递归异常", e);
			return files;
		}
		return files;

	}
	
	public static List<String> fileToLines(File file) throws IOException {
        final List<String> lines = new ArrayList<String>();
        String line = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
		} finally {
			if (in != null) {
				in.close();
			}
		}
        return lines;
    }
	
	public static void copyFile(String src, String dest) throws IOException{
		File srcFile = new File(src);
		File destFile = new File(dest);
		org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
	}
	
	public static void copyDirectory(String src, String dest) throws IOException {
		File srcDir = new File(src);
		File destDir = new File(dest);
		org.apache.commons.io.FileUtils.copyDirectory(srcDir, destDir);
	}
}
