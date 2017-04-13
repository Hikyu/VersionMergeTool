package difftool.space.yukai;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import difftool.space.yukai.differ.Processor;
import difftool.space.yukai.differ.VersionDiffer;
import difftool.space.yukai.differ.VersionDiffer.VersionDiffBuilder;
import difftool.space.yukai.utils.FileUtils;

public class VersionDiffTest {
	static FilenameFilter filter;
	static HashSet<String> diffFiles;
	static HashSet<String> notExsistFiles;
	static HashSet<String> scheduler;
	
	@BeforeClass
	public static void before() {
		filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};
		diffFiles = new HashSet<>();
		notExsistFiles = new HashSet<>();
		scheduler = new HashSet<>();
	}
	@Test
	@Ignore
	public void testGetToDiffFiles() {
		String version1RootPath = "Y:\\dbaeaver\\dbeaver-3.6.6\\plugins\\org.jkiss.dbeaver.core\\src\\org\\jkiss\\dbeaver\\ui\\controls\\resultset";
		String version2RootPath = "Y:\\dbaeaver\\dbeaver-3.6.5\\plugins\\org.jkiss.dbeaver.core\\src\\org\\jkiss\\dbeaver\\ui\\controls\\resultset";
		getToDiffFiles(version1RootPath, version2RootPath);
		System.out.println("diff***************************");
		for (String string : diffFiles) {
			System.out.println(string);
		}
		System.out.println("notExsistFiles***************************");
		for (String string : notExsistFiles) {
			System.out.println(string);
		}
		System.out.println("scheduler**************************");
		for (String string : scheduler) {
			System.out.println(string);
		}
	}
	private void getToDiffFiles(String version1RootPath, String version2RootPath) {
		try {
			Set<String> version1Paths = FileUtils.getAllFilesPath(new File(version1RootPath), filter);
			Set<String> version2Paths = FileUtils.getAllFilesPath(new File(version2RootPath), filter);
			Set<String> version1FilterPaths = new HashSet<>();
			Set<String> version2FilterPaths = new HashSet<>();
			for (String path : version1Paths) {
				version1FilterPaths.add(path.substring(version1RootPath.length()));
			}
			for (String path : version2Paths) {
				version2FilterPaths.add(path.substring(version2RootPath.length()));
			}
			// 求出新版本有，旧版本没有的文件集合
			HashSet<String> complement2 = new HashSet<>();
			complement2.addAll(version2FilterPaths);
			complement2.removeAll(version1FilterPaths);
			diffFiles.addAll(complement2);

			// 求出旧版本有，新版本没有的文件集合
			HashSet<String> complement1 = new HashSet<>();
			complement1.addAll(version1FilterPaths);
			complement1.removeAll(version2FilterPaths);
			notExsistFiles.addAll(complement1);

			// 求交集
			HashSet<String> intersection = new HashSet<>();
			intersection.addAll(version1FilterPaths);
			intersection.retainAll(version2FilterPaths);

			// 加入队列
			scheduler.addAll(intersection);
//			for (String file : intersection) {
//				String version1AbsolutePath = version1RootPath.concat(file);
//				String version2AbsolutePath = version2RootPath.concat(file);
//				DiffVersionHandler handler = new DiffVersionHandler(version1AbsolutePath, version2AbsolutePath);
//				scheduler.add(handler);
//			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Test
	public void testVersionDiff() {
		String version1RootPath = "Y:\\dbaeaver\\dbeaver-3.7.8\\plugins\\org.jkiss.dbeaver.model\\src\\";
		String version2RootPath = "Y:\\code\\dbstudio\\plugins\\org.jkiss.dbeaver.model\\src\\";
		VersionDiffBuilder builder = new VersionDiffBuilder(version1RootPath, version2RootPath);
		VersionDiffer differ = builder.threadNum(4).processor(new Processor() {
			
			@Override
			public void resultProcessor(Set<String> diffFiles, Set<String> sameFiles, Set<String> notExsistFiles, Set<String> OutOfDateFiles) {
				System.out.println("差异文件>>>>>>>>>>>>>>>>>>>>>>>>>>>" + diffFiles.size());
				for (String string : diffFiles) {
					System.out.println(string);
				}
				System.out.println("\n");
				System.out.println("相同文件>>>>>>>>>>>>>>>>>>>>>>>>>>>" + sameFiles.size());
				for (String string : sameFiles) {
					System.out.println(string);
				}
				System.out.println("\n");
				System.out.println("新版本不存在文件>>>>>>>>>>>>>>>>>>>>>>>>>>>" + notExsistFiles.size());
				for (String string : notExsistFiles) {
					System.out.println(string);
				}
				
			}
		}).build();
		
		differ.run();
	}
}
