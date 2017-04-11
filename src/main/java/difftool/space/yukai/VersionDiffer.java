package difftool.space.yukai;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import difftool.space.yukai.differ.CountableThreadPool;
import difftool.space.yukai.differ.FileDiffer;
import difftool.space.yukai.differ.Processor;
import difftool.space.yukai.entity.DiffVersionHandler;
import difftool.space.yukai.scheduler.QueueScheduler;
import difftool.space.yukai.scheduler.Scheduler;
import difftool.space.yukai.utils.FileUtils;

/**
 * 版本对比入口 1. 生成文件对比队列 2. 多线程并发读取队列，进行比较 3. 生成对比结果相同和不同的文件集合
 * 
 * 
 * @author kyu
 * @date 2017年4月10日
 */
public class VersionDiffer {
	/* 旧版本文件 */
	private final String version1RootPath;
	/* 新版本文件 */
	private final String version2RootPath;
	/* 最大线程数 */
	private int threadNum;
	/* 文件过滤 */
	private FilenameFilter filter;
	/* 对比文件队列 支持并发 */
	private Scheduler scheduler;
	/* 线程池 */
	private CountableThreadPool pool;
	/* 针对旧版本，新版本与旧版本内容相同的文件集合   支持并发*/
	private Set<String> sameFiles;
	/*
	 * 针对旧版本，新版本与旧版本内容不同的文件集合，包括： 1. 新版本有，旧版本没有的文件 2. 新旧版本不同的文件
	 *  支持并发
	 */
	private Set<String> diffFiles;
	/* 旧版本有而新版本没有的文件集合   支持并发*/
	private Set<String> notExsistFiles;
	/* 对比结果处理器*/
	private Processor processor;

	public static class VersionDiffBuilder {
		
		private final String version1RootPath;
		private final String version2RootPath;
		private int threadNum = 4;
		private Processor processor = new Processor() {
			
			@Override
			public void resultProcessor(Set<String> diffFiles, Set<String> sameFiles, Set<String> notExsistFiles) {
				// do nothing
			}
		};
		private FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};

		public VersionDiffBuilder(String oldversionRootPath, String newVersionRootPath) {
			if (!new File(oldversionRootPath).exists()) {
				throw new IllegalArgumentException("文件路径不存在! " + oldversionRootPath);
			}
			if (!new File(newVersionRootPath).exists()) {
				throw new IllegalArgumentException("文件路径不存在! " + newVersionRootPath);
			}
			this.version1RootPath = oldversionRootPath;
			this.version2RootPath = newVersionRootPath;
		}

		public VersionDiffBuilder threadNum(int threadNum) {
			this.threadNum = threadNum;
			return this;
		}

		public VersionDiffBuilder filter(FilenameFilter filter) {
			this.filter = filter;
			return this;
		}
		
		public VersionDiffBuilder processor(Processor processor) {
			this.processor = processor;
			return this;
		}

		public VersionDiffer build() {
			return new VersionDiffer(this);
		}

	}

	private VersionDiffer(VersionDiffBuilder builder) {
		this.version1RootPath = builder.version1RootPath;
		this.version2RootPath = builder.version2RootPath;
		this.filter = builder.filter;
		this.threadNum = builder.threadNum;
		this.pool = new CountableThreadPool(threadNum);
		this.processor = builder.processor;
		scheduler = new QueueScheduler();
		sameFiles = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		diffFiles = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		notExsistFiles = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	}

	/**
	 * 队列的填充与消费是顺序进行的 先递归找到所有的对比文件，导入队列 然后消费队列，进行对比
	 * 
	 * 队列是无界的，可能造成内存溢出的情况
	 * 
	 * 修改点： 可以改善为生产者消费者模式，将队列的填充与消费并发进行 队列设为有节队列，控制内存 
	 * 2017年4月10日
	 */
	public void run() {
		// 填充队列
		getToDiffFiles(version1RootPath, version2RootPath);
		
		// 比对文件
		while(true) {
			final DiffVersionHandler handler = scheduler.poll();
			if (handler == null) {
				if (pool.getThreadAlive() == 0) {
					//结束
					break;
				}
//				Thread.sleep(1000);
			} else {
				pool.execute(new Runnable() {
					public void run() {
						try {
							FileDiffer differ = new FileDiffer(handler);
							boolean diff = differ.diff();
							String commonPath = handler.getVersion1Path().substring(version1RootPath.length());
							if (diff) {//差异文件
								diffFiles.add(commonPath);
							} else {
								sameFiles.add(commonPath);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
		// 关闭线程池
		pool.shutdown();
		// 处理对比结果
		processor.resultProcessor(diffFiles, sameFiles, notExsistFiles);
	}

	/**
	 * 获取所要比较的新旧版本文件对集合 思路：简单粗暴 
	 * 1. 两个版本的各个文件路径去除根路径，分别存入set 
	 * 2. 两个set求出交集，这部分是要对比的集合
	 * 3. 求出新版本有，旧版本没有的文件集合，这部分属于diffFiles
	 * 4. 求出旧版本有，新版本没有的文件集合，这部分属于notExsistFiles
	 * 
	 * 2017年4月10日
	 */
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
			//求出新版本有，旧版本没有的文件集合
			HashSet<String> complement2 = new HashSet<>();
			complement2.addAll(version2FilterPaths);
			complement2.removeAll(version1FilterPaths);
			diffFiles.addAll(complement2);
			
			//求出旧版本有，新版本没有的文件集合
			HashSet<String> complement1 = new HashSet<>();
			complement1.addAll(version1FilterPaths);
			complement1.removeAll(version2FilterPaths);
			notExsistFiles.addAll(complement1);
			
			//求交集
			HashSet<String> intersection = new HashSet<>();
			intersection.addAll(version1FilterPaths);
	        intersection.retainAll(version2FilterPaths);
	        
	        //加入队列
	        for (String file : intersection) {
				String version1AbsolutePath = version1RootPath.concat(file);
				String version2AbsolutePath = version2RootPath.concat(file);
				DiffVersionHandler handler = new DiffVersionHandler(version1AbsolutePath, version2AbsolutePath);
				scheduler.push(handler);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	
}
