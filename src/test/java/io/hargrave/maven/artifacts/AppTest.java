package io.hargrave.maven.artifacts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import aQute.maven.api.Revision;

public class AppTest {
	@Test
	public void testAppHasAGreeting() throws Exception {
		MavenCentralArtifactReport searcher = new MavenCentralArtifactReport();
		List<Revision> result = searcher.query("g:%22biz.aQute.bnd%22+AND+a:%22biz.aQute.bndlib%22+AND+v:%223.5.0%22");
		assertNotNull("no result", result);
		assertEquals(1L, result.size());
		Revision revision = result.get(0);
		assertEquals("biz.aQute.bnd", revision.group);
		assertEquals("biz.aQute.bndlib", revision.artifact);
		assertEquals("3.5.0", revision.version.toString());
    }
}
