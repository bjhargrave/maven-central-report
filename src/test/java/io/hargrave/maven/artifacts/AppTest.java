package io.hargrave.maven.artifacts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.hargrave.maven.artifacts.MavenCentralArtifactReport.SearchResult;

public class AppTest {
	@Test
	public void testAppHasAGreeting() throws Exception {
		MavenCentralArtifactReport searcher = new MavenCentralArtifactReport();
		SearchResult result = searcher.query("g:%22biz.aQute.bnd%22+AND+a:%22biz.aQute.bndlib%22+AND+v:%223.5.0%22");
		assertNotNull("no result", result);
		assertEquals(1L, result.response.numFound);
		assertEquals(1L, result.response.docs.length);
		assertEquals("biz.aQute.bnd", result.response.docs[0].g);
		assertEquals("biz.aQute.bndlib", result.response.docs[0].a);
		assertEquals("3.5.0", result.response.docs[0].v);
    }
}
