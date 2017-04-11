package difftool.space.yukai;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import difflib.Chunk;
import difftool.space.yukai.differ.FileDiffer;
import difftool.space.yukai.entity.DiffVersionHandler;

public class FileDifferTest {
	@Test
	public void testInsert() {
		String version1 = "Y:\\dbaeaver\\dbeaver-3.6.5\\plugins\\org.jkiss.dbeaver.model\\src\\org\\jkiss\\dbeaver\\model\\data\\DBDAttributeTransformer.java";
		String version2 = "Y:\\dbaeaver\\dbeaver-3.6.6\\plugins\\org.jkiss.dbeaver.model\\src\\org\\jkiss\\dbeaver\\model\\data\\DBDAttributeTransformer.java";
		test(version1, version2);
	}
	@Test
	public void testDiff() {
		String version1 = "Y:\\dbaeaver\\dbeaver-3.6.5\\pom.xml";
		String version2 = "Y:\\dbaeaver\\dbeaver-3.6.6\\pom.xml";
		test(version1, version2);
	}
	
	@Test
	public void testSame() {
		String version1 = "Y:\\dbaeaver\\dbeaver-3.6.5\\LICENSE.md";
		String version2 = "Y:\\dbaeaver\\dbeaver-3.6.6\\LICENSE.md";
		test(version1, version2);
	}
	
	public void test(String version1,String version2) {
		System.out.println("***********" +version1 + " vs " + version2 + "***********");
		DiffVersionHandler handler = new DiffVersionHandler(version1, version2);
		FileDiffer differ = new FileDiffer(handler);
		try {
			boolean diff = differ.diff();
			System.out.println("diff ? :" + diff);
			List<Chunk> changesFromOriginal = differ.getChangesFromOriginal();
			List<Chunk> deletesFromOriginal = differ.getDeletesFromOriginal();
			List<Chunk> insertsFromOriginal = differ.getInsertsFromOriginal();
			System.out.println("changesFromOriginal********************");
			for (Chunk chunk : changesFromOriginal) {
				System.out.println(chunk.getPosition());
				System.out.println(chunk.getLines());
			}
			System.out.println("deletesFromOriginal********************");
			for (Chunk chunk : deletesFromOriginal) {
				System.out.println(chunk.getPosition());
				System.out.println(chunk.getLines());
			}
			System.out.println("insertsFromOriginal********************");
			for (Chunk chunk : insertsFromOriginal) {
				System.out.println(chunk.getPosition());
				System.out.println(chunk.getLines());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
