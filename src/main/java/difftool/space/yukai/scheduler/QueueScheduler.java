package difftool.space.yukai.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;

import difftool.space.yukai.differ.DiffVersionHandler;
/**
 * 无界队列
 * 文件数目巨大时可能内存溢出
 * @author kyu
 * @date 2017年4月10日
 */
public class QueueScheduler implements Scheduler{
	private ConcurrentLinkedQueue<DiffVersionHandler> queue = new ConcurrentLinkedQueue<DiffVersionHandler>();
	
	public void push(DiffVersionHandler diffVersion) {
		queue.add(diffVersion);
	}

	public DiffVersionHandler poll() {
		return queue.poll();
	}

}
