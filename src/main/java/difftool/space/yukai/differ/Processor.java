package difftool.space.yukai.differ;

import java.util.Set;

public interface Processor {
	void resultProcessor(Set<String> diffFiles, Set<String> sameFiles, Set<String> notExsistFiles);
}
