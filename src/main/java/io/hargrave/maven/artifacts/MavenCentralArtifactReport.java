package io.hargrave.maven.artifacts;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import aQute.bnd.http.HttpClient;
import aQute.bnd.version.MavenVersion;
import aQute.maven.api.Program;
import aQute.maven.api.Revision;

public class MavenCentralArtifactReport {
	private static final String	queryUrl	= "https://search.maven.org/solrsearch/select?rows=%s&start=%s&wt=json&q=%s";
	private final HttpClient	client		= new HttpClient();

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Must specify the query!");
		}
		String query = args[0];
		new MavenCentralArtifactReport().report(query);
	}

	void report(String query) throws Exception {
		List<Revision> revisions = query(query);
		Map<Program, Set<MavenVersion>> programs = revisions.stream()
			.collect(groupingBy(r -> r.program, TreeMap::new, mapping(r -> r.version, toCollection(TreeSet::new))));

		System.out.printf("%n%s groupId:artifactId pairs found for query: %s%n%n", programs.size(),
			URLDecoder.decode(query, "UTF-8"));
		programs.forEach((p, v) -> System.out.printf(" %-50s %s%n", p, v));
	}

	List<Revision> query(String query) throws Exception {
		List<Revision> revisions = null;
		long rows = 100;
		long start = 0;
		while (true) {
			URL url = new URL(String.format(queryUrl, rows, start, query));
			System.out.printf("Requesting %s...", url);
			SearchResult result = client.build()
				.headers("User-Agent", "Bnd")
				.get(SearchResult.class)
				.go(url);
			if (revisions == null) {
				revisions = new ArrayList<Revision>((int) result.response.numFound);
			}
			revisions.addAll(result.response.docsToRevisions());
			start = result.response.start + result.response.docs.length;
			System.out.printf(" found %s of %s%n", start, result.response.numFound);
			if (start >= result.response.numFound) {
				return revisions;
			}
		}
	}

	public static class SearchResult {
		public SearchResult() {}

		public long getLastModified() {
			return Arrays.stream(response.docs)
				.mapToLong(doc -> doc.timestamp)
				.max()
				.orElse(-1);
		}

		public ResponseHeader	responseHeader;
		public Response			response;
	}

	public static class ResponseHeader {
		public ResponseHeader() {}

		public Map<String, String> params;
	}

	public static class Response {
		public Response() {}

		public long		numFound;
		public long		start;
		public Doc[]	docs;
		List<Revision>	list;

		public List<Revision> docsToRevisions() {
			if (list != null)
				return list;
			return list = Arrays.stream(docs)
				.map(Doc::toRevision)
				.collect(toList());
		}
	}

	public static class Doc {
		public Doc() {}

		public String	id;
		public String	g;
		public String	a;
		public String	v;
		public String	latestVersion;
		public String	repositoryId;
		public String	p;
		public long		timestamp;
		public int		versionCount;
		public String[]	ec;

		public String getVersion() {
			return (v != null) ? v : latestVersion;
		}

		public Revision toRevision() {
			return Program.valueOf(g, a)
				.version(getVersion());
		}

		@Override
		public String toString() {
			return String.format("%s:%s:%s", g, a, getVersion());
		}
	}
}
