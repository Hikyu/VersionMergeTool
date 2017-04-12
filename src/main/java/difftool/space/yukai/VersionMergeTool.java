package difftool.space.yukai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import difftool.space.yukai.VersionDiffer.VersionDiffBuilder;
import difftool.space.yukai.differ.Processor;
import difftool.space.yukai.utils.FileUtils;

/**
 * 版本合并工具 思路：以Dbstudio 更新到 dbeaver 3.7.8为例。设当前Dbstudio已经更新到dbeaver3.7.5
 * 1. 找出Dbstudio与dbeaver3.7.5中内容相同文件list1，这部分文件是我们没有更改过的 
 * 2. 找出dbeaver3.7.8与dbeaver3.7.5不内容同的文件list2，这部分是3.7.8相对于3.7.5升级的部分，也是我们要对应升级的部分
 * 3. 找出list1与list2的交集，这部分是可以直接从3.7.8复制到Dbstudio的，也是可以实现工具自动化更新到 部分
 * 4. 找出list2中存在而list1中没有的文件list3，这部分文件是我们需要手动修改的文件
 * 
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

	public VersionMergeTool() {
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
	 */
	public void addToMergeVersion(String oldVersion, String newVersion, String toMerge, boolean autoMerge) {
		toMergeFiles.add(new MergeTool(toMerge, newVersion, oldVersion, autoMerge));
	}
	
	public void addToMergeVersion(String oldVersion, String newVersion, String toMerge) {
		addToMergeVersion(oldVersion, newVersion, toMerge, false);
	}
	
	public void doMerge() {
		for (MergeTool tool : toMergeFiles) {
			System.out.println("**********************************************************************");
			System.out.println(tool.toString());
			System.out.println("**********************************************************************");
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

		public MergeTool(String dbstudioPath, String dbeaver_new_path, String dbeaver_old_path, boolean autoMerge) {
			this.dbeaver_new_path = dbeaver_new_path;
			this.dbeaver_old_path = dbeaver_old_path;
			this.dbstudioPath = dbstudioPath;
			this.autoMerge = autoMerge;
			copyFailedFiles = new HashMap<>();
		}

		public void merge() {
			Set<String> diffVersionList1 = getDiffVersionList1();
			Set<String> diffVersionList2 = getDiffVersionList2();
			Set<String> listToCopy = getListToCopy(diffVersionList1, diffVersionList2);
			Set<String> listToManualMerge = getListToManualMerge(diffVersionList1, diffVersionList2);
			autoMerge(listToCopy);
			manualMerge(listToManualMerge);
		}
		
		@Override
		public String toString() {
			StringBuilder msg = new StringBuilder();
			msg.append("oldVersion : " + dbeaver_old_path).append("\n");
			msg.append("newVersion : " + dbeaver_new_path).append("\n");
			msg.append("mergeTo : " + dbstudioPath).append("\n");
			return msg.toString();
		}

		/**
		 * 打印listToManualMerge中的文件 2017年4月11日
		 * 
		 * @param listToManualMerge
		 */
		private void manualMerge(Set<String> listToManualMerge) {
			StringBuilder msg = new StringBuilder();
			msg.append("************************listToManualMerge******************************").append("\n");
			printList(listToManualMerge, msg.toString());
		}

		/**
		 * 拷贝listToCopy中的文件 
		 * src: 新版本文件 
		 * dest： Dbstudio对应文件 
		 * 2017年4月11日
		 * 
		 * @param listToCopy
		 */
		private void autoMerge(Set<String> listToCopy) {
			StringBuilder msg = new StringBuilder();
			msg.append("************************listToCopy******************************").append("\n");
			printList(listToCopy, msg.toString());
			
			if (autoMerge) {
				String srcPath = null, destPath = null;
				try {
					for (String filePath : listToCopy) {
						srcPath = dbeaver_new_path + filePath;
						destPath = dbstudioPath + filePath;
						FileUtils.copyFile(srcPath, destPath);
					}
				} catch (IOException e) {
					String errMsg = dbstudioPath + "\n" + dbeaver_new_path + "\n" + dbeaver_old_path;
					logger.error(errMsg, e);
					copyFailedFiles.put(srcPath, destPath);
				}
				
				System.out.println("auto merge complete!");
				if (!copyFailedFiles.isEmpty()) {
					System.out.println("合并失败文件>>>>");
					Iterator<Entry<String, String>> iterator = copyFailedFiles.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<String, String> next = iterator.next();
						String key = next.getKey();
						String value = next.getValue();
						System.out.println(key + "   TO   " + value);
					}
				}
			}
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

		/**
		 * 2. 找出dbeaver3.7.8与dbeaver3.7.5不内容同的文件list2，
		 * 这部分是3.7.8相对于3.7.5升级的部分，也是我们要对应升级的部分 2017年4月11日
		 * 
		 * @return
		 */
		private Set<String> getDiffVersionList2() {
			final Set<String> list = new HashSet<>();
			VersionDiffBuilder builder = new VersionDiffBuilder(dbeaver_old_path, dbeaver_new_path);
			VersionDiffer differ = builder.threadNum(4).processor(new Processor() {

				@Override
				public void resultProcessor(Set<String> diffFiles, Set<String> sameFiles, Set<String> notExsistFiles) {
					list.addAll(diffFiles);
				}
			}).build();

			differ.run();

			return list;
		}

		/**
		 * 1. 找出Dbstudio与dbeaver3.7.5中内容相同文件list1 这部分文件是我们没有更改过的 2017年4月11日
		 * 
		 * @return
		 */
		private Set<String> getDiffVersionList1() {
			final Set<String> list = new HashSet<>();
			VersionDiffBuilder builder = new VersionDiffBuilder(dbstudioPath, dbeaver_old_path);
			VersionDiffer differ = builder.threadNum(4).processor(new Processor() {

				@Override
				public void resultProcessor(Set<String> diffFiles, Set<String> sameFiles, Set<String> notExsistFiles) {
					list.addAll(sameFiles);
				}
			}).build();

			differ.run();

			return list;
		}

		public void printList(Set<String> diffFiles, String msg) {
			System.out.println(msg);
			System.out.println("File Size: " + diffFiles.size());
			for (String string : diffFiles) {
				System.out.println(string);
			}
		}

	}
}
