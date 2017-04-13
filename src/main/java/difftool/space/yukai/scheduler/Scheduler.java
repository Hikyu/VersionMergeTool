package difftool.space.yukai.scheduler;

import difftool.space.yukai.differ.DiffVersionHandler;

public interface Scheduler {
    void push(DiffVersionHandler diffVersion);
	DiffVersionHandler poll();
}
