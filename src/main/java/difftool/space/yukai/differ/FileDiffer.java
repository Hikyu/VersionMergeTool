package difftool.space.yukai.differ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difftool.space.yukai.entity.DiffVersionHandler;
import difftool.space.yukai.entity.DiffVersionHandler.DiffVersion;
/**
 * 文件对比工具
 * @author kyu
 * @date 2017年4月10日
 */
public class FileDiffer {
	private final DiffVersionHandler versionHandler;
	private List<Delta> deltas = null;

	public FileDiffer(DiffVersionHandler versionHandler) {
		this.versionHandler = versionHandler;
	}

	/**
	 * 判断文件是否有差异
	 * 2017年4月10日 
	 * @return 
	 *    true 文件有差异
	 *    false 文件相同
	 * @throws IOException
	 */
	public boolean diff() throws IOException {
		List<Chunk> changesFromOriginal = getChangesFromOriginal();
		List<Chunk> deletesFromOriginal = getDeletesFromOriginal();
		List<Chunk> insertsFromOriginal = getInsertsFromOriginal();
		return !(changesFromOriginal.isEmpty() && deletesFromOriginal.isEmpty() && insertsFromOriginal.isEmpty());
	}

	private List<Chunk> getChunksByType(Delta.TYPE type) throws IOException {
		final List<Chunk> listOfChanges = new ArrayList<Chunk>();
		final List<Delta> deltas = getDeltas();
		for (Delta delta : deltas) {
			if (delta.getType() == type) {
				listOfChanges.add(delta.getRevised());
			}
		}
		return listOfChanges;
	}

	private List<Delta> getDeltas() throws IOException {
		if (deltas == null) {
			deltas = DiffUtils.diff(versionHandler.getVersionLines(DiffVersion.VERSION1),
					versionHandler.getVersionLines(DiffVersion.VERSION2)).getDeltas();
		}
		return deltas;
	}

	public List<Chunk> getChangesFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.CHANGE);
	}

	public List<Chunk> getInsertsFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.INSERT);
	}

	public List<Chunk> getDeletesFromOriginal() throws IOException {
		return getChunksByType(Delta.TYPE.DELETE);
	}
}
