package difftool.space.yukai.merge;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import difftool.space.yukai.differ.Processor;
import difftool.space.yukai.differ.VersionDiffer;
import difftool.space.yukai.differ.VersionDiffer.VersionDiffBuilder;
import difftool.space.yukai.utils.FileUtils;

/**
 * 版本合并工具 思路：以Dbstudio 更新到 dbeaver 3.7.8为例。设当前Dbstudio已经更新到dbeaver3.7.5
 * 1. 找出Dbstudio与dbeaver3.7.5中内容相同文件list1，这部分文件是我们没有更改过的 
 * 2. 找出dbeaver3.7.8与dbeaver3.7.5内容不同的文件list2，这部分是3.7.8相对于3.7.5升级(修改)的部分，也是我们要对应升级的部分
 * 3. 找出dbeaver3.7.8中存在而dbeaver3.7.5中没有的文件list3,这部分是3.7.8相对于3.7.5升级(新增)的部分，也是我们要对应升级的部分
 * 4. 找出dbeaver3.7.5中存在而dbeaver3.7.8中没有的文件list4,这部分是3.7.8相对于3.7.5升级(删除)的部分，也是我们要对应升级的部分
 * 5. 找出list1与list2的交集list5，这部分是可以直接从3.7.8复制到Dbstudio的，也是可以实现工具自动化更新到 部分
 * 6. 找出list2中存在而list1中没有的文件list6，这部分文件是我们需要手动修改的文件
 * 7. 将list3直接复制到Dbstudio，这是升级新增的内容
 * 8. 将list4打印，这是升级需要删除的部分，删除动作需要手动进行
 * 线程不安全
 * @author kyu
 * @date 2017年4月11日
 */
public class VersionMergeTool {
	private static Logger logger = LoggerFactory.getLogger(VersionMergeTool.class);
	/* 复制失败文件 */
	private Map<String, String> allCopyFailedFiles;
	/* 待合并版本 */
	private List<MergeTool> toMergeFiles;
	/* 最大并发数 */
	private int threadNum;
	/* 备份根路径*/
	private String backupRootPath;
	
	public VersionMergeTool() {
		this(8,"");
	}
	
	public VersionMergeTool(int threadNum, String backupPath) {
		this.threadNum = threadNum;
		this.backupRootPath = backupPath;
		toMergeFiles = new ArrayList<>();
		allCopyFailedFiles = new ConcurrentHashMap<String, String>();
	}

	/**
	 * 添加合并文件
	 * 2017年4月12日 
	 * @param oldVersion
	 * @param newVersion
	 * @param toMerge
	 * @param autoMerge 是否自动合并
	 * @param filter 文件过滤器
	 */
	public void addToMergeVersion(String oldVersion, String newVersion, String toMerge, boolean autoMerge, FilenameFilter filter) {
		toMergeFiles.add(new MergeTool(toMerge, newVersion, oldVersion, autoMerge, filter));
	}
	
	public void addToMergeVersion(String oldVersion, String newVersion, String toMerge) {
		addToMergeVersion(oldVersion, newVersion, toMerge, false);
	}
	
	public void addToMergeVersion(String oldVersion, String newVersion, String toMerge, boolean autoMerge) {
        FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};
		addToMergeVersion(oldVersion, newVersion, toMerge, autoMerge, filter);
	}
	
	private String getBackupPath(String src, String dest) {
		int index = src.indexOf(File.separator);
		return dest.endsWith(File.separator) ? dest + src.substring(index + 1) : dest + src.substring(index);
	}
	
	public void doBackup() {
		if (backupRootPath == null || "".equals(backupRootPath)) {
			throw new IllegalArgumentException("备份路径不存在!");
		}
		File file = new File(backupRootPath);
		if (!file.exists() || !file.isDirectory()) {
			throw new IllegalArgumentException("备份路径不正确!");
		}
		System.out.println("***********************************开始备份*********************************");
		String sourcePath, backupPath;
		for (MergeTool mergeTool : toMergeFiles) {
			sourcePath = mergeTool.dbstudioPath;
			backupPath = getBackupPath(sourcePath, backupRootPath);
			System.out.println(sourcePath + " TO " + backupPath);
			try {
				FileUtils.copyDirectory(sourcePath, backupPath);
			} catch (IOException e) {
				logger.error(sourcePath + ": 备份失败!", e);
			}
		}
		System.out.println("***********************************备份完毕*********************************");
		
	}
	
	public void doMerge() {
		if (backupRootPath != null && !"".equals(backupRootPath)) {
			doBackup();
		}
		for (MergeTool tool : toMergeFiles) {
			System.out.println("************************************************************************");
			System.out.println(tool.toString());
			System.out.println("************************************************************************");
			tool.merge();
			allCopyFailedFiles.putAll(tool.copyFailedFiles);
		}
	}
	
	public void doMergeSync() {
		// TODO 多线程异步合并
	}

	public Map<String, String> getCopyFailedFiles() {
		return allCopyFailedFiles;
	}

	class MergeTool {
		private final String dbstudioPath;
		private final String dbeaver_old_path;
		private final String dbeaver_new_path;
		private final Map<String, String> copyFailedFiles;
		private final boolean autoMerge;
		private final FilenameFilter filter;
		private final static String DIFF_Flag = "diffFiles";
		private final static String SAME_Flag = "sameFiles";
		private final static String NOT_EXSIST_Flag = "notExsistFiles";
		private final static String OUT_OF_DATE_Flag = "outOfDateFiles";
		
		public MergeTool(String dbstudioPath, String dbeaver_new_path, String dbeaver_old_path, boolean autoMerge, FilenameFilter filter) {
			this.dbeaver_new_path = dbeaver_new_path;
			this.dbeaver_old_path = dbeaver_old_path;
			this.dbstudioPath = dbstudioPath;
			this.autoMerge = autoMerge;
			this.filter = filter;
			copyFailedFiles = new HashMap<>();
		}

		public void merge() {
			Map<String, Set<String>> dbstudio_dbeaverold = getVersionDiffResult(dbeaver_old_path, dbstudioPath);
			Set<String> list1 = dbstudio_dbeaverold.get(SAME_Flag);
			
			Map<String, Set<String>> dbeavernew_dbeaverold = getVersionDiffResult(dbeaver_old_path, dbeaver_new_path);
			Set<String> list2 = dbeavernew_dbeaverold.get(DIFF_Flag);
			Set<String> list3 = dbeavernew_dbeaverold.get(OUT_OF_DATE_Flag);
			Set<String> list4 = dbeavernew_dbeaverold.get(NOT_EXSIST_Flag);
			
			Set<String> list5 = getListToCopy(list1, list2);
			
			Set<String> list6 = getListToManualMerge(list1, list2);
			
			executeMerge(list5, list6, list3, list4);
		}
		
		/**
		 * 执行合并工作
		 * 2017年4月13日 
		 * @param list5   升级修改部分 自动
		 * @param list6   升级修改部分 手动
		 * @param list3   升级新增部分 自动
		 * @param list4   升级删除部分 手动
		 */
		private void executeMerge(Set<String> list5, Set<String> list6, Set<String> list3, Set<String> list4) {
			// 升级修改部分，自动复制
			String msg = "************************listToCopy**************************************";
			printList(list5, msg);
			if (autoMerge) {
				Map<String, String> failedFiles = autoMerge(list5);
				copyFailedFiles.putAll(failedFiles);
				System.out.println("\n auto copy complete!");
			}
			// 升级新增部分，自动复制
			msg = "************************listToAdd***************************************";
			printList(list3, msg);
			if (autoMerge) {
				Map<String, String> failedFiles = autoMerge(list3);
				copyFailedFiles.putAll(failedFiles);
				System.out.println("\n auto add complete!");
			}
			// 打印复制失败文件
			if (!copyFailedFiles.isEmpty()) {
				checkFailedFiles(copyFailedFiles);
			}
			// 升级修改部分，手动更新
			msg = "************************listToManualMerge*******************************";
			printList(list6, msg);
			// 升级删除部分，手动删除
			msg = "************************listToManualDelete******************************";
			printList(list4, msg);
		}

		private void checkFailedFiles(Map<String, String> failedFiles) {
			if (!failedFiles.isEmpty()) {
				System.out.println("copy failed files>>>>");
				Iterator<Entry<String, String>> iterator = failedFiles.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> next = iterator.next();
					String key = next.getKey();
					String value = next.getValue();
					System.out.println(key + "   TO   " + value);
				}
			}
		}


		/**
		 * 拷贝listToCopy中的文件 
		 * src: 新版本文件 
		 * dest： Dbstudio对应文件 
		 * 2017年4月11日
		 * 
		 * @param listToCopy
		 */
		private Map<String, String> autoMerge(Set<String> listToCopy) {
			String srcPath = null, destPath = null;
			Map<String, String> failedFiles = new HashMap<>();
			for (String filePath : listToCopy) {
				srcPath = dbeaver_new_path + filePath;
				destPath = dbstudioPath + filePath;
				try {
					FileUtils.copyFile(srcPath, destPath);
				} catch (IOException e) {
					String errMsg = dbstudioPath + "\n" + dbeaver_new_path + "\n" + dbeaver_old_path;
					logger.error(errMsg, e);
					failedFiles.put(srcPath, destPath);
				}
			}
			return failedFiles;
	    }

		/**
		 * 4. 找出list2中存在而list1中没有的文件list3， 这部分文件是我们需要手动修改的文件 2017年4月11日
		 * 
		 * @param diffVersionList1
		 * @param diffVersionList2
		 * @return
		 */
		private Set<String> getListToManualMerge(Set<String> diffVersionList1, Set<String> diffVersionList2) {
			HashSet<String> complement2 = new HashSet<>();
			complement2.addAll(diffVersionList2);
			complement2.removeAll(diffVersionList1);
			return complement2;
		}

		/**
		 * 3. 找出list1与list2的交集， 这部分是可以直接从3.7.8复制到Dbstudio的，也是可以实现工具自动化更新到 部分
		 * 2017年4月11日
		 * 
		 * @param diffVersionList1
		 * @param diffVersionList2
		 * @return
		 */
		private Set<String> getListToCopy(Set<String> diffVersionList1, Set<String> diffVersionList2) {
			HashSet<String> intersection = new HashSet<>();
			intersection.addAll(diffVersionList1);
			intersection.retainAll(diffVersionList2);
			return intersection;
		}

		private Map<String, Set<String>> getVersionDiffResult(String oldVer, String newVer) {
			final Set<String> diff = new HashSet<>();
			final Set<String> same = new HashSet<>();
			final Set<String> notExsist = new HashSet<>();
			final Set<String> outOfDate = new HashSet<>();
			
			VersionDiffBuilder builder = new VersionDiffBuilder(oldVer, newVer);
			VersionDiffer differ = builder.threadNum(threadNum)
					.filter(this.filter)
					.processor(new Processor() {

						@Override
						public void resultProcessor(Set<String> diffFiles, Set<String> sameFiles, 
								Set<String> notExsistFiles, Set<String> outOfDateFiles) {
							diff.addAll(diffFiles);
							same.addAll(sameFiles);
							notExsist.addAll(notExsistFiles);
							outOfDate.addAll(outOfDateFiles);
						}
					}).build();

			differ.run();
			
			Map<String, Set<String>> map = new HashMap<>();
			map.put(DIFF_Flag, diff);
			map.put(SAME_Flag, same);
			map.put(NOT_EXSIST_Flag, notExsist);
			map.put(OUT_OF_DATE_Flag, outOfDate);
			return map;
		}


		public void printList(Set<String> files, String msg) {
			System.out.println(msg);
			System.out.println("File Size: " + files.size());
			// 排序后输出 字典序
			TreeSet<String> treeSet = new TreeSet<>(files);
			for (String file : treeSet) {
				System.out.println(file);
			}
		}

		@Override
		public String toString() {
			StringBuilder msg = new StringBuilder();
			msg.append("oldVersion : " + dbeaver_old_path).append("\n");
			msg.append("newVersion : " + dbeaver_new_path).append("\n");
			msg.append("mergeTo : " + dbstudioPath);
			return msg.toString();
		}
	}
}
